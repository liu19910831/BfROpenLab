/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightListDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SingleElementPropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeStrokeTransformer;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

/**
 * @author Christian Thoens
 */
public class GraphCanvas extends Canvas<GraphNode> {

	public static final String CIRCLE_LAYOUT = "Circle Layout";
	public static final String FR_LAYOUT = "FR Layout";
	public static final String FR_LAYOUT_2 = "FR Layout 2";
	public static final String ISOM_LAYOUT = "ISOM Layout";
	public static final String KK_LAYOUT = "KK Layout";
	public static final String SPRING_LAYOUT = "Spring Layout";
	public static final String SPRING_LAYOUT_2 = "Spring Layout 2";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_LAYOUT = FR_LAYOUT;
	private static final int DEFAULT_NODESIZE = 10;
	private static final String IS_META_NODE = "IsMetaNode";

	private String layoutType;
	private int nodeSize;

	private List<GraphNode> allNodes;
	private List<Edge<GraphNode>> allEdges;
	private Set<GraphNode> nodes;
	private Set<Edge<GraphNode>> edges;
	private Map<Edge<GraphNode>, Set<Edge<GraphNode>>> joinMap;

	private Map<String, Map<String, Point2D>> collapsedNodes;
	private Map<String, GraphNode> nodeSaveMap;
	private Map<String, Edge<GraphNode>> edgeSaveMap;

	private JComboBox<String> layoutBox;
	private JButton layoutButton;
	private JTextField nodeSizeField;
	private JButton nodeSizeButton;

	private String metaNodeProperty;

	public GraphCanvas() {
		this(new ArrayList<GraphNode>(), new ArrayList<Edge<GraphNode>>(),
				new LinkedHashMap<String, Class<?>>(),
				new LinkedHashMap<String, Class<?>>(), null, null, null, null);
	}

	public GraphCanvas(List<GraphNode> nodes, List<Edge<GraphNode>> edges,
			Map<String, Class<?>> nodeProperties,
			Map<String, Class<?>> edgeProperties, String nodeIdProperty,
			String edgeIdProperty, String edgeFromProperty,
			String edgeToProperty) {
		super(nodeProperties, edgeProperties, nodeIdProperty, edgeIdProperty,
				edgeFromProperty, edgeToProperty);
		setAllowEdges(true);
		this.allNodes = nodes;
		this.allEdges = edges;
		this.nodes = new LinkedHashSet<GraphNode>();
		this.edges = new LinkedHashSet<Edge<GraphNode>>();

		Map<String, GraphNode> nodesById = new LinkedHashMap<String, GraphNode>();

		for (GraphNode node : allNodes) {
			GraphNode newNode = new GraphNode(node.getId(),
					new LinkedHashMap<String, Object>(node.getProperties()),
					node.getRegion());

			nodesById.put(node.getId(), newNode);
			this.nodes.add(newNode);
		}

		for (Edge<GraphNode> edge : allEdges) {
			this.edges.add(new Edge<GraphNode>(edge.getId(),
					new LinkedHashMap<String, Object>(edge.getProperties()),
					nodesById.get(edge.getFrom().getId()), nodesById.get(edge
							.getTo().getId())));
		}

		nodeSaveMap = CanvasUtilities.getElementsById(nodes);
		edgeSaveMap = CanvasUtilities.getElementsById(edges);

		layoutType = DEFAULT_LAYOUT;
		nodeSize = DEFAULT_NODESIZE;
		joinMap = new LinkedHashMap<Edge<GraphNode>, Set<Edge<GraphNode>>>();
		collapsedNodes = new LinkedHashMap<String, Map<String, Point2D>>();
		metaNodeProperty = CanvasUtilities.createNewProperty(IS_META_NODE,
				getNodeProperties());
		getNodeProperties().put(metaNodeProperty, Boolean.class);

		layoutBox = new JComboBox<String>(new String[] { CIRCLE_LAYOUT,
				FR_LAYOUT, FR_LAYOUT_2, ISOM_LAYOUT, KK_LAYOUT, SPRING_LAYOUT,
				SPRING_LAYOUT_2 });
		layoutBox.setSelectedItem(layoutType);
		layoutButton = new JButton("Apply");
		layoutButton.addActionListener(this);
		addOptionsItem("Layout", layoutBox, layoutButton);
		nodeSizeField = new JTextField("" + nodeSize, 5);
		nodeSizeButton = new JButton("Apply");
		nodeSizeButton.addActionListener(this);
		addOptionsItem("Node Size", nodeSizeField, nodeSizeButton);

		getViewer().getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<GraphNode>(nodeSize,
						new LinkedHashMap<GraphNode, Double>()));
		applyLayout();
	}

	public Set<GraphNode> getNodes() {
		return nodes;
	}

	public Set<Edge<GraphNode>> getEdges() {
		return edges;
	}

	public Map<String, GraphNode> getNodeSaveMap() {
		return nodeSaveMap;
	}

	public Map<String, Edge<GraphNode>> getEdgeSaveMap() {
		return edgeSaveMap;
	}

	public Map<Edge<GraphNode>, Set<Edge<GraphNode>>> getJoinMap() {
		return joinMap;
	}

	public Map<String, Point2D> getNodePositions() {
		return getNodePositions(nodes);
	}

	public void setNodePositions(Map<String, Point2D> nodePositions) {
		if (nodePositions.isEmpty()) {
			return;
		}

		int n = 0;

		for (GraphNode node : nodes) {
			if (nodePositions.get(node.getId()) == null) {
				n++;
			}
		}

		Layout<GraphNode, Edge<GraphNode>> layout = new StaticLayout<GraphNode, Edge<GraphNode>>(
				getViewer().getGraphLayout().getGraph());
		Point2D upperLeft = toGraphCoordinates(0, 0);
		Point2D upperRight = toGraphCoordinates(
				getViewer().getPreferredSize().width, 0);
		double x1 = upperLeft.getX();
		double x2 = upperRight.getX();
		double y = upperLeft.getY();
		int i = 0;

		for (GraphNode node : nodes) {
			Point2D pos = nodePositions.get(node.getId());

			if (pos != null) {
				layout.setLocation(node, pos);
			} else {
				double x = x1 + (double) i / (double) n * (x2 - x1);

				layout.setLocation(node, new Point2D.Double(x, y));
				i++;
			}
		}

		layout.setSize(getViewer().getSize());
		getViewer().setGraphLayout(layout);
	}

	public String getLayoutType() {
		return layoutType;
	}

	public void setLayoutType(String layoutType) {
		this.layoutType = layoutType;
		applyLayout();
	}

	public Map<String, Map<String, Point2D>> getCollapsedNodes() {
		return collapsedNodes;
	}

	public void setCollapsedNodes(
			Map<String, Map<String, Point2D>> collapsedNodes) {
		this.collapsedNodes = collapsedNodes;
		applyNodeCollapse();
	}

	public int getNodeSize() {
		return nodeSize;
	}

	public void setNodeSize(int nodeSize) {
		this.nodeSize = nodeSize;
		nodeSizeField.setText(nodeSize + "");
		applyHighlights();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		if (e.getSource() == layoutButton) {
			layoutType = (String) layoutBox.getSelectedItem();
			applyLayout();
		} else if (e.getSource() == nodeSizeButton) {
			try {
				nodeSize = Integer.parseInt(nodeSizeField.getText());
				applyHighlights();
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this,
						"Node Size must be Integer Value", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	protected void resetLayout() {
		setTransform(1.0, 1.0, 0.0, 0.0);
	}

	@Override
	protected HighlightListDialog openNodeHighlightDialog() {
		return new HighlightListDialog(this, getNodeProperties(), true, true,
				true, getNodeHighlightConditions(), null);
	}

	@Override
	protected HighlightListDialog openEdgeHighlightDialog() {
		return new HighlightListDialog(this, getEdgeProperties(), true, true,
				true, getEdgeHighlightConditions(), null);
	}

	@Override
	protected boolean applyHighlights() {
		Set<String> nodeIdsBefore = CanvasUtilities
				.getElementIds(getVisibleNodes());
		Set<String> edgeIdsBefore = CanvasUtilities
				.getElementIds(getVisibleEdges());

		CanvasUtilities.applyNodeHighlights(getViewer(), nodes,
				getNodeHighlightConditions(), nodeSize, false);
		CanvasUtilities.applyEdgeHighlights(getViewer(), edges,
				getEdgeHighlightConditions());
		CanvasUtilities.applyEdgelessNodes(getViewer(), isSkipEdgelessNodes());

		Set<String> nodeIdsAfter = CanvasUtilities
				.getElementIds(getVisibleNodes());
		Set<String> edgeIdsAfter = CanvasUtilities
				.getElementIds(getVisibleEdges());

		return !nodeIdsBefore.equals(nodeIdsAfter)
				|| !edgeIdsBefore.equals(edgeIdsAfter);
	}

	@Override
	protected void applyTransform() {
	}

	@Override
	protected void collapseToNode() {
		Set<String> selectedIds = getSelectedNodeIds();

		for (String id : collapsedNodes.keySet()) {
			if (selectedIds.contains(id)) {
				JOptionPane.showMessageDialog(this,
						"Some of the selected nodes are already collapsed",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		Class<?> type = getNodeProperties().get(getNodeIdProperty());
		String newId = null;

		while (true) {
			newId = (String) JOptionPane.showInputDialog(this,
					"Specify ID for Meta Node", "Node ID",
					JOptionPane.QUESTION_MESSAGE, null, null, "");

			boolean isInteger = true;

			try {
				Integer.parseInt(newId);
			} catch (NumberFormatException e) {
				isInteger = false;
			}

			if (newId == null) {
				return;
			} else if (type == Integer.class && !isInteger) {
				JOptionPane.showMessageDialog(this,
						"ID must be of same type as ID column in input table\n"
								+ "Please enter Integer value", "Error",
						JOptionPane.ERROR_MESSAGE);
			} else if (nodeSaveMap.containsKey(newId)) {
				JOptionPane.showMessageDialog(this,
						"ID already exists, please specify different ID",
						"Error", JOptionPane.ERROR_MESSAGE);
			} else {
				break;
			}
		}

		Map<String, Point2D> absPos = getNodePositions(CanvasUtilities
				.getElementsById(getViewer().getGraphLayout().getGraph()
						.getVertices(), selectedIds));
		Map<String, Point2D> relPos = new LinkedHashMap<String, Point2D>();
		Point2D center = CanvasUtilities.getCenter(absPos.values());

		for (String id : absPos.keySet()) {
			relPos.put(id,
					CanvasUtilities.substractPoints(absPos.get(id), center));
		}

		collapsedNodes.put(newId, relPos);
		applyNodeCollapse();
		setSelectedNodeIds(new LinkedHashSet<String>(Arrays.asList(newId)));
	}

	@Override
	protected void expandFromNode() {
		Set<String> selectedIds = getSelectedNodeIds();

		for (String id : selectedIds) {
			if (!collapsedNodes.keySet().contains(id)) {
				JOptionPane.showMessageDialog(this,
						"Some of the selected nodes are not collapsed",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		Set<String> newIds = new LinkedHashSet<String>();

		for (String id : selectedIds) {
			Map<String, Point2D> removed = collapsedNodes.remove(id);
			Point2D center = getViewer().getGraphLayout().transform(
					nodeSaveMap.get(id));

			newIds.addAll(removed.keySet());

			for (String newId : removed.keySet()) {
				getViewer().getGraphLayout().setLocation(
						nodeSaveMap.get(newId),
						CanvasUtilities.addPoints(removed.get(newId), center));
			}
		}

		applyNodeCollapse();
		setSelectedNodeIds(newIds);
	}

	@Override
	protected GraphMouse<GraphNode, Edge<GraphNode>> createMouseModel() {
		return new GraphMouse<GraphNode, Edge<GraphNode>>(
				new PickingGraphMousePlugin<GraphNode, Edge<GraphNode>>() {

					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getButton() == MouseEvent.BUTTON1
								&& e.getClickCount() == 2) {
							GraphNode node = getViewer().getPickSupport()
									.getVertex(getViewer().getGraphLayout(),
											e.getX(), e.getY());
							Edge<GraphNode> edge = getViewer().getPickSupport()
									.getEdge(getViewer().getGraphLayout(),
											e.getX(), e.getY());

							if (node != null) {
								SingleElementPropertiesDialog dialog = new SingleElementPropertiesDialog(
										e.getComponent(), node,
										getNodeProperties());

								dialog.setVisible(true);
							} else if (edge != null) {
								SingleElementPropertiesDialog dialog = new SingleElementPropertiesDialog(
										e.getComponent(), edge,
										getEdgeProperties());

								dialog.setVisible(true);
							}
						}
					}
				});
	}

	protected void applyNodeCollapse() {
		Set<String> selectedNodeIds = getSelectedNodeIds();
		Set<String> selectedEdgeIds = getSelectedEdgeIds();

		nodes = new LinkedHashSet<GraphNode>();
		edges = new LinkedHashSet<Edge<GraphNode>>();

		Map<String, String> collapseTo = new LinkedHashMap<String, String>();

		for (String to : collapsedNodes.keySet()) {
			for (String from : collapsedNodes.get(to).keySet()) {
				collapseTo.put(from, to);
			}
		}

		Map<String, GraphNode> nodesById = new LinkedHashMap<String, GraphNode>();

		for (GraphNode node : allNodes) {
			if (!collapseTo.keySet().contains(node.getId())) {
				GraphNode newNode = nodeSaveMap.get(node.getId());

				if (newNode == null) {
					newNode = new GraphNode(node.getId(),
							new LinkedHashMap<String, Object>(
									node.getProperties()), node.getRegion());
					getViewer().getGraphLayout().setLocation(newNode,
							getViewer().getGraphLayout().transform(node));
				}

				nodes.add(newNode);
				nodesById.put(node.getId(), newNode);
			}
		}

		Set<GraphNode> metaNodes = new LinkedHashSet<GraphNode>();

		for (String newId : collapsedNodes.keySet()) {
			GraphNode newNode = nodeSaveMap.get(newId);

			if (newNode == null) {
				Set<GraphNode> nodes = CanvasUtilities.getElementsById(
						nodeSaveMap, collapsedNodes.get(newId).keySet());
				Point2D pos = CanvasUtilities.getCenter(getNodePositions(nodes)
						.values());

				newNode = combineNodes(newId, nodes);
				getViewer().getGraphLayout().setLocation(newNode, pos);
			}

			nodes.add(newNode);
			nodesById.put(newNode.getId(), newNode);
			metaNodes.add(newNode);
		}

		for (Edge<GraphNode> edge : allEdges) {
			GraphNode from = nodesById.get(edge.getFrom().getId());
			GraphNode to = nodesById.get(edge.getTo().getId());

			if (from == null) {
				from = nodesById.get(collapseTo.get(edge.getFrom().getId()));
			}

			if (to == null) {
				to = nodesById.get(collapseTo.get(edge.getTo().getId()));
			}

			Edge<GraphNode> newEdge = edgeSaveMap.get(edge.getId());

			if (newEdge == null) {
				newEdge = new Edge<GraphNode>(
						edge.getId(),
						new LinkedHashMap<String, Object>(edge.getProperties()),
						from, to);
			} else if (!newEdge.getFrom().equals(from)
					|| !newEdge.getTo().equals(to)) {
				newEdge = new Edge<GraphNode>(newEdge.getId(),
						newEdge.getProperties(), from, to);
			}

			if (newEdge.getFrom().equals(newEdge.getTo())) {
				if (!metaNodes.contains(newEdge.getFrom())) {
					edges.add(newEdge);
				}
			} else {
				edges.add(newEdge);
			}
		}

		if (isJoinEdges()) {
			joinMap = CanvasUtilities.joinEdges(edges, getEdgeProperties(),
					getEdgeIdProperty(), getEdgeFromProperty(),
					getEdgeToProperty(),
					CanvasUtilities.getElementIds(allEdges));
			edges = joinMap.keySet();
		} else {
			joinMap = new LinkedHashMap<Edge<GraphNode>, Set<Edge<GraphNode>>>();
		}

		nodeSaveMap.putAll(CanvasUtilities.getElementsById(nodes));
		edgeSaveMap.putAll(CanvasUtilities.getElementsById(edges));
		getViewer().getGraphLayout().setGraph(createGraph());
		getViewer().getRenderContext().setVertexStrokeTransformer(
				new NodeStrokeTransformer<GraphNode>(metaNodes));
		getViewer().getPickedVertexState().clear();
		applyHighlights();
		setSelectedNodeIds(selectedNodeIds);
		setSelectedEdgeIds(selectedEdgeIds);
	}

	@Override
	protected void applyEdgeJoin() {
		applyNodeCollapse();
	}

	private void applyLayout() {
		Graph<GraphNode, Edge<GraphNode>> graph = createGraph();
		Layout<GraphNode, Edge<GraphNode>> layout = null;

		if (layoutType.equals(CIRCLE_LAYOUT)) {
			layout = new CircleLayout<GraphNode, Edge<GraphNode>>(graph);
		} else if (layoutType.equals(FR_LAYOUT)) {
			layout = new FRLayout<GraphNode, Edge<GraphNode>>(graph);
		} else if (layoutType.equals(FR_LAYOUT_2)) {
			layout = new FRLayout2<GraphNode, Edge<GraphNode>>(graph);
		} else if (layoutType.equals(ISOM_LAYOUT)) {
			layout = new ISOMLayout<GraphNode, Edge<GraphNode>>(graph);
		} else if (layoutType.equals(KK_LAYOUT)) {
			layout = new KKLayout<GraphNode, Edge<GraphNode>>(graph);
		} else if (layoutType.equals(SPRING_LAYOUT)) {
			layout = new SpringLayout<GraphNode, Edge<GraphNode>>(graph);
		} else if (layoutType.equals(SPRING_LAYOUT_2)) {
			layout = new SpringLayout2<GraphNode, Edge<GraphNode>>(graph);
		}

		layout.setSize(getViewer().getSize());
		setTransform(1.0, 1.0, 0.0, 0.0);
		getViewer().setGraphLayout(layout);
	}

	private Map<String, Point2D> getNodePositions(Collection<GraphNode> nodes) {
		Map<String, Point2D> map = new LinkedHashMap<String, Point2D>();
		Layout<GraphNode, Edge<GraphNode>> layout = getViewer()
				.getGraphLayout();

		for (GraphNode node : nodes) {
			Point2D pos = layout.transform(node);

			if (pos != null) {
				map.put(node.getId(), pos);
			}
		}

		return map;
	}

	private GraphNode combineNodes(String id, Collection<GraphNode> nodes) {
		Map<String, Object> properties = new LinkedHashMap<String, Object>();

		for (GraphNode node : nodes) {
			CanvasUtilities.addMapToMap(properties, getNodeProperties(),
					node.getProperties());
		}

		if (getNodeIdProperty() != null) {
			Class<?> type = getNodeProperties().get(getNodeIdProperty());

			if (type == String.class) {
				properties.put(getNodeIdProperty(), id);
			} else if (type == Integer.class) {
				properties.put(getNodeIdProperty(), Integer.parseInt(id));
			}
		}

		properties.put(metaNodeProperty, true);

		return new GraphNode(id, properties, null);
	}

	private Graph<GraphNode, Edge<GraphNode>> createGraph() {
		Graph<GraphNode, Edge<GraphNode>> graph = new DirectedSparseMultigraph<GraphNode, Edge<GraphNode>>();

		for (GraphNode node : nodes) {
			graph.addVertex(node);
		}

		for (Edge<GraphNode> edge : edges) {
			graph.addEdge(edge, edge.getFrom(), edge.getTo());
		}

		return graph;
	}
}
