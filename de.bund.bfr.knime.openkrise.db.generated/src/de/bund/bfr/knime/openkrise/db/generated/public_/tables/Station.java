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
 * This class is generated by jOOQ
 */
package de.bund.bfr.knime.openkrise.db.generated.public_.tables;


import de.bund.bfr.knime.openkrise.db.generated.public_.Keys;
import de.bund.bfr.knime.openkrise.db.generated.public_.Public;
import de.bund.bfr.knime.openkrise.db.generated.public_.tables.records.StationRecord;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Identity;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.6.2"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Station extends TableImpl<StationRecord> {

	private static final long serialVersionUID = 984748903;

	/**
	 * The reference instance of <code>PUBLIC.Station</code>
	 */
	public static final Station STATION = new Station();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<StationRecord> getRecordType() {
		return StationRecord.class;
	}

	/**
	 * The column <code>PUBLIC.Station.ID</code>.
	 */
	public final TableField<StationRecord, Integer> ID = createField("ID", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.Station.Produktkatalog</code>.
	 */
	public final TableField<StationRecord, Integer> PRODUKTKATALOG = createField("Produktkatalog", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Station.Name</code>.
	 */
	public final TableField<StationRecord, String> NAME = createField("Name", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>PUBLIC.Station.Strasse</code>.
	 */
	public final TableField<StationRecord, String> STRASSE = createField("Strasse", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>PUBLIC.Station.Hausnummer</code>.
	 */
	public final TableField<StationRecord, String> HAUSNUMMER = createField("Hausnummer", org.jooq.impl.SQLDataType.VARCHAR.length(10), this, "");

	/**
	 * The column <code>PUBLIC.Station.Postfach</code>.
	 */
	public final TableField<StationRecord, String> POSTFACH = createField("Postfach", org.jooq.impl.SQLDataType.VARCHAR.length(20), this, "");

	/**
	 * The column <code>PUBLIC.Station.PLZ</code>.
	 */
	public final TableField<StationRecord, String> PLZ = createField("PLZ", org.jooq.impl.SQLDataType.VARCHAR.length(10), this, "");

	/**
	 * The column <code>PUBLIC.Station.Ort</code>.
	 */
	public final TableField<StationRecord, String> ORT = createField("Ort", org.jooq.impl.SQLDataType.VARCHAR.length(60), this, "");

	/**
	 * The column <code>PUBLIC.Station.District</code>.
	 */
	public final TableField<StationRecord, String> DISTRICT = createField("District", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>PUBLIC.Station.Bundesland</code>.
	 */
	public final TableField<StationRecord, String> BUNDESLAND = createField("Bundesland", org.jooq.impl.SQLDataType.VARCHAR.length(30), this, "");

	/**
	 * The column <code>PUBLIC.Station.Land</code>.
	 */
	public final TableField<StationRecord, String> LAND = createField("Land", org.jooq.impl.SQLDataType.VARCHAR.length(100), this, "");

	/**
	 * The column <code>PUBLIC.Station.Longitude</code>.
	 */
	public final TableField<StationRecord, Double> LONGITUDE = createField("Longitude", org.jooq.impl.SQLDataType.DOUBLE, this, "");

	/**
	 * The column <code>PUBLIC.Station.Latitude</code>.
	 */
	public final TableField<StationRecord, Double> LATITUDE = createField("Latitude", org.jooq.impl.SQLDataType.DOUBLE, this, "");

	/**
	 * The column <code>PUBLIC.Station.Ansprechpartner</code>.
	 */
	public final TableField<StationRecord, String> ANSPRECHPARTNER = createField("Ansprechpartner", org.jooq.impl.SQLDataType.VARCHAR.length(100), this, "");

	/**
	 * The column <code>PUBLIC.Station.Telefon</code>.
	 */
	public final TableField<StationRecord, String> TELEFON = createField("Telefon", org.jooq.impl.SQLDataType.VARCHAR.length(30), this, "");

	/**
	 * The column <code>PUBLIC.Station.Fax</code>.
	 */
	public final TableField<StationRecord, String> FAX = createField("Fax", org.jooq.impl.SQLDataType.VARCHAR.length(30), this, "");

	/**
	 * The column <code>PUBLIC.Station.EMail</code>.
	 */
	public final TableField<StationRecord, String> EMAIL = createField("EMail", org.jooq.impl.SQLDataType.VARCHAR.length(100), this, "");

	/**
	 * The column <code>PUBLIC.Station.Webseite</code>.
	 */
	public final TableField<StationRecord, String> WEBSEITE = createField("Webseite", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>PUBLIC.Station.Betriebsnummer</code>.
	 */
	public final TableField<StationRecord, String> BETRIEBSNUMMER = createField("Betriebsnummer", org.jooq.impl.SQLDataType.VARCHAR.length(50), this, "");

	/**
	 * The column <code>PUBLIC.Station.Betriebsart</code>.
	 */
	public final TableField<StationRecord, String> BETRIEBSART = createField("Betriebsart", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>PUBLIC.Station.VATnumber</code>.
	 */
	public final TableField<StationRecord, String> VATNUMBER = createField("VATnumber", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>PUBLIC.Station.Code</code>.
	 */
	public final TableField<StationRecord, String> CODE = createField("Code", org.jooq.impl.SQLDataType.VARCHAR.length(25), this, "");

	/**
	 * The column <code>PUBLIC.Station.CasePriority</code>.
	 */
	public final TableField<StationRecord, Double> CASEPRIORITY = createField("CasePriority", org.jooq.impl.SQLDataType.DOUBLE, this, "");

	/**
	 * The column <code>PUBLIC.Station.AnzahlFaelle</code>.
	 */
	public final TableField<StationRecord, Integer> ANZAHLFAELLE = createField("AnzahlFaelle", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Station.AlterMin</code>.
	 */
	public final TableField<StationRecord, Integer> ALTERMIN = createField("AlterMin", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Station.AlterMax</code>.
	 */
	public final TableField<StationRecord, Integer> ALTERMAX = createField("AlterMax", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Station.DatumBeginn</code>.
	 */
	public final TableField<StationRecord, Date> DATUMBEGINN = createField("DatumBeginn", org.jooq.impl.SQLDataType.DATE, this, "");

	/**
	 * The column <code>PUBLIC.Station.DatumHoehepunkt</code>.
	 */
	public final TableField<StationRecord, Date> DATUMHOEHEPUNKT = createField("DatumHoehepunkt", org.jooq.impl.SQLDataType.DATE, this, "");

	/**
	 * The column <code>PUBLIC.Station.DatumEnde</code>.
	 */
	public final TableField<StationRecord, Date> DATUMENDE = createField("DatumEnde", org.jooq.impl.SQLDataType.DATE, this, "");

	/**
	 * The column <code>PUBLIC.Station.Erregernachweis</code>.
	 */
	public final TableField<StationRecord, Integer> ERREGERNACHWEIS = createField("Erregernachweis", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Station.Serial</code>.
	 */
	public final TableField<StationRecord, String> SERIAL = createField("Serial", org.jooq.impl.SQLDataType.VARCHAR.length(16383), this, "");

	/**
	 * The column <code>PUBLIC.Station.ImportSources</code>.
	 */
	public final TableField<StationRecord, String> IMPORTSOURCES = createField("ImportSources", org.jooq.impl.SQLDataType.VARCHAR.length(16383), this, "");

	/**
	 * The column <code>PUBLIC.Station.Kommentar</code>.
	 */
	public final TableField<StationRecord, String> KOMMENTAR = createField("Kommentar", org.jooq.impl.SQLDataType.VARCHAR.length(1023), this, "");

	/**
	 * The column <code>PUBLIC.Station.Adresse</code>.
	 */
	public final TableField<StationRecord, String> ADRESSE = createField("Adresse", org.jooq.impl.SQLDataType.VARCHAR.length(32768), this, "");

	/**
	 * Create a <code>PUBLIC.Station</code> table reference
	 */
	public Station() {
		this("Station", null);
	}

	/**
	 * Create an aliased <code>PUBLIC.Station</code> table reference
	 */
	public Station(String alias) {
		this(alias, STATION);
	}

	private Station(String alias, Table<StationRecord> aliased) {
		this(alias, aliased, null);
	}

	private Station(String alias, Table<StationRecord> aliased, Field<?>[] parameters) {
		super(alias, Public.PUBLIC, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Identity<StationRecord, Integer> getIdentity() {
		return Keys.IDENTITY_STATION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<StationRecord> getPrimaryKey() {
		return Keys.SYS_PK_10149;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<StationRecord>> getKeys() {
		return Arrays.<UniqueKey<StationRecord>>asList(Keys.SYS_PK_10149);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Station as(String alias) {
		return new Station(alias, this);
	}

	/**
	 * Rename this table
	 */
	public Station rename(String name) {
		return new Station(name, null);
	}
}
