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
package org.eclipse.rdf4j.query.resultio.sparqlxml;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.rdf4j.common.xml.XMLWriter;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultWriter;

/**
 * A {@link BooleanQueryResultWriter} that writes boolean query results in the
 * <a href="http://www.w3.org/TR/rdf-sparql-XMLres/">SPARQL Query Results XML
 * Format</a>.
 */
public class SPARQLBooleanXMLWriter extends AbstractSPARQLXMLWriter implements BooleanQueryResultWriter {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SPARQLBooleanXMLWriter(OutputStream out) {
		super(out);
	}

	public SPARQLBooleanXMLWriter(XMLWriter xmlWriter) {
		super(xmlWriter);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public final BooleanQueryResultFormat getBooleanQueryResultFormat() {
		return BooleanQueryResultFormat.SPARQL;
	}

	@Override
	public final BooleanQueryResultFormat getQueryResultFormat() {
		return getBooleanQueryResultFormat();
	}

	@Override
	public void write(boolean value)
		throws IOException
	{
		try {
			handleBoolean(value);
		}
		catch (QueryResultHandlerException e) {
			if (e.getCause() != null && e.getCause() instanceof IOException) {
				throw (IOException)e.getCause();
			}
			else {
				throw new IOException(e);
			}
		}
	}

}
