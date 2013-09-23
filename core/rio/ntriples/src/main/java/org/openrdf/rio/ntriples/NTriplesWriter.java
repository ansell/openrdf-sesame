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
package org.openrdf.rio.ntriples;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.helpers.BasicWriterSettings;
import org.openrdf.rio.helpers.RDFWriterBase;

/**
 * An implementation of the RDFWriter interface that writes RDF documents in
 * N-Triples format. The N-Triples format is defined in <a
 * href="http://www.w3.org/TR/rdf-testcases/#ntriples">this section</a> of the
 * RDF Test Cases document.
 */
public class NTriplesWriter extends RDFWriterBase implements RDFWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected final Writer writer;

	protected boolean writingStarted;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new NTriplesWriter that will write to the supplied OutputStream.
	 * 
	 * @param out
	 *        The OutputStream to write the N-Triples document to.
	 */
	public NTriplesWriter(OutputStream out) {
		this(new OutputStreamWriter(out, Charset.forName("US-ASCII")));
	}

	/**
	 * Creates a new NTriplesWriter that will write to the supplied Writer.
	 * 
	 * @param writer
	 *        The Writer to write the N-Triples document to.
	 */
	public NTriplesWriter(Writer writer) {
		this.writer = writer;
		writingStarted = false;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public RDFFormat getRDFFormat() {
		return RDFFormat.NTRIPLES;
	}

	@Override
	public void startRDF()
		throws RDFHandlerException
	{
		if (writingStarted) {
			throw new RuntimeException("Document writing has already started");
		}

		writingStarted = true;
	}

	@Override
	public void endRDF()
		throws RDFHandlerException
	{
		if (!writingStarted) {
			throw new RuntimeException("Document writing has not yet started");
		}

		try {
			writer.flush();
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
		finally {
			writingStarted = false;
		}
	}

	@Override
	public void handleNamespace(String prefix, String name) {
		// N-Triples does not support namespace prefixes.
	}

	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		if (!writingStarted) {
			throw new RuntimeException("Document writing has not yet been started");
		}

		try {
			NTriplesUtil.append(st.getSubject(), writer);
			writer.write(" ");
			NTriplesUtil.append(st.getPredicate(), writer);
			writer.write(" ");
			NTriplesUtil.append(st.getObject(), writer,
					getWriterConfig().get(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL));
			writer.write(" .\n");
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	@Override
	public void handleComment(String comment)
		throws RDFHandlerException
	{
		try {
			writer.write("# ");
			writer.write(comment);
			writer.write("\n");
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	@Override
	public final Collection<RioSetting<?>> getSupportedSettings() {
		Set<RioSetting<?>> result = new HashSet<RioSetting<?>>(super.getSupportedSettings());

		result.add(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL);

		return result;
	}

	@Override
	public void handleBaseURI(String baseURI)
		throws RDFHandlerException
	{
		// ignore
	}
}
