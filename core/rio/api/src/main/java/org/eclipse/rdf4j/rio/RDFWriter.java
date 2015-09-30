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
package org.eclipse.rdf4j.rio;

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
