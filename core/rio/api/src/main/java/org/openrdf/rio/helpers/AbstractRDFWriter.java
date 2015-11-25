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
package org.openrdf.rio.helpers;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.WriterConfig;

/**
 * Base class for {@link RDFWriter}s offering common functionality for RDF
 * writers.
 * 
 * @author Peter Ansell
 */
public abstract class AbstractRDFWriter implements RDFWriter {

	/**
	 * Mapping from namespace prefixes to namespace names.
	 */
	protected Map<String, String> namespaceTable;

	/**
	 * A collection of configuration options for this writer.
	 */
	private WriterConfig writerConfig = new WriterConfig();

	/**
	 * Default constructor.
	 */
	public AbstractRDFWriter() {
	}

	@Override
	public void handleNamespace(String prefix, String uri)
		throws RDFHandlerException
	{
		namespaceTable.put(prefix, uri);
	}

	@Override
	public RDFWriter setWriterConfig(WriterConfig config) {
		this.writerConfig = config;
		return this;
	}

	@Override
	public WriterConfig getWriterConfig() {
		return this.writerConfig;
	}

	/*
	 * Default implementation. Implementing classes must override this to specify that they support given settings.
	 */
	@Override
	public Collection<RioSetting<?>> getSupportedSettings() {
		return Collections.emptyList();
	}

	@Override
	public <T> RDFWriter set(RioSetting<T> setting, T value) {
		getWriterConfig().set(setting, value);
		return this;
	}
}
