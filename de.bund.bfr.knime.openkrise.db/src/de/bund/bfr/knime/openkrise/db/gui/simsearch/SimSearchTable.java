/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.openkrise.db.gui.simsearch;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.IntStream;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.RowSorter.SortKey;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.plaf.MenuItemUI;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import de.bund.bfr.knime.openkrise.db.gui.dbtable.header.GuiMessages;
import de.bund.bfr.knime.openkrise.db.gui.simsearch.SimSearchJTable.RowHeaderColumnRenderer;

public class SimSearchTable extends JScrollPane{

  
  /**
	 * 
	 */
	private static final long serialVersionUID = 6567218882276999489L;
	
	
	private TableColumnModel tableColumnModel;
  private TableColumnModel rowHeaderColumnModel;
  private SimSearchJTable table;
  private SimSearchJTable rowHeaderColumnTable;
  
  
  private JTextField filterTextBox;
  private JCheckBox useRegexFilterCheckBox;
  private AbstractButton inactiveRowFilterSwitch;
  
  private JButton ignoreSimSetButton;
  private JButton ignoreAllPairsInSimSetButton;
  
  private ViewSettings viewSettings;
  private int[] rowHeights;
  private List<Integer> frozenColumns; 
  private Set<Integer> invisibleColumns; 
    
  
  private JPopupMenu popupMenu;
  private CopyAdapter copyAdapter;


  public SimSearchTable(ViewSettings viewSettings) {
    super(); 
    this.viewSettings = viewSettings;
    this.init();
  }

  private void init() {
    this.viewSettings = new ViewSettings();
    this.initTables();
    
    JViewport jv = new JViewport();
    jv.setView(this.rowHeaderColumnTable);
    jv.setPreferredSize(this.rowHeaderColumnTable.getMaximumSize());

    // With out shutting off autoResizeMode, our tables won't scroll
    // correctly (horizontally, anyway)
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    this.setRowHeader(jv);
    
    
    this.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, this.rowHeaderColumnTable
        .getTableHeader());
  }

  @SuppressWarnings("serial")	private void initColumnModels() {
    this.tableColumnModel = new DefaultTableColumnModel() {
      private static final int COLUMN_WIDTH_MIN = 10;
      private static final int COLUMN_WIDTH_MAX = Integer.MAX_VALUE;
      
      @Override 
      public void addColumn(TableColumn tc) {
        // Ignore row header column, frozen columns and invisible columns
        if(tc.getModelIndex()==0 || frozenColumns.contains(tc.getModelIndex()) || invisibleColumns.contains(tc.getModelIndex())) {  
          return;
        }
        tc.setMinWidth(COLUMN_WIDTH_MIN); // just for looks, really...
        tc.setMaxWidth(COLUMN_WIDTH_MAX);
        tc.setResizable(true);
        super.addColumn(tc);
      }
    };

    this.rowHeaderColumnModel = new DefaultTableColumnModel() {
      private static final int ROW_HEADER_WIDTH = 40;
      private static final int FREEZE_COLUMN_MAX_WIDTH = Integer.MAX_VALUE; //400;
      private static final int FREEZE_COLUMN_MIN_WIDTH = 10; //20;
      
      public void addColumn(TableColumn tc) {
        if(tc.getModelIndex()==0 || frozenColumns.contains(tc.getModelIndex())) {
          if(tc.getModelIndex()==0) {
            // RowHeader column
            tc.setMaxWidth(ROW_HEADER_WIDTH); //    tc.getPreferredWidth());
            tc.setMinWidth(ROW_HEADER_WIDTH);
            tc.setResizable(false);
          } else {
        	  // frozen columns
            tc.setMinWidth(FREEZE_COLUMN_MIN_WIDTH);
            tc.setMaxWidth(FREEZE_COLUMN_MAX_WIDTH);
          }
          super.addColumn(tc);
        }
        // Drop the rest of the columns . . . 
      }
    };
  }

  // this method is needed because a sort on a column header row header table is otherwise not synchronized to the remaining table
  private void addMouseListenerToTable() {
    MouseListener mouseListener = new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        if(e.getButton()==MouseEvent.BUTTON1) {
          e.consume();
          SimSearchTable.this.table.updateUI();
          SimSearchTable.this.rowHeaderColumnTable.updateUI();
        }
      }

    };

    this.table.getTableHeader().addMouseListener(mouseListener);
  }
  
  private void addDragAndDropFeature() {
    SimSearchTableRowTransferHandler transferHandler = new SimSearchTableRowTransferHandler(this);

    Repainter dropLocationRepainter = new Repainter();
    
    Arrays.asList(this.table,this.rowHeaderColumnTable).forEach( t -> {
      t.setTransferHandler(transferHandler);
      t.setDropMode(DropMode.ON_OR_INSERT_ROWS);
      t.setDragEnabled(true);
      t.addPropertyChangeListener("dropLocation", dropLocationRepainter);
    });
  }
  
  
  private void addRowResizeFeature() {
	  this.rowHeaderColumnTable.setRowResizingAllowed(true);
  }
  
  private void freezeUnfreezeColumn(int columnModelIndex) {
	  Dimension dim = null;
    if(this.frozenColumns.contains(columnModelIndex)) {
      // unfreeze operation
      int columnViewIndex = this.rowHeaderColumnTable.convertColumnIndexToView(columnModelIndex);
      TableColumn tableColumn = this.rowHeaderColumnTable.getColumnModel().getColumn(columnViewIndex);
      
      dim = new Dimension(this.rowHeaderColumnTable.getWidth() - tableColumn.getWidth(), SimSearchTable.this.rowHeaderColumnTable.getHeight());
      
      this.rowHeaderColumnModel.removeColumn(tableColumn);
       this.frozenColumns.remove((Integer) columnModelIndex);
      tableColumn.setResizable(true);
      this.tableColumnModel.addColumn(tableColumn);
      this.tableColumnModel.moveColumn(this.tableColumnModel.getColumnCount()-1, 0);
    } else if(this.frozenColumns.isEmpty()) {
      // this is a valid freeze operation
      int columnViewIndex = this.table.convertColumnIndexToView(columnModelIndex);
      TableColumn tableColumn = this.table.getColumnModel().getColumn(columnViewIndex); 
      
      dim = new Dimension(this.rowHeaderColumnTable.getWidth() + tableColumn.getWidth(), SimSearchTable.this.rowHeaderColumnTable.getHeight());
      
      this.table.removeColumn(tableColumn);
      this.frozenColumns.add(columnModelIndex);
      this.rowHeaderColumnTable.addColumn(tableColumn);
      
    } else {
      return;
    }
    
    SimSearchTable.this.rowHeaderColumnTable.setSize(dim);
    SimSearchTable.this.getRowHeader().setPreferredSize(dim);
    this.getRowHeader().setPreferredSize(new Dimension(dim.width, this.rowHeaderColumnTable.getMaximumSize().height));
  }
  
  private void updateTablePosition() {
    JViewport jv = this.getRowHeader();
    if(jv==null) return;
    
    jv.setPreferredSize(this.rowHeaderColumnTable.getMaximumSize());
  }
  
  private void showHeaderPopUp(Component comp, int x, int y) {
    this.popupMenu.removeAll();
    
    JTableHeader tableHeader = (JTableHeader) comp;
    int columnIndex = tableHeader.columnAtPoint(new Point(x,y));
    int columnModelIndex = tableHeader.getTable().convertColumnIndexToModel(columnIndex);
    
    if(columnModelIndex>0) {
      JMenuItem menuItem = new JMenuItem();
      if(this.frozenColumns.contains(columnModelIndex))
        menuItem.setText("unfreeze");
      else {
        menuItem.setText("freeze");
        menuItem.setEnabled(this.frozenColumns.isEmpty());
      }
      menuItem.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent arg0) {
          SimSearchTable.this.freezeUnfreezeColumn(columnModelIndex);          
        }
        
      });
      this.popupMenu.add(menuItem);
    }
    
    JMenu showColumnsMenu = new JMenu("Show columns");
    this.addShowColumnsSubMenuItems(showColumnsMenu);
    this.popupMenu.add(showColumnsMenu);
    // this.popupMenu.show(comp, x, y);
    // don't use the tableHeader as invoker
    // it causes an error if the last column is removed from the table
    this.popupMenu.show(tableHeader.getTable(), x, y);
  }
  
  private void showRowPopUp(Component comp, int x, int y) {
	    this.popupMenu.removeAll();
	    
	    JTable table = (JTable) comp;
	    Point p = new Point(x,y);
	    int columnIndex = table.columnAtPoint(p);
	    int rowAtPoint = table.rowAtPoint(p);
	    if(rowAtPoint<0 || columnIndex<0) return;
	    
	    int[] selectedRows = table.getSelectedRows();
	    int[] rowIndices;
	    if(IntStream.of(selectedRows).anyMatch(i -> i==rowAtPoint)) {
	    	// take all selected Rows
	    	rowIndices = selectedRows;
	    } else {
	    	rowIndices = new int[] {rowAtPoint};
	    }
	    
	    int[] rowModelIndices = new int[rowIndices.length];
	    for(int i=0; i<rowIndices.length; ++i) rowModelIndices[i] = table.convertRowIndexToModel(rowIndices[i]);
	    
	    boolean isUnmergeAvailable = IntStream.of(rowModelIndices).allMatch(i -> (this.getModel().isMerged(i) || this.getModel().getMergeCount(i)>0));
	    
	    
      JMenuItem unmergeMenuItem = new JMenuItem("unmerge");
      unmergeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_MASK));
      unmergeMenuItem.getAccessibleContext().setAccessibleDescription(
          "Unmerge rows.");
      unmergeMenuItem.setEnabled(isUnmergeAvailable);
      
      unmergeMenuItem.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent arg0) {
          SimSearchTable.this.getModel().unmergeRows(rowModelIndices);          
        }
        
      });
      this.popupMenu.add(unmergeMenuItem);
      
      if(this.copyAdapter!=null) {
	      JMenuItem copyMenuItem = new JMenuItem("copy");
	      copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
	      copyMenuItem.getAccessibleContext().setAccessibleDescription(
	          "Copy data to clipboard.");
	      
	      copyMenuItem.addActionListener(new ActionListener() {
	
	        @Override
	        public void actionPerformed(ActionEvent arg0) {
	          copyAdapter.copyData(table, rowIndices, columnIndex);          
	        }
	        
	      });
	      this.popupMenu.add(copyMenuItem);
      }
	    
	    this.popupMenu.show(table, x, y);
  }
  
  private void addShowColumnsSubMenuItems(JMenu menu) {
    for(int column=1; column<this.getModel().getColumnCount(); ++column) {
      
      JCheckBoxMenuItem item = new ColumnVisibilityCheckBoxMenuItem(GuiMessages.getString(this.getModel().getColumnName(column)),column);
      item.setSelected(!this.invisibleColumns.contains(column));
      
      menu.add(item);
    }
  }
  
  private void setColumnVisible(int modelColumnIndex, boolean visible) {
    if(visible) {
      if(!this.invisibleColumns.contains(modelColumnIndex)) return;
      this.invisibleColumns.remove((Integer) modelColumnIndex);
      TableColumn tableColumn = new TableColumn(modelColumnIndex);
      this.table.addColumn(tableColumn);
      this.table.initColumnRenderer(tableColumn);
      tableColumn.setPreferredWidth(this.table.getPreferredColumnWidth(this.table.getColumnCount()-1));
    } else {
      if(this.invisibleColumns.contains(modelColumnIndex)) return;
      this.invisibleColumns.add(modelColumnIndex);
      if(this.frozenColumns.contains((Integer) modelColumnIndex)) {
        // this is a frozen column
        this.frozenColumns.remove((Integer) modelColumnIndex);
        int viewColumnIndex = this.rowHeaderColumnTable.convertColumnIndexToView(modelColumnIndex);
        if(viewColumnIndex>=0) {
          TableColumn tableColumn = this.rowHeaderColumnTable.getColumnModel().getColumn(viewColumnIndex); 
          this.rowHeaderColumnTable.getColumnModel().removeColumn(tableColumn);
          this.updateTablePosition();
        }
      } else {
        // not a frozen column
        int viewColumnIndex = this.table.convertColumnIndexToView(modelColumnIndex);
        if(viewColumnIndex>=0) {
          TableColumn tableColumn = this.table.getColumnModel().getColumn(viewColumnIndex); 
          this.table.getColumnModel().removeColumn(tableColumn);
        }
      }
    }
    this.updateVisibleSorterColumns(this.getRowSorter());
  }
  
  private void updateVisibleSorterColumns(SimSearchRowSorter rowSorter) {
	  //SimSearchRowSorter rowSorter = this.getRowSorter();
	  if(rowSorter!=null) {
	    List<Integer> visibleColumns = new ArrayList<>();
        for(JTable t: Arrays.asList(this.table, this.rowHeaderColumnTable)) for(int i=(t==this.rowHeaderColumnTable?1:0); i<t.getColumnCount(); ++i) visibleColumns.add(t.convertColumnIndexToModel(i));
        rowSorter.setVisibleColumns(Collections.unmodifiableList(visibleColumns));
	  }
  }
  
  private void initTables() {
    this.initColumnModels();

    this.table = new SimSearchJTable(this.viewSettings);
    this.table.setAutoCreateColumnsFromModel(false);
    
    this.getViewport().setView(this.table);
    this.rowHeaderColumnTable = new SimSearchJTable(this.viewSettings);
    this.rowHeaderColumnTable.setAutoCreateColumnsFromModel(false);

    this.table.setColumnModel(this.tableColumnModel);
    this.rowHeaderColumnTable.setColumnModel(this.rowHeaderColumnModel);
    this.rowHeaderColumnTable.getTableHeader().setReorderingAllowed(false);
    
    this.table.setPartnerTable(this.rowHeaderColumnTable);
    this.rowHeaderColumnTable.setPartnerTable(this.table);
    
    this.addPopupFeature();
    this.addFrozenColumnsResizeFeature();
    
    // Make sure that selections between the main table and the header stay
    // in sync (by sharing the same model)
    table.setSelectionModel(this.rowHeaderColumnTable.getSelectionModel());
    
    
    this.addMouseListenerToTable();
    this.addDragAndDropFeature();
    this.addRowResizeFeature();
    this.addCopyFeature();
  }
  
  private void addCopyFeature() {
	  this.copyAdapter = new CopyAdapter(this.table, this.rowHeaderColumnTable);
  }
  
  private void addSelectionAndRowHeightUpdateOnSortFix(SimSearchRowSorter rowSorter) {
	  rowSorter.addRowSorterListener(new RowSorterListener() {

		@Override
		public void sorterChanged(RowSorterEvent e) {
			
			// update row selection
			int[] selectedRows = table.getSelectedRows();
			
			table.clearSelection();
			for(int i=0; i<selectedRows.length; ++i) { 
				int modelIndex = e.convertPreviousRowIndexToModel(selectedRows[i]);
				int viewIndex = ( modelIndex>=0 ? e.getSource().convertRowIndexToView(modelIndex) : -1 );
				if(viewIndex>=0) {
					table.addRowSelectionInterval(viewIndex, viewIndex);
				}
			}
			// update row heights
			updateRowHeights();
			// the next call is needed 
			// because the table does not show the correct status if the filter removes all rows from view 
			updateView();
			
		}
		  
	  });
  }
  
  private void addPopupFeature() {
	  this.popupMenu = new JPopupMenu();
	    
	    MouseListener popupListener = new MouseAdapter() {
	      public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	        	if(e.getSource() instanceof JTable) {
	        		SimSearchTable.this.showRowPopUp(e.getComponent(), e.getX(), e.getY());
	        	} else {
	        		SimSearchTable.this.showHeaderPopUp(e.getComponent(), e.getX(), e.getY());
	        	}
	        }
	    }
	    };
	    
	    Arrays.asList(this.table, this.rowHeaderColumnTable).forEach(t -> {
	    	t.addMouseListener(popupListener);
	    	t.getTableHeader().addMouseListener(popupListener);
	    });
	    
  }

  private void addFrozenColumnsResizeFeature() {
	  MouseAdapter mouseAdapter = new MouseAdapter() {
	      private int columnWidthAtMouseDragStart = -1;
	      private int tableWidthAtMouseDragStart = -1;
	      private int mouseDragStartX = -1;
	      private TableColumn tableColumn = null;
	      
	      @Override
	      public void mouseClicked(MouseEvent e) {
	        if(!e.isConsumed()) {
	          if(SwingUtilities.isLeftMouseButton(e)) {
	            // 
	            SimSearchTable.this.updateView();
	          }
	        }
	      }

	      @Override
	      public void mouseReleased(MouseEvent e) {
	        this.tableColumn = null;
	      }
	      
	      @Override
	      public void mousePressed(MouseEvent e) {
	    	  if(SwingUtilities.isLeftMouseButton(e)) {
	    		  // this might be the start of an drag operation
	    		  JTableHeader header = (JTableHeader) e.getComponent();
		            int columnIndex = header.columnAtPoint(e.getPoint());
		            this.tableColumn = header.getColumnModel().getColumn(columnIndex);
		            this.tableWidthAtMouseDragStart = SimSearchTable.this.rowHeaderColumnTable.getWidth();
		            this.columnWidthAtMouseDragStart = this.tableColumn.getWidth();
		            this.mouseDragStartX = e.getX();
	    	  }
	        
	      }
	      
	      @Override
	      public void mouseDragged(MouseEvent arg0) {
	    	  
	        if(SwingUtilities.isLeftMouseButton(arg0)) {
	          
	          if(this.tableColumn==null) {

	          } else {
	           int delta = arg0.getX() - this.mouseDragStartX;
	      
	            tableColumn.setPreferredWidth(this.columnWidthAtMouseDragStart + delta);
	            
	            Dimension dim = new Dimension(this.tableWidthAtMouseDragStart + delta, SimSearchTable.this.rowHeaderColumnTable.getHeight());
	            SimSearchTable.this.rowHeaderColumnTable.setSize(dim);
	            SimSearchTable.this.getRowHeader().setPreferredSize(dim);
	          } 
	        }
	      }
	    };
	    this.rowHeaderColumnTable.getTableHeader().addMouseListener(mouseAdapter);
	    this.rowHeaderColumnTable.getTableHeader().addMouseMotionListener(mouseAdapter);
	    
  }
  
  private SimSearchTableModel getModel() {
    return ((this.table==null || this.table.getModel()==null || !(this.table.getModel() instanceof SimSearchTableModel)) ? null :  (SimSearchTableModel) this.table.getModel());
  }
  
  private SimSearchRowSorter getRowSorter() {
    return ((this.table==null || this.table.getRowSorter()==null) ? null :  (SimSearchRowSorter) this.table.getRowSorter());
  }
  
  private int[] convertViewRowsToModelRows(int[] rows) {
    for(int i=0; i<rows.length; ++i) rows[i] = this.table.getRowSorter().convertRowIndexToModel(rows[i]);
    return rows;
  }
  
  private int convertDropRowToModelRow(int row) {
    return (row>=this.table.getRowCount()?-1:this.table.getRowSorter().convertRowIndexToModel(row));
  }

  protected void updateRowHeader() {
    ((SimSearchJTable.RowHeaderColumnRenderer)  this.rowHeaderColumnTable.getColumnModel().getColumn(0).getCellRenderer()).emptyCache();
    this.table.updateUI();
    this.rowHeaderColumnTable.updateUI();
  }

  private void processInactiveRowFilterEnabledChangedEvent() {
    if(this.getRowSorter()!=null) {
      this.getRowSorter().setInactiveRowFilterEnabled(this.inactiveRowFilterSwitch.isSelected());
    }
  }
  
  private void processTextFilterChangedEvent() {

    if(this.filterTextBox==null || this.getRowSorter()==null) return;

    this.applyTextFilterToRowSorter();
  }
  
  private void applyTextFilterToRowSorter() {
    if(this.filterTextBox==null || this.getRowSorter()==null) return;
    
    if(this.useRegexFilterCheckBox!=null && this.useRegexFilterCheckBox.isSelected()) {
      try {
        Pattern pattern = Pattern.compile( this.filterTextBox.getText() );
        this.filterTextBox.setBackground(Color.white);
        this.getRowSorter().setRowFilter(pattern);
        this.filterTextBox.setToolTipText("Enter regular expression to filter rows.");
      } catch(PatternSyntaxException e) {
    	
    	  ((SimSearchRowSorter) this.table.getRowSorter()).setRowFilter("");
        this.filterTextBox.setBackground(Color.RED);
        this.filterTextBox.setToolTipText("The syntax of the regular expression is not valid.");
      }
    } else {
      ((SimSearchRowSorter) this.table.getRowSorter()).setRowFilter(this.filterTextBox.getText());
      this.filterTextBox.setBackground(Color.white);
      this.filterTextBox.setToolTipText("Enter text to filter rows.");
    }
  }


  public void registerRowTextFilter(JTextField filterTextBox, JCheckBox useRegExFilterCheckBox) {

    if(filterTextBox!=null) {

      this.filterTextBox = filterTextBox;
      this.filterTextBox.getDocument().addDocumentListener(new DocumentListener() {

        @Override
        public void changedUpdate(DocumentEvent e) {
          SimSearchTable.this.processTextFilterChangedEvent();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
          SimSearchTable.this.processTextFilterChangedEvent();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
          SimSearchTable.this.processTextFilterChangedEvent();
        }

      });
      
      if(useRegExFilterCheckBox!=null) {
        this.useRegexFilterCheckBox = useRegExFilterCheckBox;

        this.useRegexFilterCheckBox.addChangeListener(new ChangeListener()  {
          @Override
          public void stateChanged(ChangeEvent arg0) {
            SimSearchTable.this.processTextFilterChangedEvent();
          }
        }
            );   

      }
    }
  }
  
  public void registerInactiveRowFilterSwitch(AbstractButton inactiveRowFilterSwitch) {
    if(inactiveRowFilterSwitch==null) return;
    this.inactiveRowFilterSwitch = inactiveRowFilterSwitch;
    inactiveRowFilterSwitch.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        SimSearchTable.this.processInactiveRowFilterEnabledChangedEvent();
      }

    });
  }
  
  public void registerSimSetIgnoreButtons(JButton ignoreSimSetButton, JButton ignoreAllPairsInSimSetButton) {
	  if(ignoreSimSetButton==null) return;
	  if(ignoreAllPairsInSimSetButton==null) return;
	  this.ignoreSimSetButton = ignoreSimSetButton;
	  this.ignoreAllPairsInSimSetButton = ignoreAllPairsInSimSetButton;
	  ActionListener actionListener = new ActionListener() {

		  @Override
		  public void actionPerformed(ActionEvent arg0) {
			  SimSearchTable.this.processIgnoreButtonEvent((JButton) arg0.getSource());
		  };
	  };
	  this.ignoreSimSetButton.addActionListener(actionListener);
	  this.ignoreAllPairsInSimSetButton.addActionListener(actionListener);
  }

  private void processIgnoreButtonEvent(JButton ignoreButton) {
	int n = this.getModel().getRowCount();
	for(int row=0; row<n; ++row) if(this.table.convertRowIndexToView(row)<0 && !this.getModel().isMerged(row))
		if(JOptionPane.showConfirmDialog(this, "Not all rows are shown in the view (filter is active). Proceed nethertheless?","Confirm dissimilarity mark",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)!=JOptionPane.YES_OPTION) return;
	
    if(ignoreButton == this.ignoreSimSetButton) this.getModel().ignoreSimSet();
    else if(ignoreButton == this.ignoreAllPairsInSimSetButton) this.getModel().ignoreAllPairsInSimSet();
  }

  
  protected ViewSettings getViewSettings() {
	  return this.viewSettings;
  }
  
  public boolean isSimSetIgnored() {
    return (this.getModel()==null?false:this.getModel().isSimSetIgnored());
  }
  
  public boolean isSimSetIgnoreAvailable() {
    return (this.getModel()==null?false:this.getModel().isSimSetIgnoreAvailable());
  }
  
  public boolean isMergeValid(int[] rowsToMerge, int rowToMergeTo) {
    rowsToMerge = convertViewRowsToModelRows(rowsToMerge);
    rowToMergeTo = convertViewRowsToModelRows(new int[] {rowToMergeTo})[0];
    return ((SimSearchTableModel) this.table.getModel()).isMergeValid(rowsToMerge, rowToMergeTo);
  }
  
  
  public void mergeRows(int[] rowsToMerge, int rowToMergeTo) {
    rowsToMerge = convertViewRowsToModelRows(rowsToMerge);
    rowToMergeTo = convertViewRowsToModelRows(new int[] {rowToMergeTo})[0];
    this.getModel().mergeRows(rowsToMerge, rowToMergeTo);
  }

  public boolean isRowMoveValid(int[] rowsToMove, int rowToMoveBefore) {
    rowsToMove = convertViewRowsToModelRows(rowsToMove);
    rowToMoveBefore = convertDropRowToModelRow(rowToMoveBefore);
    return this.getRowSorter().isRowMoveValid(rowsToMove, rowToMoveBefore);
  }
  
  public void moveRows(int[] rowsToMove, int rowToMoveBefore) {
    rowsToMove = convertViewRowsToModelRows(rowsToMove);
    rowToMoveBefore = convertDropRowToModelRow(rowToMoveBefore);
    this.getRowSorter().moveRows(rowsToMove, rowToMoveBefore);
  }
  
  private boolean isInactiveRowFilterEnabled() { return (this.inactiveRowFilterSwitch==null?false:this.inactiveRowFilterSwitch.isSelected()); }
 
  public void updateView() {
    ((RowHeaderColumnRenderer) this.rowHeaderColumnTable.getColumnModel().getColumn(0).getCellRenderer()).emptyCache();
    this.table.updateUI();
    this.rowHeaderColumnTable.updateUI();
  }

  
  private List<Integer> loadColumnSettings() { //SimSearchTableModel tableModel) {
    
    SimSearchTableModel tableModel = this.getModel();
    SimSearch.SimSet simSet = tableModel.getSimSet();
    
    this.frozenColumns = new ArrayList<>();
    this.invisibleColumns = new HashSet<>();

    List<String> availableColumns = new ArrayList<>(tableModel.getColumnNames());
    availableColumns.remove(0); // Status
    
    List<String> frozenColumns = new ArrayList<>(); //availableColumns.stream().collect(Collectors.toList());
    if(this.viewSettings.frozenColumns.containsKey(simSet.getType())) {
    	frozenColumns = new ArrayList<>(this.viewSettings.frozenColumns.get(simSet.getType()));  
    	frozenColumns.retainAll(availableColumns);
    }
    
    Set<String> invisibleColumns = new HashSet<>();
    if(this.viewSettings.invisibleColumns.containsKey(simSet.getType())) {
    	invisibleColumns = new HashSet<>(this.viewSettings.invisibleColumns.get(simSet.getType()));
    	invisibleColumns.retainAll(availableColumns);
    }
    
    Set<String> visibleNotFrozenColumns = new HashSet<>(availableColumns);
    visibleNotFrozenColumns.removeAll(invisibleColumns);
    visibleNotFrozenColumns.removeAll(frozenColumns);
    
    List<String> columnOrder = new ArrayList<>();
    if(this.viewSettings.columnOrder.containsKey(simSet.getType())) {
    	columnOrder = new ArrayList<>(this.viewSettings.columnOrder.get(simSet.getType()));
    	columnOrder.retainAll(visibleNotFrozenColumns);
    }
    
    
    for(String columnName: frozenColumns) this.frozenColumns.add(availableColumns.indexOf(columnName)+1);
    for(String columnName: invisibleColumns) this.invisibleColumns.add(availableColumns.indexOf(columnName)+1);
    
    List<Integer> columnSorting = new ArrayList<>();
    for(String columnName: columnOrder) columnSorting.add(availableColumns.indexOf(columnName)+1);
    
    for(int column=1; column<tableModel.getColumnCount(); ++column) 
      if(!this.invisibleColumns.contains(column) && 
          !this.frozenColumns.contains(column) && !columnSorting.contains(column)) columnSorting.add(column); 
    
    return columnSorting;
  }
  
  private void applyColumnSettingsToView() {
    
    SimSearchTableModel tableModel = this.getModel();
    SimSearch.SimSet simSet = tableModel.getSimSet();
    
    Map<Integer, Integer> columnWidths = this.viewSettings.columnWidths.get(simSet);
    
    Arrays.asList(this.rowHeaderColumnTable, this.table).forEach(table -> {
      for(int column=(table==this.table?0:1); column<table.getColumnCount(); ++column) {
        int modelIndex = table.convertColumnIndexToModel(column);
        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        int width = ((columnWidths!=null && columnWidths.containsKey(modelIndex) )
        		? columnWidths.get(modelIndex)
        				:table.getPreferredColumnWidth(column));
          
        if(table==this.rowHeaderColumnTable) {

        	
        	Dimension dim = new Dimension(this.rowHeaderColumnTable.getColumnModel().getColumn(0).getWidth()+width, this.rowHeaderColumnTable.getHeight() );
        	SimSearchTable.this.rowHeaderColumnTable.setSize(dim);
            this.getRowHeader().setPreferredSize(new Dimension(dim.width, this.rowHeaderColumnTable.getMaximumSize().height));
            tableColumn.setPreferredWidth(width);
        } else {
        	tableColumn.setPreferredWidth(width); //           getColumnModel().getColumn(column).sizeWidthToFit();
        }
      }
    });
  }
  
  private void applyRowSettingsToView() {
	  SimSearchTableModel tableModel = this.getModel();
	  SimSearch.SimSet simSet = tableModel.getSimSet();
	  Map<Integer,Integer> idToRowHeight = viewSettings.rowHeights.get(simSet.getType());
	  rowHeights = new int[tableModel.getRowCount()];
	  int nRows = tableModel.getRowCount();
	  for(int row=0; row<nRows; ++row) {
		  Integer rowHeight = (idToRowHeight==null?null:idToRowHeight.get(tableModel.getID(row)));
		  if(rowHeight!=null) rowHeights[row] = rowHeight;
		  else rowHeights[row] = getPreferredRowHeight(row);
	  }
	  
	  this.rowHeaderColumnTable.setRowHeights(rowHeights);
	  this.updateRowHeights();
  }
  
  private int getPreferredRowHeight(int row) {
	  int height = ViewSettings.ROW_HEIGHT_MIN;
	  for(SimSearchJTable table : Arrays.asList(this.table, this.rowHeaderColumnTable)) {
		  height = Math.max(height, table.getPreferredRowHeight(row));
	  }
	  return height;
  }
  
  private void updateRowHeights() {
	    EventQueue.invokeLater(new Runnable() {
	      @Override public void run() {
	    	  int n = table.getRowCount();
	    	  for(int viewIndex=0; viewIndex<n; ++viewIndex) {
	    		  int modelIndex = table.convertRowIndexToModel(viewIndex);
	    		  table.setRowHeight(viewIndex, rowHeights[modelIndex]);
	  	          rowHeaderColumnTable.setRowHeight(viewIndex, rowHeights[modelIndex]);
	    	  }
	        
	      }
	    });
	  }
  
  private void applyRowSortingToView() {
    SimSearchTableModel tableModel = this.getModel();
    SimSearch.SimSet simSet = tableModel.getSimSet();
    
    // applyRowSorting
    if(this.viewSettings.rowOrder.containsKey(simSet)) {
      this.getRowSorter().applySorting(this.viewSettings.rowOrder.get(simSet), this.viewSettings.sortKeys.get(simSet));
    } else {
      //No sorting predefined
      this.getRowSorter().setSortKeys(new ArrayList<>(Arrays.asList(new SortKey(0, SortOrder.DESCENDING))));
    }
  }
  
  public void applySettingsFromView() {
	SimSearchTableModel tableModel = this.getModel();
	SimSearch.SimSet simSet = tableModel.getSimSet();
	
    List<String> columnOrder = new ArrayList<>();
    for(int column=0; column<this.table.getColumnCount(); ++column) columnOrder.add(this.table.getColumnName(column));
    this.viewSettings.columnOrder.put(simSet.getType(), columnOrder);
    List<String> frozenColumns = new ArrayList<>();
    for(int column=1; column<this.rowHeaderColumnTable.getColumnCount(); ++column) frozenColumns.add(this.rowHeaderColumnTable.getColumnName(column));
    this.viewSettings.frozenColumns.put(simSet.getType(), frozenColumns);
    
    Set<String> invisibleColumns = new HashSet<>();
    for(int column: this.invisibleColumns) invisibleColumns.add(tableModel.getColumnName(column));
    this.viewSettings.invisibleColumns.put(simSet.getType(), invisibleColumns);
    
    this.viewSettings.rowOrder.put(simSet, this.getRowSorter().getIdOrder());
    this.viewSettings.sortKeys.put(simSet, new ArrayList<>(this.getRowSorter().getSortKeys()));
    
    Map<Integer, Integer> columnWidths = new HashMap<>(); 
    for(JTable table: Arrays.asList(this.table, this.rowHeaderColumnTable)) {
      for(int column=(table==this.table?0:1); column<table.getColumnCount(); ++column) {
        columnWidths.put(table.convertColumnIndexToModel(column), table.getColumnModel().getColumn(column).getPreferredWidth());
      }
    }
    this.viewSettings.columnWidths.put(simSet, columnWidths);
    Map<Integer, Integer> rowHeights = this.viewSettings.rowHeights.get(simSet.getType());
    if(rowHeights==null) rowHeights = new HashMap<>();
    for(int row=0; row<tableModel.getRowCount(); ++row) rowHeights.put(tableModel.getID(row), this.rowHeights[row]);
    this.viewSettings.rowHeights.put(simSet.getType(), rowHeights);
  }
  
  private void addColumns() {
    List<Integer> columnSorting = this.loadColumnSettings();
    
    this.table.removeColumns();
    for(Integer column: columnSorting) this.table.addColumn(new TableColumn(column));
    
    this.rowHeaderColumnTable.removeColumns();
    this.rowHeaderColumnTable.addColumn(new TableColumn(0));
    for(Integer column: frozenColumns) this.rowHeaderColumnTable.addColumn(new TableColumn(column));
  }
  
  public void loadData(SimSearchTableModel tableModel) {
    List<Integer> selectedIds = new ArrayList<>();
    if(this.getModel()!=null) {
      this.applySettingsFromView();
      if(tableModel!=null && tableModel.getSimSet()==this.getModel().getSimSet()) selectedIds = this.table.getSelectedIds();
    }
    
    if (tableModel!=null) {
      this.table.setModel(tableModel);
      this.rowHeaderColumnTable.setModel(tableModel);
      
      
      this.addColumns();
      Arrays.asList(this.table, this.rowHeaderColumnTable).forEach(table -> {
        table.initColumnRenderers();
        table.updateUI();
      });
      
      this.resetRowSorter(tableModel);
  
      JViewport jv = new JViewport();
      jv.setView(this.rowHeaderColumnTable);
      int width = this.rowHeaderColumnTable.getSize().width;
      if(width==0) width = this.rowHeaderColumnTable.getMaximumSize().width;
      jv.setPreferredSize(new Dimension(width, this.rowHeaderColumnTable.getMaximumSize().height));
      this.setRowHeader(jv);
  
      this.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, this.rowHeaderColumnTable
          .getTableHeader());
  
      this.table.applyIdSelection(selectedIds);
      this.setBorderTitle(tableModel.getSimSet().getType());
    } 
  }
  
  
  private void resetRowSorter(SimSearchTableModel tableModel) {
	  SimSearchRowSorter rowSorter = new SimSearchRowSorter(tableModel);
      this.updateVisibleSorterColumns(rowSorter);

      table.setRowSorter(rowSorter);
      this.rowHeaderColumnTable.setRowSorter(rowSorter);
      
      this.applyRowSettingsToView();
      this.applyColumnSettingsToView();
      
      rowSorter.setInactiveRowFilterEnabled(this.isInactiveRowFilterEnabled());
      if(this.filterTextBox.getText()!=null && !this.filterTextBox.getText().isEmpty()) this.applyTextFilterToRowSorter();
      
      this.applyRowSortingToView();
      
      this.addSelectionAndRowHeightUpdateOnSortFix(rowSorter);

  }
  
  public void setBorderTitle(String text) {
    if(text==null || text.isEmpty()) text = "Results";
    if(this.getBorder()==null || !(this.getBorder() instanceof TitledBorder)) {
      this.setBorder(BorderFactory.createTitledBorder(text));
    } else {
      ((TitledBorder) this.getBorder()).setTitle(text);
    }
  }
  
  public void updateRowHeightMenu(JMenu menu, boolean addShortCuts) {
    if(this.getModel()==null) {
      menu.setEnabled(false);
    } else {
      menu.setEnabled(true);
    }
  }
  
  private void setBorderTitle(SimSearch.SimSet.Type simSetType) {
    switch(simSetType) {
      case STATION:
        setBorderTitle("Similar stations:");
        break;
      case PRODUCT:
        setBorderTitle("Similar products:");
      case LOT:
        setBorderTitle("Similar lots:");
        break;
      case DELIVERY:
        setBorderTitle("Similar deliveries:");
        break;
      default:
        setBorderTitle((String) null);
    }
  }
  
  public void clear() {
	this.table.setRowSorter(null);
	this.rowHeaderColumnTable.setRowSorter(null);
    this.table.setModel(null);
    this.rowHeaderColumnTable.setModel(this.table.getModel());
    this.setBorderTitle("");
    this.inactiveRowFilterSwitch.setEnabled(false);
    this.filterTextBox.setEnabled(false);
    if(useRegexFilterCheckBox!=null) this.useRegexFilterCheckBox.setEnabled(false);
    this.ignoreAllPairsInSimSetButton.setEnabled(false);
    this.ignoreSimSetButton.setEnabled(false);
  }
  
  // this class is needed because the default JCheckBoxMenuItem closes the popup menu then clicked
  private class ColumnVisibilityCheckBoxMenuItem extends JCheckBoxMenuItem {
/**
	 * 
	 */
	private static final long serialVersionUID = -5531025505218227306L;
    
    private ColumnVisibilityCheckBoxMenuItem(String text, int column) {
      super(text);

      super.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if(SwingUtilities.isLeftMouseButton(e)) {
            ColumnVisibilityCheckBoxMenuItem.this.setSelected(!ColumnVisibilityCheckBoxMenuItem.this.isSelected());
            SimSearchTable.this.setColumnVisible(column, ColumnVisibilityCheckBoxMenuItem.this.isSelected());
          }
        }
      });
    }
    

    
    public void addMouseListener(MouseListener listener) {
      Class<?> declaringClass = null;
      try {
        declaringClass = listener.getClass().getDeclaringClass();
      } catch(Exception err) {}
      
      // the MenuItemUI is not allowed to be added as mouseListener because it is responsible for closing the popup
      if(declaringClass==null || !MenuItemUI.class.isAssignableFrom(declaringClass)) {
        super.addMouseListener(listener);
      }
    }
  }
  
  class Repainter implements PropertyChangeListener {
	  public void propertyChange(PropertyChangeEvent pce) {
		  repaintDropLocation(pce.getOldValue());
		  repaintDropLocation(pce.getNewValue());
	  }
  }
  
  private void repaintDropLocation(Object object) {
    this.table.repaint();
    this.rowHeaderColumnTable.repaint();
  }
  
  public void processUserChangeRowHeightRequest(int rowHeightDelta) {
    if(this.getModel()==null) return;
    if(rowHeightDelta>0) this.table.setRowHeight(this.table.getRowHeight() + rowHeightDelta);
    else if (rowHeightDelta<0) this.table.setRowHeight(Math.max(this.table.getRowHeight() + rowHeightDelta, ViewSettings.ROW_HEIGHT_MIN));
    if(this.table.getRowHeight()!=this.rowHeaderColumnTable.getRowHeight()) this.rowHeaderColumnTable.setRowHeight(this.table.getRowHeight());
  }
 
  
  private static class CopyAdapter implements ActionListener {
	  
	  private JTable rowHeader;
	  private JTable table;
	  
	  private CopyAdapter(JTable table, JTable rowHeader) {
		  this.rowHeader = rowHeader;
		  this.table = table;
	      KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK,false);
	      
	      table.registerKeyboardAction(this,"Copy",copy,JComponent.WHEN_FOCUSED);
	      rowHeader.registerKeyboardAction(this,"Copy",copy,JComponent.WHEN_FOCUSED);
	  }

	@Override
	public void actionPerformed(ActionEvent e) {
		
		JTable tableToCopyFrom = (JTable) e.getSource();
		
		int[] rowsToCopyFrom = tableToCopyFrom.getSelectedRows();
        int colToCopyFrom = tableToCopyFrom.getSelectedColumn();
        if(rowsToCopyFrom.length>0) this.copyData(tableToCopyFrom, rowsToCopyFrom, colToCopyFrom);
        
	}
	
	public void copyData(JTable tableToCopyFrom, int[] rowIndices, int columnIndex ) {
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		List<String> rowTextList = new ArrayList<>();
        for(int row : rowIndices) {
        	List<String> cellTextList = new ArrayList<>();
        	if(tableToCopyFrom==rowHeader && columnIndex==0) {
        		// full row copy
        		for(JTable table : Arrays.asList(rowHeader, table)) {
        			for(int col=(table==rowHeader?1:0); col<table.getColumnCount(); ++col) {
        				Object value = table.getValueAt(row,col);
        				cellTextList.add((value==null?"":value.toString()));
        			}
        		}
        	} else {
        		// cell copy
        		Object value = tableToCopyFrom.getValueAt(row,columnIndex);
        		cellTextList.add((value==null?"":value.toString()));
        	}
        	rowTextList.add(String.join("\t", cellTextList));
        }
        
        StringSelection strSel = new StringSelection(String.join("\n", rowTextList));
        cb.setContents(strSel, strSel);
	}
	  
  }
  
public static class ViewSettings {
    
    public static final int ROW_HEIGHT_MIN = 20;
    private static final int CELL_MARGIN_Y_DEFAULT = 2;
    private static final int CELL_MARGIN_X_DEFAULT = 5;
  
    
    private Map<SimSearch.SimSet.Type, List<String>> columnOrder;
    private Map<SimSearch.SimSet.Type, List<String>> frozenColumns;
    private Map<SimSearch.SimSet.Type, Set<String>> invisibleColumns;
    private Map<SimSearch.SimSet, Map<Integer,Integer>> columnWidths;
    private Map<SimSearch.SimSet.Type, Map<Integer, Integer>> rowHeights;
    private Map<SimSearch.SimSet, List<Integer>> rowOrder;
    private Map<SimSearch.SimSet, List<SortKey>> sortKeys;
    private Font font;
    protected int cellMarginY;
    protected int cellMarginX;
    
    public ViewSettings() {
      this.columnOrder = new HashMap<>();
      this.frozenColumns = new HashMap<>();
      this.invisibleColumns = this.createDefaultInvisibleColumnsMap();
      this.columnWidths = new HashMap<>();
      this.rowHeights = new HashMap<>();
      this.rowOrder = new HashMap<>();
      this.sortKeys = new HashMap<>();
      this.cellMarginX = CELL_MARGIN_X_DEFAULT;
      this.cellMarginY = CELL_MARGIN_Y_DEFAULT;
      this.font = UIManager.getFont("Table.font");
    }
    
    private Map<SimSearch.SimSet.Type, Set<String>> createDefaultInvisibleColumnsMap() {
    	Map<SimSearch.SimSet.Type, Set<String>> result = new HashMap<>();
    	result.put(SimSearch.SimSet.Type.STATION, new HashSet<>(Arrays.asList("ID","Produktkatalog", "DatumEnde", "DatumHoehepunkt", "Erregernachweis", "Latitude", "EMail", "Betriebsnummer", "Longitude", "Code", "AlterMax", "ImportSources", "Webseite", "Ansprechpartner", "AnzahlFaelle", "DatumBeginn", "Serial", "CasePriority", "Fax", "Telefon", "VATnumber", "AlterMin")));
    	result.put(SimSearch.SimSet.Type.PRODUCT, new HashSet<>(Arrays.asList("ID", "IntendedUse", "Serial", "Matrices", "Prozessierung", "Code", "ImportSources")));
    	result.put(SimSearch.SimSet.Type.LOT, new HashSet<>(Arrays.asList("ID", "Serial", "pd_month", "OriginCountry", "pd_year", "MicrobioSample", "pd_day", "ImportSources")));
    	result.put(SimSearch.SimSet.Type.DELIVERY, new HashSet<>(Arrays.asList("ID", "Explanation_EndChain", "Serial", "EndChain", "Unitmenge", "Contact_Questions_Remarks", "Further_Traceback", "ImportSources", "UnitEinheit")));
    	return result;
    }
    
    protected Font getFont() { return this.font; }
  }
  

}
