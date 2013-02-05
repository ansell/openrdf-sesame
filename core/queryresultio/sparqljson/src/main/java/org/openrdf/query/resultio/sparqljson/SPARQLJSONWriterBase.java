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
package org.openrdf.query.resultio.sparqljson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Iterator;

import info.aduna.io.IndentingWriter;
import info.aduna.text.StringUtil;

import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultFormat;
import org.openrdf.query.resultio.QueryResultWriter;

/**
 * An abstract class to implement the base functionality for both
 * SPARQLBooleanJSONWriter and SPARQLResultsJSONWriter.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
abstract class SPARQLJSONWriterBase implements QueryResultWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected IndentingWriter writer;

	protected boolean documentOpen = false;

	protected boolean headerComplete = false;

	public SPARQLJSONWriterBase(OutputStream out) {
		Writer w = new OutputStreamWriter(out, Charset.forName("UTF-8"));
		w = new BufferedWriter(w, 1024);
		writer = new IndentingWriter(w);
	}

	protected void endDocument()
		throws IOException
	{
		closeBraces(); // root braces
		writer.flush();
		documentOpen = false;
		headerComplete = false;
	}

	protected void writeKeyValue(String key, String value)
		throws IOException
	{
		writeKey(key);
		writeString(value);
	}

	protected void writeKeyValue(String key, Iterable<String> array)
		throws IOException
	{
		writeKey(key);
		writeArray(array);
	}

	protected void writeKey(String key)
		throws IOException
	{
		writeString(key);
		writer.write(": ");
	}

	protected void writeString(String value)
		throws IOException
	{
		// Escape special characters
		value = StringUtil.gsub("\\", "\\\\", value);
		value = StringUtil.gsub("\"", "\\\"", value);
		value = StringUtil.gsub("/", "\\/", value);
		value = StringUtil.gsub("\b", "\\b", value);
		value = StringUtil.gsub("\f", "\\f", value);
		value = StringUtil.gsub("\n", "\\n", value);
		value = StringUtil.gsub("\r", "\\r", value);
		value = StringUtil.gsub("\t", "\\t", value);

		writer.write("\"");
		writer.write(value);
		writer.write("\"");
	}

	protected void writeArray(Iterable<String> array)
		throws IOException
	{
		openArray();
		Iterator<String> iter = array.iterator();
		while (iter.hasNext()) {
			String value = iter.next();

			writeString(value);

			if (iter.hasNext()) {
				writer.write(", ");
			}
		}
		closeArray();
	}

	protected void openArray()
		throws IOException
	{
		writer.write("[");
		writer.writeEOL();
		writer.increaseIndentation();
	}

	protected void closeArray()
		throws IOException
	{
		writer.writeEOL();
		writer.decreaseIndentation();
		writer.write("]");
	}

	protected void openBraces()
		throws IOException
	{
		writer.write("{");
		writer.writeEOL();
		writer.increaseIndentation();
	}

	protected void closeBraces()
		throws IOException
	{
		writer.writeEOL();
		writer.decreaseIndentation();
		writer.write("}");
	}

	protected void writeComma()
		throws IOException
	{
		writer.write(", ");
		writer.writeEOL();
	}
}
