/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
 ******************************************************************************/
package de.bund.bfr.knime.openkrise.views.gisgraphview;

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.Canvas;
import de.bund.bfr.knime.gis.views.canvas.CanvasListener;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvas;

/**
 * <code>NodeDialog</code> for the "GisGraphView" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class GisGraphViewNodeDialog extends DataAwareNodeDialogPane implements
		ComponentListener, CanvasListener {

	private JPanel panel;
	private JSplitPane splitPane;
	private GraphCanvas graphCanvas;
	private LocationCanvas gisCanvas;

	private boolean resized;

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;

	private GisGraphViewSettings set;

	private JCheckBox exportAsSvgBox;

	/**
	 * New pane for configuring the GisGraphView node.
	 */
	protected GisGraphViewNodeDialog() {
		set = new GisGraphViewSettings();
		exportAsSvgBox = new JCheckBox("Export As Svg");

		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(UI.createWestPanel(UI.createHorizontalPanel(exportAsSvgBox)),
				BorderLayout.NORTH);
		panel.addComponentListener(this);

		addTab("Options", panel, false);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input)
			throws NotConfigurableException {
		shapeTable = (BufferedDataTable) input[0];
		nodeTable = (BufferedDataTable) input[1];
		edgeTable = (BufferedDataTable) input[2];

		set.loadSettings(settings);

		if (input[3] != null) {
			try {
				set.loadFromXml(KnimeUtils
						.tableToXml((BufferedDataTable) input[3]));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		exportAsSvgBox.setSelected(set.isExportAsSvg());
		updateSplitPane(false);
		resized = false;
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		set.getGraphSettings().setFromCanvas(graphCanvas, resized);
		set.getGisSettings().setFromCanvas(gisCanvas, resized);
		set.saveSettings(settings);
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if (SwingUtilities.getWindowAncestor(panel).isActive()) {
			resized = true;
		}
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void nodeSelectionChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setSelectedNodeIds(graphCanvas.getSelectedNodeIds());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setSelectedNodeIds(gisCanvas.getSelectedNodeIds());
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void edgeSelectionChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setSelectedEdgeIds(graphCanvas.getSelectedEdgeIds());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setSelectedEdgeIds(gisCanvas.getSelectedEdgeIds());
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void nodeHighlightingChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setNodeHighlightConditions(graphCanvas
					.getNodeHighlightConditions());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setNodeHighlightConditions(gisCanvas
					.getNodeHighlightConditions());
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void edgeHighlightingChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setEdgeHighlightConditions(graphCanvas
					.getEdgeHighlightConditions());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setEdgeHighlightConditions(gisCanvas
					.getEdgeHighlightConditions());
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void edgeJoinChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setJoinEdges(graphCanvas.isJoinEdges());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setJoinEdges(gisCanvas.isJoinEdges());
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void skipEdgelessChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setSkipEdgelessNodes(graphCanvas.isSkipEdgelessNodes());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setSkipEdgelessNodes(gisCanvas.isSkipEdgelessNodes());
			graphCanvas.addCanvasListener(this);
		}
	}

	private void updateSplitPane(boolean showWarning) {
		if (splitPane != null) {
			panel.remove(splitPane);
		}

		GisGraphViewCanvasCreator creator = new GisGraphViewCanvasCreator(
				shapeTable, nodeTable, edgeTable, set);

		graphCanvas = creator.createGraphCanvas();
		gisCanvas = creator.createGisCanvas();

		if (graphCanvas != null && gisCanvas != null) {
			graphCanvas.addCanvasListener(this);
			gisCanvas.addCanvasListener(this);
		} else {
			graphCanvas = new GraphCanvas(false);
			graphCanvas.setCanvasSize(set.getGraphSettings().getCanvasSize());
			gisCanvas = new LocationCanvas(true);
			gisCanvas.setCanvasSize(set.getGisSettings().getCanvasSize());

			if (showWarning) {
				JOptionPane.showMessageDialog(panel,
						"Error reading nodes and edges", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}

		graphCanvas.addComponentListener(this);
		gisCanvas.addComponentListener(this);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphCanvas,
				gisCanvas);
		splitPane.setResizeWeight(0.5);
		panel.add(splitPane, BorderLayout.CENTER);
		panel.revalidate();
	}
}
