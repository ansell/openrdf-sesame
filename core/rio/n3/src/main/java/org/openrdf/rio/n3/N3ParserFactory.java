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
package org.openrdf.rio.n3;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.turtle.TurtleParser;

/**
 * An {@link RDFParserFactory} for N3 parsers.
 * 
 * @author Arjohn Kampman
 */
public class N3ParserFactory implements RDFParserFactory {

	/**
	 * Returns {@link RDFFormat#N3}.
	 */
	public RDFFormat getRDFFormat() {
		return RDFFormat.N3;
	}

	/**
	 * Returns a new instance of {@link TurtleParser}.
	 */
	public RDFParser getParser() {
		return new TurtleParser();
	}
}
