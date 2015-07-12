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
import org.openrdf.query.resultio.QueryResultWriterBase;

/**
 * Writer for the plain text boolean result format.
 * 
 * @author Arjohn Kampman
 */
public class BooleanTextWriter extends QueryResultWriterBase implements BooleanQueryResultWriter {

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
