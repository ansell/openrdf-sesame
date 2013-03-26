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
package org.openrdf.rio;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;

/**
 * An interface for RDF document writers. To allow RDF document writers to be
 * created through reflection, all implementing classes should define at least
 * two public constructors: one with an {@link OutputStream} argument and one
 * with an {@link Writer} argument.
 */
public interface RDFWriter extends RDFHandler {

	/**
	 * Gets the RDF format that this RDFWriter uses.
	 */
	public RDFFormat getRDFFormat();

	/**
	 * Sets all supplied writer configuration options.
	 * 
	 * @param config
	 *        a writer configuration object.
	 * @since 2.7.0
	 */
	public void setWriterConfig(WriterConfig config);

	/**
	 * Retrieves the current writer configuration as a single object.
	 * 
	 * @return a writer configuration object representing the current
	 *         configuration of the writer.
	 * @since 2.7.0
	 */
	public WriterConfig getWriterConfig();

	/**
	 * @return A collection of {@link RioSetting}s that are supported by this
	 *         RDFWriter.
	 * @since 2.7.0
	 */
	public Collection<RioSetting<?>> getSupportedSettings();

}
