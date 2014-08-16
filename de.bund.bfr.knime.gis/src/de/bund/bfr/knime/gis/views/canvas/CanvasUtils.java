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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.knime.base.data.xml.SvgCell;
import org.knime.base.data.xml.SvgImageContent;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.transformer.EdgeArrowTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.EdgeDrawTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.EdgeStrokeTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.LabelTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeFillTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class CanvasUtils {

	private static final int TEXTURE_SIZE = 3;

	private CanvasUtils() {
	}

	public static Point2D addPoints(Point2D p1, Point2D p2) {
		return new Point2D.Double(p1.getX() + p2.getX(), p1.getY() + p2.getY());
	}

	public static Point2D substractPoints(Point2D p1, Point2D p2) {
		return new Point2D.Double(p1.getX() - p2.getX(), p1.getY() - p2.getY());
	}

	public static Point2D getCenter(Collection<Point2D> points) {
		double x = 0.0;
		double y = 0.0;

		for (Point2D p : points) {
			if (p == null) {
				return null;
			}

			x += p.getX();
			y += p.getY();
		}

		x /= points.size();
		y /= points.size();

		return new Point2D.Double(x, y);
	}

	public static String toRangeString(Point2D p) {
		NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

		return format.format(p.getX()) + " -> " + format.format(p.getY());
	}

	public static <V extends Node> Map<Edge<V>, Set<Edge<V>>> joinEdges(
			Collection<Edge<V>> edges, Map<String, Class<?>> properties,
			String idProperty, String fromProperty, String toProperty,
			Set<String> usedIds) {
		Map<V, Map<V, Set<Edge<V>>>> edgeMap = new LinkedHashMap<>();

		for (Edge<V> edge : edges) {
			V from = edge.getFrom();
			V to = edge.getTo();

			if (!edgeMap.containsKey(from)) {
				edgeMap.put(from, new LinkedHashMap<V, Set<Edge<V>>>());
			}

			if (!edgeMap.get(from).containsKey(to)) {
				edgeMap.get(from).put(to, new LinkedHashSet<Edge<V>>());
			}

			edgeMap.get(from).get(to).add(edge);
		}

		Map<Edge<V>, Set<Edge<V>>> joined = new LinkedHashMap<>();
		int index = 0;

		for (V from : edgeMap.keySet()) {
			for (V to : edgeMap.get(from).keySet()) {
				Map<String, Object> prop = new LinkedHashMap<>();

				for (Edge<V> edge : edgeMap.get(from).get(to)) {
					CanvasUtils.addMapToMap(prop, properties,
							edge.getProperties());
				}

				while (!usedIds.add(index + "")) {
					index++;
				}

				prop.put(idProperty, index + "");
				prop.put(fromProperty, from.getId());
				prop.put(toProperty, to.getId());
				joined.put(new Edge<>(index + "", prop, from, to),
						edgeMap.get(from).get(to));
			}
		}

		return joined;
	}

	public static void addMapToMap(Map<String, Object> map,
			Map<String, Class<?>> properties, Map<String, Object> addMap) {
		for (String property : properties.keySet()) {
			addObjectToMap(map, property, properties.get(property),
					addMap.get(property));
		}
	}

	public static void addObjectToMap(Map<String, Object> map, String property,
			Class<?> type, Object obj) {
		if (type == String.class) {
			String value = (String) obj;

			if (map.containsKey(property)) {
				if (map.get(property) == null
						|| !map.get(property).equals(value)) {
					map.put(property, null);
				}
			} else {
				map.put(property, value);
			}
		} else if (type == Integer.class) {
			Integer value = (Integer) obj;

			if (map.get(property) != null) {
				if (value != null) {
					map.put(property, (Integer) map.get(property) + value);
				}
			} else {
				map.put(property, value);
			}
		} else if (type == Double.class) {
			Double value = (Double) obj;

			if (map.get(property) != null) {
				if (value != null) {
					map.put(property, (Double) map.get(property) + value);
				}
			} else {
				map.put(property, value);
			}
		} else if (type == Boolean.class) {
			Boolean value = (Boolean) obj;

			if (map.containsKey(property)) {
				if (map.get(property) == null
						|| !map.get(property).equals(value)) {
					map.put(property, null);
				}
			} else {
				map.put(property, value);
			}
		}
	}

	public static <T extends Element> Set<T> getHighlightedElements(
			Collection<T> elements, List<HighlightCondition> highlightConditions) {
		Set<T> highlightedElements = new LinkedHashSet<>();

		for (HighlightCondition condition : highlightConditions) {
			Map<T, Double> highlighted = condition.getValues(elements);

			for (T element : highlighted.keySet()) {
				if (highlighted.get(element) > 0.0) {
					highlightedElements.add(element);
				}
			}
		}

		return highlightedElements;
	}

	public static Set<String> getElementIds(
			Collection<? extends Element> elements) {
		Set<String> ids = new LinkedHashSet<>();

		for (Element element : elements) {
			ids.add(element.getId());
		}

		return ids;
	}

	public static <T extends Element> Set<T> getElementsById(
			Collection<T> elements, Collection<String> ids) {
		Set<T> result = new LinkedHashSet<>();

		for (T element : elements) {
			if (ids.contains(element.getId())) {
				result.add(element);
			}
		}

		return result;
	}

	public static <T extends Element> Map<String, T> getElementsById(
			Collection<T> elements) {
		Map<String, T> result = new LinkedHashMap<>();

		for (T element : elements) {
			result.put(element.getId(), element);
		}

		return result;
	}

	public static <T extends Element> Set<T> getElementsById(
			Map<String, T> elements, Collection<String> ids) {
		Set<T> result = new LinkedHashSet<>();

		for (String id : ids) {
			if (elements.containsKey(id)) {
				result.add(elements.get(id));
			}
		}

		return result;
	}

	public static Double getMeanValue(Collection<? extends Element> elements,
			String property) {
		double sum = 0.0;
		int n = 0;

		for (Element element : elements) {
			Object o = element.getProperties().get(property);

			if (o instanceof Double) {
				sum += (Double) o;
				n++;
			}
		}

		if (n == 0) {
			return null;
		}

		return sum / n;
	}

	public static <V extends Node> void applyNodeHighlights(
			VisualizationViewer<V, Edge<V>> viewer,
			HighlightConditionList nodeHighlightConditions, int nodeSize) {
		List<Color> colors = new ArrayList<>();
		Map<V, List<Double>> alphaValues = new LinkedHashMap<>();
		Map<V, Double> thicknessValues = new LinkedHashMap<>();
		Map<V, Set<String>> labelLists = new LinkedHashMap<>();
		Collection<V> nodes = viewer.getGraphLayout().getGraph().getVertices();
		boolean prioritize = nodeHighlightConditions.isPrioritizeColors();

		for (V node : nodes) {
			alphaValues.put(node, new ArrayList<Double>());
			thicknessValues.put(node, 0.0);
		}

		for (HighlightCondition condition : nodeHighlightConditions
				.getConditions()) {
			if (condition.isInvisible()) {
				continue;
			}

			Map<V, Double> values = condition.getValues(nodes);

			if (condition.isUseThickness()) {
				for (V node : nodes) {
					thicknessValues.put(node, thicknessValues.get(node)
							+ values.get(node));
				}
			}

			if (condition.getColor() != null) {
				colors.add(condition.getColor());

				for (V node : nodes) {
					List<Double> alphas = alphaValues.get(node);

					if (!prioritize || alphas.isEmpty()
							|| Collections.max(alphas) == 0.0) {
						alphas.add(values.get(node));
					} else {
						alphas.add(0.0);
					}
				}
			}

			if (condition.getLabelProperty() != null) {
				String property = condition.getLabelProperty();

				for (V node : nodes) {
					if (values.get(node) != 0.0
							&& node.getProperties().get(property) != null) {
						if (!labelLists.containsKey(node)) {
							labelLists.put(node, new LinkedHashSet<String>());
						}

						labelLists.get(node).add(
								node.getProperties().get(property).toString());
					}
				}
			}
		}

		Map<V, String> labels = new LinkedHashMap<>();

		for (V node : labelLists.keySet()) {
			if (!labelLists.get(node).isEmpty()) {
				String label = "";

				for (String s : labelLists.get(node)) {
					label += s + "/";
				}

				labels.put(node, label.substring(0, label.length() - 1));
			}
		}

		viewer.getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<>(nodeSize, thicknessValues));
		viewer.getRenderContext().setVertexFillPaintTransformer(
				new NodeFillTransformer<>(viewer, alphaValues, colors));
		viewer.getRenderContext().setVertexLabelTransformer(
				new LabelTransformer<>(labels));
	}

	public static <V extends Node> void applyNodeLabels(
			VisualizationViewer<V, Edge<V>> viewer,
			HighlightConditionList nodeHighlightConditions) {
		Map<V, Set<String>> labelLists = new LinkedHashMap<>();
		Collection<V> nodes = viewer.getGraphLayout().getGraph().getVertices();

		for (HighlightCondition condition : nodeHighlightConditions
				.getConditions()) {
			Map<V, Double> values = condition.getValues(nodes);

			if (condition.getLabelProperty() != null) {
				String property = condition.getLabelProperty();

				for (V node : nodes) {
					if (values.get(node) != 0.0
							&& node.getProperties().get(property) != null) {
						if (!labelLists.containsKey(node)) {
							labelLists.put(node, new LinkedHashSet<String>());
						}

						labelLists.get(node).add(
								node.getProperties().get(property).toString());
					}
				}
			}
		}

		Map<V, String> labels = new LinkedHashMap<>();

		for (V node : labelLists.keySet()) {
			if (!labelLists.get(node).isEmpty()) {
				String label = "";

				for (String s : labelLists.get(node)) {
					label += s + "/";
				}

				labels.put(node, label.substring(0, label.length() - 1));
			}
		}

		viewer.getRenderContext().setVertexLabelTransformer(
				new LabelTransformer<>(labels));
	}

	public static <V extends Node> void applyEdgeHighlights(
			VisualizationViewer<V, Edge<V>> viewer,
			HighlightConditionList edgeHighlightConditions) {
		List<Color> colors = new ArrayList<>();
		Map<Edge<V>, List<Double>> alphaValues = new LinkedHashMap<>();
		Map<Edge<V>, Double> thicknessValues = new LinkedHashMap<>();
		Map<Edge<V>, Set<String>> labelLists = new LinkedHashMap<>();
		Collection<Edge<V>> edges = viewer.getGraphLayout().getGraph()
				.getEdges();
		boolean prioritize = edgeHighlightConditions.isPrioritizeColors();

		for (Edge<V> edge : edges) {
			alphaValues.put(edge, new ArrayList<Double>());
			thicknessValues.put(edge, 0.0);
		}

		for (HighlightCondition condition : edgeHighlightConditions
				.getConditions()) {
			if (condition.isInvisible()) {
				continue;
			}

			Map<Edge<V>, Double> values = condition.getValues(edges);

			if (condition.getColor() != null) {
				colors.add(condition.getColor());

				for (Edge<V> edge : edges) {
					List<Double> alphas = alphaValues.get(edge);

					if (!prioritize || alphas.isEmpty()
							|| Collections.max(alphas) == 0.0) {
						alphas.add(values.get(edge));
					} else {
						alphas.add(0.0);
					}
				}
			}

			if (condition.isUseThickness()) {
				for (Edge<V> edge : edges) {
					thicknessValues.put(edge, thicknessValues.get(edge)
							+ values.get(edge));
				}
			}

			if (condition.getLabelProperty() != null) {
				String property = condition.getLabelProperty();

				for (Edge<V> edge : edges) {
					if (values.get(edge) != 0.0
							&& edge.getProperties().get(property) != null) {
						if (!labelLists.containsKey(edge)) {
							labelLists.put(edge, new LinkedHashSet<String>());
						}

						labelLists.get(edge).add(
								edge.getProperties().get(property).toString());
					}
				}
			}
		}

		Map<Edge<V>, String> labels = new LinkedHashMap<>();

		for (Edge<V> edge : labelLists.keySet()) {
			if (!labelLists.get(edge).isEmpty()) {
				String label = "";

				for (String s : labelLists.get(edge)) {
					label += s + "/";
				}

				labels.put(edge, label.substring(0, label.length() - 1));
			}
		}

		viewer.getRenderContext().setEdgeDrawPaintTransformer(
				new EdgeDrawTransformer<>(viewer, alphaValues, colors));
		viewer.getRenderContext().setEdgeStrokeTransformer(
				new EdgeStrokeTransformer<>(thicknessValues));
		viewer.getRenderContext().setEdgeArrowTransformer(
				new EdgeArrowTransformer<>(thicknessValues));
		viewer.getRenderContext().setEdgeLabelTransformer(
				new LabelTransformer<>(labels));
	}

	public static Paint mixColors(Color backgroundColor, List<Color> colors,
			List<Double> alphas) {
		double rb = backgroundColor.getRed() / 255.0;
		double gb = backgroundColor.getGreen() / 255.0;
		double bb = backgroundColor.getBlue() / 255.0;
		List<Color> cs = new ArrayList<>();

		for (int i = 0; i < colors.size(); i++) {
			double alpha = alphas.get(i);

			if (alpha > 0.0) {
				double r = colors.get(i).getRed() / 255.0 * alpha + rb
						* (1 - alpha);
				double g = colors.get(i).getGreen() / 255.0 * alpha + gb
						* (1 - alpha);
				double b = colors.get(i).getBlue() / 255.0 * alpha + bb
						* (1 - alpha);

				cs.add(new Color((float) r, (float) g, (float) b));
			}
		}

		if (cs.isEmpty()) {
			return backgroundColor;
		} else if (cs.size() == 1) {
			return cs.get(0);
		}

		BufferedImage img = new BufferedImage(cs.size() * TEXTURE_SIZE, 1,
				BufferedImage.TYPE_INT_RGB);

		for (int i = 0; i < cs.size() * TEXTURE_SIZE; i++) {
			img.setRGB(i, 0, cs.get(i / TEXTURE_SIZE).getRGB());
		}

		return new TexturePaint(img, new Rectangle(img.getWidth(),
				img.getHeight()));
	}

	public static void drawImageWithAlpha(Graphics g, BufferedImage img,
			int alpha) {
		float[] edgeScales = { 1f, 1f, 1f, alpha / 255.0f };
		float[] edgeOffsets = new float[4];

		((Graphics2D) g).drawImage(img, new RescaleOp(edgeScales, edgeOffsets,
				null), 0, 0);
	}

	public static <T extends Element> Set<T> removeInvisibleElements(
			Set<T> elements, HighlightConditionList highlightConditions) {
		Set<T> removed = new LinkedHashSet<>();

		for (HighlightCondition condition : highlightConditions.getConditions()) {
			if (!condition.isInvisible()) {
				continue;
			}

			Map<T, Double> values = condition.getValues(elements);

			for (Iterator<T> iterator = elements.iterator(); iterator.hasNext();) {
				T element = iterator.next();

				if (values.get(element) != 0.0) {
					removed.add(element);
					iterator.remove();
				}
			}
		}

		return removed;
	}

	public static <V extends Node> Set<Edge<V>> removeNodelessEdges(
			Set<Edge<V>> edges, Set<V> nodes) {
		Set<Edge<V>> removed = new LinkedHashSet<>();

		for (Iterator<Edge<V>> iterator = edges.iterator(); iterator.hasNext();) {
			Edge<V> edge = iterator.next();

			if (!nodes.contains(edge.getFrom())
					|| !nodes.contains(edge.getTo())) {
				removed.add(edge);
				iterator.remove();
			}
		}

		return removed;
	}

	public static <V extends Node> Set<V> removeEdgelessNodes(Set<V> nodes,
			Set<Edge<V>> edges) {
		Set<V> nodesWithEdges = new LinkedHashSet<>();

		for (Edge<V> edge : edges) {
			nodesWithEdges.add(edge.getFrom());
			nodesWithEdges.add(edge.getTo());
		}

		Set<V> removed = new LinkedHashSet<>();

		for (Iterator<V> iterator = nodes.iterator(); iterator.hasNext();) {
			V node = iterator.next();

			if (!nodesWithEdges.contains(node)) {
				removed.add(node);
				iterator.remove();
			}
		}

		return removed;
	}

	public static <V extends Node> Graph<V, Edge<V>> createGraph(
			Collection<V> nodes, Collection<Edge<V>> edges) {
		Graph<V, Edge<V>> graph = new DirectedSparseMultigraph<>();

		for (V node : nodes) {
			graph.addVertex(node);
		}

		for (Edge<V> edge : edges) {
			graph.addEdge(edge, edge.getFrom(), edge.getTo());
		}

		return graph;
	}

	public static BufferedImage getBufferedImage(Canvas<?>... canvas) {
		int width = 0;
		int height = 0;

		for (Canvas<?> c : canvas) {
			width += c.getCanvasSize().width;
			height = Math.max(height, c.getCanvasSize().height);
		}

		BufferedImage img = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		int x = 0;

		for (Canvas<?> c : canvas) {
			VisualizationImageServer<?, ?> server = c
					.getVisualizationServer(false);

			g.translate(x, 0);
			server.paint(g);
			x += c.getCanvasSize().width;
		}

		return img;
	}

	public static SVGDocument getSvgDocument(Canvas<?>... canvas) {
		int width = 0;
		int height = 0;

		for (Canvas<?> c : canvas) {
			width += c.getCanvasSize().width;
			height = Math.max(height, c.getCanvasSize().height);
		}

		SVGDOMImplementation domImpl = new SVGDOMImplementation();
		Document document = domImpl.createDocument(null, "svg", null);
		SVGGraphics2D g = new SVGGraphics2D(document);
		int x = 0;

		g.setSVGCanvasSize(new Dimension(width, height));

		for (Canvas<?> c : canvas) {
			VisualizationImageServer<?, ?> server = c
					.getVisualizationServer(true);

			g.translate(x, 0);
			server.paint(g);
			x += c.getCanvasSize().width;
		}

		g.finalize();
		document.replaceChild(g.getRoot(), document.getDocumentElement());

		return (SVGDocument) document;
	}

	public static ImagePortObject getImage(boolean asSvg, Canvas<?>... canvas)
			throws IOException {
		if (asSvg) {
			return new ImagePortObject(new SvgImageContent(
					CanvasUtils.getSvgDocument(canvas), true),
					new ImagePortObjectSpec(SvgCell.TYPE));
		} else {
			BufferedImage img = CanvasUtils.getBufferedImage(canvas);
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			ImageIO.write(img, "png", out);

			return new ImagePortObject(new PNGImageContent(out.toByteArray()),
					new ImagePortObjectSpec(PNGImageContent.TYPE));
		}
	}

	public static ImagePortObjectSpec getImageSpec(boolean asSvg) {
		if (asSvg) {
			return new ImagePortObjectSpec(SvgCell.TYPE);
		} else {
			return new ImagePortObjectSpec(PNGImageContent.TYPE);
		}
	}
}