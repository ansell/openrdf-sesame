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
package org.openrdf.query.resultio.text;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQueryResultHandlerException;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.AbstractQueryResultWriter;

/**
 * Writer for the plain text boolean result format.
 * 
 * @author Arjohn Kampman
 */
public class BooleanTextWriter extends AbstractQueryResultWriter implements BooleanQueryResultWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The writer to write the boolean result to.
	 */
	private Writer writer;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BooleanTextWriter(OutputStream out) {
		writer = new OutputStreamWriter(out, Charset.forName("US-ASCII"));
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public final BooleanQueryResultFormat getBooleanQueryResultFormat() {
		return BooleanQueryResultFormat.TEXT;
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

	@Override
	public void handleBoolean(boolean value)
		throws QueryResultHandlerException
	{
		try {
			writer.write(Boolean.toString(value));
			writer.flush();
		}
		catch (IOException e) {
			throw new BooleanQueryResultHandlerException(e);
		}
	}

	@Override
	public void startDocument()
		throws QueryResultHandlerException
	{
		// Ignored by BooleanTextWriter
	}

	@Override
	public void handleStylesheet(String stylesheetUrl)
		throws QueryResultHandlerException
	{
		// Ignored by BooleanTextWriter
	}

	@Override
	public void startHeader()
		throws QueryResultHandlerException
	{
		// Ignored by BooleanTextWriter
	}

	@Override
	public void handleLinks(List<String> linkUrls)
		throws QueryResultHandlerException
	{
		// Ignored by BooleanTextWriter
	}

	@Override
	public void endHeader()
		throws QueryResultHandlerException
	{
		// Ignored by BooleanTextWriter
	}

	@Override
	public void startQueryResult(List<String> bindingNames)
		throws TupleQueryResultHandlerException
	{
		throw new UnsupportedOperationException("Cannot handle tuple results");
	}

	@Override
	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		throw new UnsupportedOperationException("Cannot handle tuple results");
	}

	@Override
	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		throw new UnsupportedOperationException("Cannot handle tuple results");
	}

	@Override
	public void handleNamespace(String prefix, String uri)
		throws QueryResultHandlerException
	{
		// Ignored by BooleanTextWriter
	}
}
