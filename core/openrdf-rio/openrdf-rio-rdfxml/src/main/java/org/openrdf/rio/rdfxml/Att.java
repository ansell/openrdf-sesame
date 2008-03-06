/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.rdfxml;


/**
 * An XML attribute.
 */
class Att {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String _namespace;
	private String _localName;
	private String _qName;
	private String _value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Att(String namespace, String localName, String qName, String value) {
		_namespace = namespace;
		_localName = localName;
		_qName = qName;
		_value = value;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getNamespace() {
		return _namespace;
	}

	public String getLocalName() {
		return _localName;
	}

	public String getURI() {
		return _namespace + _localName;
	}

	public String getQName() {
		return _qName;
	}

	public String getValue() {
		return _value;
	}
}
