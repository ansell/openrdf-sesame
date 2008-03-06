/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

public class DAWGTestCase {

	private String _name;
	private String _comment;
	
	private String _queryFile;
	private String _dataSet;
	
	private String _result;
	
	public void setComment(String comment) {
		_comment = comment;
	}
	
	public String getComment() {
		return _comment;
	}
	
	public void setName(String name) {
		_name = name;
	}
	
	public String getName() {
		return _name;
	}

	public void setQueryFile(String queryFile) {
		_queryFile = queryFile;
	}
	
	public String getQueryFile() {
		return _queryFile;
	}
	
	public void setResult(String result) {
		_result = result;
	}
	
	public String getResult() {
		return _result;
	}
	
	public void setDataSet(String dataSet) {
		_dataSet = dataSet;
	}
	
	public String getDataSet() {
		return _dataSet;
	}
	
}
