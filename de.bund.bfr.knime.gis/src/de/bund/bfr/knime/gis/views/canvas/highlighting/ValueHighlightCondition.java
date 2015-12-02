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
package de.bund.bfr.knime.gis.views.canvas.highlighting;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import de.bund.bfr.knime.gis.views.canvas.element.Element;

public class ValueHighlightCondition implements HighlightCondition, Serializable {

	public static final String VALUE_TYPE = "Value";
	public static final String LOG_VALUE_TYPE = "Log Value";
	public static final String[] TYPES = { VALUE_TYPE, LOG_VALUE_TYPE };

	private static final long serialVersionUID = 1L;

	private String property;
	private String type;
	private boolean zeroAsMinimum;
	private String name;
	private boolean showInLegend;
	private Color color;
	private boolean invisible;
	private boolean useThickness;
	private String labelProperty;

	public ValueHighlightCondition() {
		this(null, null, false, null, true, null, false, false, null);
	}

	public ValueHighlightCondition(String property, String type, boolean zeroAsMinimum, String name,
			boolean showInLegend, Color color, boolean invisible, boolean useThickness, String labelProperty) {
		setProperty(property);
		setType(type);
		setZeroAsMinimum(zeroAsMinimum);
		setName(name);
		setShowInLegend(showInLegend);
		setColor(color);
		setInvisible(invisible);
		setUseThickness(useThickness);
		setLabelProperty(labelProperty);
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isZeroAsMinimum() {
		return zeroAsMinimum;
	}

	public void setZeroAsMinimum(boolean zeroAsMinimum) {
		this.zeroAsMinimum = zeroAsMinimum;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isShowInLegend() {
		return showInLegend;
	}

	public void setShowInLegend(boolean showInLegend) {
		this.showInLegend = showInLegend;
	}

	@Override
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public boolean isInvisible() {
		return invisible;
	}

	public void setInvisible(boolean invisible) {
		this.invisible = invisible;
	}

	@Override
	public boolean isUseThickness() {
		return useThickness;
	}

	public void setUseThickness(boolean useThickness) {
		this.useThickness = useThickness;
	}

	@Override
	public String getLabelProperty() {
		return labelProperty;
	}

	public void setLabelProperty(String labelProperty) {
		this.labelProperty = labelProperty;
	}

	@Override
	public <T extends Element> Map<T, Double> getValues(Collection<? extends T> elements) {
		Map<T, Double> values = new LinkedHashMap<>();

		for (T element : elements) {
			values.put(element, toPositiveDouble(element.getProperties().get(property)));
		}

		if (values.isEmpty()) {
			return values;
		}

		if (!zeroAsMinimum) {
			double min = Collections.min(values.values());

			if (min != 0.0) {
				for (T element : elements) {
					values.put(element, values.get(element) - min);
				}
			}
		}

		double max = Collections.max(values.values());

		if (max != 0.0) {
			for (T element : elements) {
				values.put(element, values.get(element) / max);
			}
		}

		if (type.equals(LOG_VALUE_TYPE)) {
			for (T element : elements) {
				values.put(element, Math.log10(values.get(element) * 9.0 + 1.0));
			}
		}

		return values;
	}

	@Override
	public Point2D getValueRange(Collection<? extends Element> elements) {
		DoubleSummaryStatistics stats = elements.stream()
				.mapToDouble(e -> toPositiveDouble(e.getProperties().get(property))).summaryStatistics();
		double min = zeroAsMinimum || stats.getCount() == 0 ? 0.0 : stats.getMin();
		double max = stats.getCount() == 0 ? 1.0 : stats.getMax();

		return new Point2D.Double(min, max);
	}

	@Override
	public ValueHighlightCondition copy() {
		return new ValueHighlightCondition(property, type, zeroAsMinimum, name, showInLegend, color, invisible,
				useThickness, labelProperty);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + Objects.hashCode(color);
		result = prime * result + (invisible ? 1231 : 1237);
		result = prime * result + Objects.hashCode(labelProperty);
		result = prime * result + Objects.hashCode(name);
		result = prime * result + Objects.hashCode(property);
		result = prime * result + (showInLegend ? 1231 : 1237);
		result = prime * result + Objects.hashCode(type);
		result = prime * result + (useThickness ? 1231 : 1237);
		result = prime * result + (zeroAsMinimum ? 1231 : 1237);

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}

		ValueHighlightCondition c = (ValueHighlightCondition) obj;

		return Objects.equals(property, c.property) && Objects.equals(type, c.type) && zeroAsMinimum == c.zeroAsMinimum
				&& Objects.equals(name, c.name) && showInLegend == c.showInLegend && Objects.equals(color, c.color)
				&& invisible == c.invisible && useThickness == c.useThickness
				&& Objects.equals(labelProperty, c.labelProperty);
	}

	@Override
	public String toString() {
		return getName() != null ? getName() : "Value Condition";
	}

	protected static double toPositiveDouble(Object value) {
		if (value instanceof Number) {
			double d = ((Number) value).doubleValue();

			return Double.isFinite(d) && d >= 0.0 ? d : 0.0;
		}

		return 0.0;
	}
}
