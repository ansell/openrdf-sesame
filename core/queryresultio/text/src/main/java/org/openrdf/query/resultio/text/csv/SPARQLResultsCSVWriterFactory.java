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
package org.openrdf.query.resultio.text.csv;

import java.io.OutputStream;

import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;

/**
 * A {@link TupleQueryResultWriterFactory} for writers of SPARQL/CSV tuple query
 * results.
 * 
 * @author Jeen Broekstra
 */
public class SPARQLResultsCSVWriterFactory implements TupleQueryResultWriterFactory {

	/**
	 * Returns {@link TupleQueryResultFormat#CSV}.
	 */
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.CSV;
	}

	/**
	 * Returns a new instance of {@link SPARQLResultsCSVWriter}.
	 */
	public TupleQueryResultWriter getWriter(OutputStream out) {
		return new SPARQLResultsCSVWriter(out);
	}
}
