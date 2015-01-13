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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;

import de.bund.bfr.knime.gis.geocode.GeocodingNodeModel;
import de.bund.bfr.knime.gis.views.canvas.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.openkrise.MyDelivery;
import de.bund.bfr.knime.openkrise.TracingColumns;
import de.bund.bfr.knime.openkrise.TracingUtils;
import de.bund.bfr.knime.openkrise.views.canvas.TracingGisCanvas;
import de.bund.bfr.knime.openkrise.views.canvas.TracingGraphCanvas;

public class TracingViewCanvasCreator {

	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;
	private BufferedDataTable shapeTable;
	private HashMap<Integer, MyDelivery> deliveries;
	private TracingViewSettings set;

	private NodePropertySchema nodeSchema;
	private EdgePropertySchema edgeSchema;

	public TracingViewCanvasCreator(BufferedDataTable nodeTable,
			BufferedDataTable edgeTable, BufferedDataTable shapeTable,
			HashMap<Integer, MyDelivery> tracing, TracingViewSettings set) {
		this.nodeTable = nodeTable;
		this.edgeTable = edgeTable;
		this.shapeTable = shapeTable;
		this.deliveries = tracing;
		this.set = set;

		Map<String, Class<?>> nodeProperties = TracingUtils
				.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = TracingUtils
				.getTableColumns(edgeTable.getSpec());

		if (!nodeProperties.containsKey(TracingColumns.WEIGHT)) {
			nodeProperties.put(TracingColumns.WEIGHT, Double.class);
		}

		if (!nodeProperties.containsKey(TracingColumns.CROSS_CONTAMINATION)) {
			nodeProperties.put(TracingColumns.CROSS_CONTAMINATION,
					Boolean.class);
		}

		if (!nodeProperties.containsKey(TracingColumns.SCORE)) {
			nodeProperties.put(TracingColumns.SCORE, Double.class);
		}

		if (!nodeProperties.containsKey(TracingColumns.OBSERVED)) {
			nodeProperties.put(TracingColumns.OBSERVED, Boolean.class);
		}

		if (!nodeProperties.containsKey(TracingColumns.BACKWARD)) {
			nodeProperties.put(TracingColumns.BACKWARD, Boolean.class);
		}

		if (!nodeProperties.containsKey(TracingColumns.FORWARD)) {
			nodeProperties.put(TracingColumns.FORWARD, Boolean.class);
		}

		if (!edgeProperties.containsKey(TracingColumns.WEIGHT)) {
			edgeProperties.put(TracingColumns.WEIGHT, Double.class);
		}

		if (!edgeProperties.containsKey(TracingColumns.CROSS_CONTAMINATION)) {
			edgeProperties.put(TracingColumns.CROSS_CONTAMINATION,
					Boolean.class);
		}

		if (!edgeProperties.containsKey(TracingColumns.OBSERVED)) {
			edgeProperties.put(TracingColumns.OBSERVED, Boolean.class);
		}

		if (!edgeProperties.containsKey(TracingColumns.SCORE)) {
			edgeProperties.put(TracingColumns.SCORE, Double.class);
		}

		if (!edgeProperties.containsKey(TracingColumns.BACKWARD)) {
			edgeProperties.put(TracingColumns.BACKWARD, Boolean.class);
		}

		if (!edgeProperties.containsKey(TracingColumns.FORWARD)) {
			edgeProperties.put(TracingColumns.FORWARD, Boolean.class);
		}

		nodeSchema = new NodePropertySchema(nodeProperties, TracingColumns.ID);
		edgeSchema = new EdgePropertySchema(edgeProperties, TracingColumns.ID,
				TracingColumns.FROM, TracingColumns.TO);
		nodeSchema.setLatitude(GeocodingNodeModel.LATITUDE_COLUMN);
		nodeSchema.setLongitude(GeocodingNodeModel.LONGITUDE_COLUMN);
	}

	public TracingGraphCanvas createGraphCanvas()
			throws InvalidSettingsException {
		Map<String, GraphNode> nodes = TracingUtils.readGraphNodes(nodeTable,
				nodeSchema.getMap());
		List<Edge<GraphNode>> edges = TracingUtils.readEdges(edgeTable,
				edgeSchema.getMap(), nodes);

		TracingGraphCanvas canvas = new TracingGraphCanvas(new ArrayList<>(
				nodes.values()), edges, nodeSchema, edgeSchema, deliveries);

		canvas.setPerformTracing(false);
		set.getGraphSettings().setToCanvas(canvas, nodeSchema.getMap(),
				edgeSchema.getMap(), false);
		canvas.setNodeWeights(set.getNodeWeights());
		canvas.setEdgeWeights(set.getEdgeWeights());
		canvas.setNodeCrossContaminations(set.getNodeCrossContaminations());
		canvas.setEdgeCrossContaminations(set.getEdgeCrossContaminations());
		canvas.setObservedNodes(set.getObservedNodes());
		canvas.setObservedEdges(set.getObservedEdges());
		canvas.setEnforceTemporalOrder(set.isEnforeTemporalOrder());
		canvas.setShowForward(set.isShowForward());
		canvas.setPerformTracing(true);
		canvas.setNodePositions(set.getGraphSettings().getNodePositions());

		return canvas;
	}

	public TracingGisCanvas createGisCanvas() throws InvalidSettingsException {
		Map<String, LocationNode> nodes = TracingUtils.readLocationNodes(
				nodeTable, nodeSchema.getMap());
		List<Edge<LocationNode>> edges = TracingUtils.readEdges(edgeTable,
				edgeSchema.getMap(), nodes);
		List<RegionNode> regions = TracingUtils.readRegionNodes(shapeTable);
		TracingGisCanvas canvas = new TracingGisCanvas(new ArrayList<>(
				nodes.values()), edges, nodeSchema, edgeSchema, regions,
				deliveries);

		canvas.setPerformTracing(false);
		set.getGraphSettings().setToCanvas(canvas, nodeSchema.getMap(),
				edgeSchema.getMap(), false);
		set.getGisSettings().setToCanvas(canvas);
		canvas.setNodeWeights(set.getNodeWeights());
		canvas.setEdgeWeights(set.getEdgeWeights());
		canvas.setNodeCrossContaminations(set.getNodeCrossContaminations());
		canvas.setEdgeCrossContaminations(set.getEdgeCrossContaminations());
		canvas.setObservedNodes(set.getObservedNodes());
		canvas.setObservedEdges(set.getObservedEdges());
		canvas.setEnforceTemporalOrder(set.isEnforeTemporalOrder());
		canvas.setShowForward(set.isShowForward());
		canvas.setPerformTracing(true);

		return canvas;
	}
}
