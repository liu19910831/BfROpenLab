/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.gui.jmapviewer.OsmMercator;

import com.google.common.math.DoubleMath;
import com.google.common.primitives.Doubles;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;

public class LocationOsmCanvas extends OsmCanvas<LocationNode> {

	private static final long serialVersionUID = 1L;

	public LocationOsmCanvas(boolean allowEdges, Naming naming) {
		this(new ArrayList<LocationNode>(),
				new ArrayList<Edge<LocationNode>>(), new NodePropertySchema(),
				new EdgePropertySchema(), naming, allowEdges);
	}

	public LocationOsmCanvas(List<LocationNode> nodes,
			NodePropertySchema nodeSchema, Naming naming) {
		this(nodes, new ArrayList<Edge<LocationNode>>(), nodeSchema,
				new EdgePropertySchema(), naming, false);
	}

	public LocationOsmCanvas(List<LocationNode> nodes,
			List<Edge<LocationNode>> edges, NodePropertySchema nodeSchema,
			EdgePropertySchema edgeSchema, Naming naming) {
		this(nodes, edges, nodeSchema, edgeSchema, naming, true);
	}

	private LocationOsmCanvas(List<LocationNode> nodes,
			List<Edge<LocationNode>> edges, NodePropertySchema nodeSchema,
			EdgePropertySchema edgeSchema, Naming naming, boolean allowEdges) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);

		setPopupMenu(new CanvasPopupMenu(this, allowEdges, false, true));
		setOptionsPanel(new CanvasOptionsPanel(this, allowEdges, true, true));
		viewer.getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<>(getNodeSize(),
						new LinkedHashMap<LocationNode, Double>()));

		for (LocationNode node : this.nodes) {
			viewer.getGraphLayout().setLocation(
					node,
					new Point2D.Double(OsmMercator.LonToX(node.getCenter()
							.getX(), 0), OsmMercator.LatToY(node.getCenter()
							.getY(), 0)));
		}
	}

	@Override
	protected LocationNode createMetaNode(String id,
			Collection<LocationNode> nodes) {
		Map<String, Object> properties = new LinkedHashMap<>();

		for (LocationNode node : nodes) {
			CanvasUtils.addMapToMap(properties, nodeSchema,
					node.getProperties());
		}

		properties.put(nodeSchema.getId(), id);
		properties.put(metaNodeProperty, true);

		if (nodeSchema.getLatitude() != null) {
			properties.put(nodeSchema.getLatitude(),
					CanvasUtils.getMeanValue(nodes, nodeSchema.getLatitude()));
		}

		if (nodeSchema.getLongitude() != null) {
			properties.put(nodeSchema.getLongitude(),
					CanvasUtils.getMeanValue(nodes, nodeSchema.getLongitude()));
		}

		List<Double> xList = new ArrayList<Double>();
		List<Double> yList = new ArrayList<Double>();

		for (LocationNode node : nodes) {
			xList.add(node.getCenter().getX());
			yList.add(node.getCenter().getY());
		}

		double x = DoubleMath.mean(Doubles.toArray(xList));
		double y = DoubleMath.mean(Doubles.toArray(yList));
		LocationNode newNode = new LocationNode(id, properties,
				new Point2D.Double(x, y));

		viewer.getGraphLayout().setLocation(newNode, newNode.getCenter());

		return newNode;
	}
}