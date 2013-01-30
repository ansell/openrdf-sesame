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
package org.openrdf.queryrender;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedQuery;

/**
 * <p>
 * Interface for Sesame-based query renderers
 * </p>
 * 
 * @author Michael Grove
 * @since 2.7.0
 */
public interface QueryRenderer {

	/**
	 * Return the language this QueryRenderer will render queries in.
	 * 
	 * @return the query language
	 */
	public QueryLanguage getLanguage();

	/**
	 * Render the query object to a string in the language supported by this
	 * renderer
	 * 
	 * @param theQuery
	 *        the query to render
	 * @return the rendered query
	 * @throws Exception
	 *         if there is an error while rendering
	 */
	public String render(ParsedQuery theQuery)
		throws Exception;
}
