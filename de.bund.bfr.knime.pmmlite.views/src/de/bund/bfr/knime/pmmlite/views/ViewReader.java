/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.views;

import java.util.Map;

import de.bund.bfr.knime.pmmlite.core.Plotable;
import de.bund.bfr.knime.pmmlite.core.PmmUnit;
import de.bund.bfr.knime.pmmlite.views.chart.ChartSelectionPanel;

public interface ViewReader {

	Map<String, Plotable> getPlotables();

	Map<String, String> getLegend();

	Map<String, PmmUnit> getUnits();

	ChartSelectionPanel.Builder getSelectionPanelBuilder();
}
