/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.rio.RDFParser.DatatypeHandling;
import org.openrdf.rio.helpers.BasicParserSettings;

/**
 * A container object for easy setting and passing of {@link RDFParser}
 * configuration options.
 * 
 * @author Jeen Broekstra
 */
public class ParserConfig {

	private final ConcurrentMap<ParserSetting<Object>, Object> settings = new ConcurrentHashMap<ParserSetting<Object>, Object>();

	protected final Logger log = LoggerFactory.getLogger(ParserConfig.class);

	/**
	 * Creates a ParserConfig object with default configuration: verifyData is
	 * <tt>true</tt>, stopAtFirstError is <tt>true</tt>, preserveBNodeIDS is
	 * <tt>false</tt>, and dataTypeHandling is set to VERIFY.
	 */
	public ParserConfig() {
	}

	/**
	 * Creates a ParserConfig object with the supplied config settings.
	 */
	public ParserConfig(boolean verifyData, boolean stopAtFirstError, boolean preserveBNodeIDs,
			DatatypeHandling datatypeHandling)
	{
		this.set(BasicParserSettings.VERIFY_DATA, verifyData);
		this.set(BasicParserSettings.STOP_AT_FIRST_ERROR, stopAtFirstError);
		this.set(BasicParserSettings.PRESERVE_BNODE_IDS, preserveBNodeIDs);
		this.set(BasicParserSettings.DATATYPE_HANDLING, datatypeHandling);
	}

	/**
	 * @return Returns the {@link BasicParserSettings#VERIFY_DATA} setting.
	 */
	public boolean verifyData() {
		return this.get(BasicParserSettings.VERIFY_DATA);
	}

	/**
	 * @return Returns the {@link BasicParserSettings#STOP_AT_FIRST_ERROR}
	 *         setting.
	 */
	public boolean stopAtFirstError() {
		return this.get(BasicParserSettings.STOP_AT_FIRST_ERROR);
	}

	/**
	 * @return Returns the {@link BasicParserSettings#PRESERVE_BNODE_IDS}
	 *         setting.
	 */
	public boolean isPreserveBNodeIDs() {
		return this.get(BasicParserSettings.PRESERVE_BNODE_IDS);
	}

	/**
	 * @return Returns the {@link BasicParserSettings#DATATYPE_HANDLING} setting.
	 */
	public DatatypeHandling datatypeHandling() {
		return this.get(BasicParserSettings.DATATYPE_HANDLING);
	}

	/**
	 * Return the value for a given {@link ParserSetting} or the default value if
	 * it has not been set.
	 * 
	 * @param setting
	 *        The {@link ParserSetting} to fetch a value for.
	 * @return The value for the parser setting, or the default value if it is
	 *         not set.
	 * @since 2.7.0
	 */
	@SuppressWarnings("unchecked")
	public final <T extends Object> T get(ParserSetting<T> setting) {
		Object result = settings.get(setting);

		if (result == null) {
			return setting.getDefaultValue();
		}

		return (T)result;
	}

	/**
	 * Sets a {@link ParserSetting} to have a new value. If the value is null,
	 * the parser setting is removed and the default will be used instead.
	 * 
	 * @param setting
	 *        The setting to set a new value for.
	 * @param value
	 *        The value for the parser setting, or null to reset the parser
	 *        setting to use the default value.
	 * @since 2.7.0
	 */
	@SuppressWarnings("unchecked")
	public final <T extends Object> void set(ParserSetting<T> setting, T value) {

		if (value == null) {
			settings.remove(setting);
		}
		else {
			Object putIfAbsent = settings.putIfAbsent((ParserSetting<Object>)setting, value);

			if (putIfAbsent != null) {
				settings.put((ParserSetting<Object>)setting, value);

				this.log.trace("Overriding previous setting for {}", setting.getDefaultValue());
			}
		}
	}

	/**
	 * Checks for whether a {@link ParserSetting} has been explicitly set by a
	 * user.
	 * 
	 * @param setting
	 *        The setting to check for.
	 * @return True if the parser setting has been explicitly set, or false
	 *         otherwise.
	 * @since 2.7.0
	 */
	public final <T extends Object> boolean isSet(ParserSetting<T> setting) {
		return settings.containsKey(setting);
	}
}
