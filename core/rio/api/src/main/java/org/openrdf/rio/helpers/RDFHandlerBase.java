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
package org.openrdf.rio.helpers;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * Base class for {@link RDFHandler}s with dummy implementations of all methods.
 */
public class RDFHandlerBase implements RDFHandler {

	public void startRDF()
		throws RDFHandlerException
	{
	}

	public void endRDF()
		throws RDFHandlerException
	{
	}

	public void handleNamespace(String prefix, String uri)
		throws RDFHandlerException
	{
	}

	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
	}

	public void handleComment(String comment)
		throws RDFHandlerException
	{
	}

	@Override
	public void handleBaseURI(URI baseURI)
		throws RDFHandlerException
	{
	}
}
