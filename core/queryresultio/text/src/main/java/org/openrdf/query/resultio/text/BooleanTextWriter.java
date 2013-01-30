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

import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;

/**
 * Writer for the plain text boolean result format.
 * 
 * @author Arjohn Kampman
 */
public class BooleanTextWriter implements BooleanQueryResultWriter {

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
	public void write(boolean value)
		throws IOException
	{
		writer.write(Boolean.toString(value));
		writer.flush();
	}

	@Override
	public void startDocument()
		throws IOException
	{
		// Ignored by BooleanTextWriter
	}

	@Override
	public void handleStylesheet(String stylesheetUrl)
		throws IOException
	{
		// Ignored by BooleanTextWriter
	}

	@Override
	public void startHeader()
		throws IOException
	{
		// Ignored by BooleanTextWriter
	}

	@Override
	public void handleLinks(List<String> linkUrls)
		throws IOException
	{
		// Ignored by BooleanTextWriter
	}

	@Override
	public void endHeader()
		throws IOException
	{
		// Ignored by BooleanTextWriter
	}
}
