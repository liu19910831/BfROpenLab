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
package de.bund.bfr.knime.gis.views.regionvisualizer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.BufferedDataTable;

import com.vividsolutions.jts.geom.MultiPolygon;

import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.gis.views.ViewUtilities;
import de.bund.bfr.knime.gis.views.canvas.RegionCanvas;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;

public class RegionVisualizerCanvasCreator {

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;
	private RegionVisualizerSettings set;

	private Set<String> nonExistingRegions;

	public RegionVisualizerCanvasCreator(BufferedDataTable shapeTable,
			BufferedDataTable nodeTable, RegionVisualizerSettings set) {
		this.shapeTable = shapeTable;
		this.nodeTable = nodeTable;
		this.set = set;

		nonExistingRegions = new LinkedHashSet<>();
	}

	public RegionCanvas createCanvas() {
		Map<String, MultiPolygon> polygonMap = ViewUtilities.readPolygons(
				shapeTable, set.getGisSettings().getShapeColumn(), set
						.getGisSettings().getShapeRegionColumn());
		Map<String, Class<?>> nodeProperties = KnimeUtilities
				.getTableColumns(nodeTable.getSpec());
		List<RegionNode> nodes = new ArrayList<>(ViewUtilities.readRegionNodes(
				nodeTable, nodeProperties, polygonMap, null,
				set.getGisSettings().getNodeRegionColumn(), nonExistingRegions)
				.values());

		if (nodes.isEmpty()) {
			return null;
		}

		RegionCanvas canvas = new RegionCanvas(nodes, nodeProperties, set
				.getGisSettings().getNodeRegionColumn());

		canvas.setShowLegend(set.getGisSettings().isShowLegend());
		canvas.setCanvasSize(set.getGisSettings().getCanvasSize());
		canvas.setFontSize(set.getGisSettings().getFontSize());
		canvas.setFontBold(set.getGisSettings().isFontBold());
		canvas.setBorderAlpha(set.getGisSettings().getBorderAlpha());
		canvas.setEditingMode(set.getGisSettings().getEditingMode());
		canvas.setNodeHighlightConditions(set.getGisSettings()
				.getNodeHighlightConditions());

		if (!Double.isNaN(set.getGisSettings().getScaleX())
				&& !Double.isNaN(set.getGisSettings().getScaleY())
				&& !Double.isNaN(set.getGisSettings().getTranslationX())
				&& !Double.isNaN(set.getGisSettings().getTranslationY())) {
			canvas.setTransform(set.getGisSettings().getScaleX(), set
					.getGisSettings().getScaleY(), set.getGisSettings()
					.getTranslationX(), set.getGisSettings().getTranslationY());
		}

		return canvas;
	}

	public Set<String> getNonExistingRegions() {
		return nonExistingRegions;
	}
}
