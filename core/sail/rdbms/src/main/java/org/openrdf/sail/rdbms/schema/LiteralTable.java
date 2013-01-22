/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.SQLException;

/**
 * A Facade to the five literal value tables. Which are labels, languages,
 * datatypes, numeric values, and dateTime values.
 * 
 * @author James Leigh
 * 
 */
public class LiteralTable {

	public static final boolean ONLY_INSERT_LABEL = false;

	private ValueTable labels;

	private ValueTable longLabels;

	private ValueTable languages;

	private ValueTable datatypes;

	private ValueTable numeric;

	private ValueTable dateTime;

	private int version;

	private IdSequence ids;

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	public ValueTable getLabelTable() {
		return labels;
	}

	public void setLabelTable(ValueTable labels) {
		this.labels = labels;
	}

	public ValueTable getLongLabelTable() {
		return longLabels;
	}

	public void setLongLabelTable(ValueTable longLabels) {
		this.longLabels = longLabels;
	}

	public ValueTable getLanguageTable() {
		return languages;
	}

	public void setLanguageTable(ValueTable languages) {
		this.languages = languages;
	}

	public ValueTable getDatatypeTable() {
		return datatypes;
	}

	public void setDatatypeTable(ValueTable datatypes) {
		this.datatypes = datatypes;
	}

	public ValueTable getNumericTable() {
		return numeric;
	}

	public void setNumericTable(ValueTable numeric) {
		this.numeric = numeric;
	}

	public ValueTable getDateTimeTable() {
		return dateTime;
	}

	public void setDateTimeTable(ValueTable dateTime) {
		this.dateTime = dateTime;
	}

	public void close()
		throws SQLException
	{
		labels.close();
		longLabels.close();
		languages.close();
		datatypes.close();
		numeric.close();
		dateTime.close();
	}

	public int getBatchSize() {
		return labels.getBatchSize();
	}

	public int getIdVersion() {
		return version;
	}

	public void insertSimple(Number id, String label)
		throws SQLException, InterruptedException
	{
		if (ids.isLong(id)) {
			longLabels.insert(id, label);
		}
		else {
			labels.insert(id, label);
		}
	}

	public void insertLanguage(Number id, String label, String language)
		throws SQLException, InterruptedException
	{
		insertSimple(id, label);
		languages.insert(id, language);
	}

	public void insertDatatype(Number id, String label, String datatype)
		throws SQLException, InterruptedException
	{
		insertSimple(id, label);
		datatypes.insert(id, datatype);
	}

	public void insertNumeric(Number id, String label, String datatype, double value)
		throws SQLException, InterruptedException
	{
		labels.insert(id, label);
		datatypes.insert(id, datatype);
		numeric.insert(id, value);
	}

	public void insertDateTime(Number id, String label, String datatype, long value)
		throws SQLException, InterruptedException
	{
		labels.insert(id, label);
		datatypes.insert(id, datatype);
		dateTime.insert(id, value);
	}

	public void optimize()
		throws SQLException
	{
		labels.optimize();
		longLabels.optimize();
		languages.optimize();
		datatypes.optimize();
		numeric.optimize();
		dateTime.optimize();
	}

	public boolean expunge(String condition)
		throws SQLException
	{
		boolean bool = false;
		bool |= labels.expunge(condition);
		bool |= longLabels.expunge(condition);
		bool |= languages.expunge(condition);
		bool |= datatypes.expunge(condition);
		bool |= numeric.expunge(condition);
		bool |= dateTime.expunge(condition);
		return bool;
	}
}
