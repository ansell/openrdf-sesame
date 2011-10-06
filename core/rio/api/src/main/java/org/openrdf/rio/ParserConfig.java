/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import org.openrdf.rio.RDFParser.DatatypeHandling;

/**
 * A read-only container object for easy passing of {@link RDFParser}
 * configuration options.
 * 
 * @author Jeen Broekstra
 */
public class ParserConfig {

	private final boolean verifyData;

	private final boolean stopAtFirstError;

	private final boolean preserveBNodeIDs;

	private final DatatypeHandling datatypeHandling;

	/**
	 * Creates a ParserConfig object with default configuration: verifyData is
	 * <tt>true</tt>, stopAtFirstError is <tt>true</tt>, preserveBNodeIDS is
	 * <tt>false</tt>, and dataTypeHandling is set to VERIFY.
	 */
	public ParserConfig() {
		this(true, true, false, DatatypeHandling.VERIFY);
	}

	/**
	 * Creates a ParserConfig object with the supplied config settings.
	 */
	public ParserConfig(boolean verifyData, boolean stopAtFirstError, boolean preserveBNodeIDs,
			DatatypeHandling datatypeHandling)
	{
		this.verifyData = verifyData;
		this.stopAtFirstError = stopAtFirstError;
		this.preserveBNodeIDs = preserveBNodeIDs;
		this.datatypeHandling = datatypeHandling;
	}

	/**
	 * @return Returns the verifyData setting.
	 */
	public boolean verifyData() {
		return verifyData;
	}

	/**
	 * @return Returns the stopAtFirstError setting.
	 */
	public boolean stopAtFirstError() {
		return stopAtFirstError;
	}

	/**
	 * @return Returns the preserveBNodeIDs setting.
	 */
	public boolean isPreserveBNodeIDs() {
		return preserveBNodeIDs;
	}

	/**
	 * @return Returns the datatypeHandling setting.
	 */
	public DatatypeHandling datatypeHandling() {
		return datatypeHandling;
	}

}
