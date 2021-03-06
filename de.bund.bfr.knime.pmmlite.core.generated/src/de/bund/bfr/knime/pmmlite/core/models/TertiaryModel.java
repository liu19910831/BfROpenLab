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
/**
 */
package de.bund.bfr.knime.pmmlite.core.models;

import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Tertiary Model</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModel#getFormula <em>Formula</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModel#getData <em>Data</em>}</li>
 * </ul>
 *
 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getTertiaryModel()
 * @model
 * @generated
 */
public interface TertiaryModel extends Model {
	/**
	 * Returns the value of the '<em><b>Formula</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Formula</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Formula</em>' reference.
	 * @see #setFormula(TertiaryModelFormula)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getTertiaryModel_Formula()
	 * @model
	 * @generated
	 */
	TertiaryModelFormula getFormula();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.TertiaryModel#getFormula <em>Formula</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Formula</em>' reference.
	 * @see #getFormula()
	 * @generated
	 */
	void setFormula(TertiaryModelFormula value);

	/**
	 * Returns the value of the '<em><b>Data</b></em>' reference list.
	 * The list contents are of type {@link de.bund.bfr.knime.pmmlite.core.data.TimeSeries}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data</em>' reference list.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getTertiaryModel_Data()
	 * @model
	 * @generated
	 */
	EList<TimeSeries> getData();

} // TertiaryModel
