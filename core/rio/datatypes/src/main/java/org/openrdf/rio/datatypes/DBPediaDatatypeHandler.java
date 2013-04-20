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
package org.openrdf.rio.datatypes;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.LiteralUtilException;
import org.openrdf.rio.DatatypeHandler;

/**
 * An implementation of a datatype handler that can process DBPedia datatypes.
 * 
 * @author Peter Ansell
 * @since 2.7.1
 */
public class DBPediaDatatypeHandler implements DatatypeHandler {

	/**
	 * Default constructor.
	 */
	public DBPediaDatatypeHandler() {
	}

	@Override
	public boolean isRecognizedDatatype(URI datatypeUri) {
		return datatypeUri.stringValue().startsWith("http://dbpedia.org/datatype/");
	}

	@Override
	public boolean verifyDatatype(String literalValue, URI datatypeUri)
		throws LiteralUtilException
	{
		if (isRecognizedDatatype(datatypeUri)) {
			// TODO: Implement verification
			return true;
		}
		
		throw new LiteralUtilException("Could not verify DBPedia literal");
	}

	@Override
	public Literal normalizeDatatype(String literalValue, URI datatypeUri, ValueFactory valueFactory)
		throws LiteralUtilException
	{
		if(isRecognizedDatatype(datatypeUri)) {
			return valueFactory.createLiteral(literalValue, datatypeUri);
		}
		
		throw new LiteralUtilException("Could not normalise DBPedia literal");
	}

	@Override
	public String getKey() {
		return DatatypeHandler.DBPEDIA;
	}
}
