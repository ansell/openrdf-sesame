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
package org.openrdf.rio.rdfjson;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.helpers.BasicWriterSettings;
import org.openrdf.rio.helpers.RDFWriterBase;

/**
 * {@link RDFWriter} implementation for the RDF/JSON format
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class RDFJSONWriter extends RDFWriterBase implements RDFWriter {

	private Writer writer;

	private OutputStream outputStream;

	private Model graph;

	private final RDFFormat actualFormat;

	public RDFJSONWriter(final OutputStream out, final RDFFormat actualFormat) {
		this.outputStream = out;
		this.actualFormat = actualFormat;
	}

	public RDFJSONWriter(final Writer writer, final RDFFormat actualFormat) {
		this.writer = writer;
		this.actualFormat = actualFormat;
	}

	@Override
	public void endRDF()
		throws RDFHandlerException
	{
		try {
			if (this.writer != null) {
				RDFJSONUtility.modelToRdfJson(this.graph, this.writer, this.getWriterConfig());
				this.writer.flush();
			}
			else if (this.outputStream != null) {
				RDFJSONUtility.modelToRdfJson(this.graph, this.outputStream, this.getWriterConfig());
				this.outputStream.flush();
			}
			else {
				throw new IllegalStateException("The output stream and the writer were both null.");
			}
		}
		catch (final IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	@Override
	public RDFFormat getRDFFormat() {
		return this.actualFormat;
	}

	@Override
	public Collection<RioSetting<?>> getSupportedSettings() {
		final Set<RioSetting<?>> results = new HashSet<RioSetting<?>>(super.getSupportedSettings());

		results.add(BasicWriterSettings.PRETTY_PRINT);

		return results;
	}

	@Override
	public void handleComment(final String comment)
		throws RDFHandlerException
	{
		// Comments are ignored.
	}

	@Override
	public void handleNamespace(final String prefix, final String uri)
		throws RDFHandlerException
	{
		// Namespace prefixes are not used in RDF/JSON.
	}

	@Override
	public void handleStatement(final Statement statement)
		throws RDFHandlerException
	{
		this.graph.add(statement);
	}

	@Override
	public void startRDF()
		throws RDFHandlerException
	{
		this.graph = new TreeModel();
	}

}
