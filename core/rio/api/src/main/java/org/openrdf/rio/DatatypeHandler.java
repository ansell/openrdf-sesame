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
package org.openrdf.rio;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.LiteralUtilException;

/**
 * An interface defining methods related to verification and normalisation of
 * typed literals and datatype URIs.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public interface DatatypeHandler {

	public boolean isRecognisedDatatype(URI datatypeUri);

	public boolean verifyDatatype(String literalValue, URI datatypeUri)
		throws LiteralUtilException;

	public Literal normaliseDatatype(String literalValue, URI datatypeUri, ValueFactory valueFactory)
		throws LiteralUtilException;

	/**
	 * A unique key for this language handler to identify it in the
	 * DatatypeHandlerRegistry.
	 * 
	 * @return A unique string key.
	 */
	public String getKey();

}
