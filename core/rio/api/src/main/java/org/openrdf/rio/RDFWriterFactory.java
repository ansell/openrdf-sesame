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

import org.openrdf.model.URI;

/**
 * A RDFWriterFactory returns {@link RDFWriter}s for a specific RDF format.
 * 
 * @author Arjohn Kampman
 */
public interface RDFWriterFactory {

	/**
	 * Returns the RDF format for this factory.
	 */
	public RDFFormat getRDFFormat();

	/**
	 * Returns an RDFWriter instance that will write to the supplied output
	 * stream.
	 * 
	 * @param out
	 *        The OutputStream to write the RDF to.
	 */
	public RDFWriter getWriter(OutputStream out);

	/**
	 * Returns an RDFWriter instance that will write to the supplied output
	 * stream, using the specified URI for the overall document base URI.
	 * 
	 * @param out
	 *        The OutputStream to write the RDF to.
	 * @param baseURI
	 *        An optional base URI to use as the initial base URI for the
	 *        resulting RDFWriter. If null, then the default, if any, base URI,
	 *        for the given format must be used.
	 * @since 2.8.0
	 */
	public RDFWriter getWriter(OutputStream out, URI defaultBaseURI);

	/**
	 * Returns an RDFWriter instance that will write to the supplied writer.
	 * (Optional operation)
	 * 
	 * @param writer
	 *        The Writer to write the RDF to.
	 * @throws UnsupportedOperationException
	 *         if the RDFWriter the specific format does not support writing to a
	 *         {@link java.io.Writer}
	 */
	public RDFWriter getWriter(Writer writer);

	/**
	 * Returns an RDFWriter instance that will write to the supplied writer,
	 * using the specified URI for the overall document base URI. (Optional
	 * operation)
	 * 
	 * @param writer
	 *        The Writer to write the RDF to.
	 * @param baseURI
	 *        An optional base URI to use as the initial base URI for the
	 *        resulting RDFWriter. If null, then the default, if any, base URI,
	 *        for the given format must be used.
	 * @throws UnsupportedOperationException
	 *         if the RDFWriter the specific format does not support writing to a
	 *         {@link java.io.Writer}
	 * @since 2.8.0
	 */
	public RDFWriter getWriter(Writer writer, URI defaultBaseURI);
}
