/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.query.resultio;

import java.util.Collection;
import java.util.Collections;

import org.openrdf.query.QueryResultHandler;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RioSetting;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Base class for {@link QueryResultParser}s offering common functionality for
 * query result parsers.
 */
public abstract class AbstractQueryResultParser implements QueryResultParser {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The {@link ValueFactory} to use for creating RDF model objects.
	 */
	protected ValueFactory valueFactory;

	/**
	 * The {@link QueryResultHandler} that will handle the parsed query results.
	 */
	protected QueryResultHandler handler;

	private ParserConfig parserConfig = new ParserConfig();

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new parser base that, by default, will use the global instance
	 * of {@link SimpleValueFactory} to create Value objects.
	 */
	public AbstractQueryResultParser() {
		this(SimpleValueFactory.getInstance());
	}

	/**
	 * Creates a new parser base that will use the supplied ValueFactory to
	 * create Value objects.
	 */
	public AbstractQueryResultParser(ValueFactory valueFactory) {
		setValueFactory(valueFactory);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void setValueFactory(ValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

	@Override
	public void setQueryResultHandler(QueryResultHandler handler) {
		this.handler = handler;
	}

	@Override
	public void setParserConfig(ParserConfig config) {
		this.parserConfig = config;
	}

	@Override
	public ParserConfig getParserConfig() {
		return this.parserConfig;
	}

	/*
	 * Default implementation. Implementing classes may override this to declare their supported settings.
	 */
	@Override
	public Collection<RioSetting<?>> getSupportedSettings() {
		return Collections.emptyList();
	}
}
