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

import java.io.OutputStream;

import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.BooleanQueryResultWriterFactory;

/**
 * A {@link BooleanQueryResultWriterFactory} for writers of plain text boolean
 * query results.
 * 
 * @author Arjohn Kampman
 */
public class BooleanTextWriterFactory implements BooleanQueryResultWriterFactory {

	/**
	 * Returns {@link BooleanQueryResultFormat#TEXT}.
	 */
	public BooleanQueryResultFormat getBooleanQueryResultFormat() {
		return BooleanQueryResultFormat.TEXT;
	}

	/**
	 * Returns a new instance of BinaryQueryResultWriter.
	 */
	public BooleanQueryResultWriter getWriter(OutputStream out) {
		return new BooleanTextWriter(out);
	}
}
