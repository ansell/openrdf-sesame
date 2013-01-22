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
package org.openrdf.queryrender.serql;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.queryrender.QueryRenderer;

/**
 * <p>
 * Implementation of the {@link QueryRenderer} interface which renders
 * {@link org.openrdf.query.parser.ParsedQuery} objects as strings in SeRQL
 * syntax
 * </p>
 * 
 * @author Michael Grove
 * @since 2.7.0
 */
public class SeRQLQueryRenderer implements QueryRenderer {

	public final static boolean SERQL_ONE_X_COMPATIBILITY_MODE = false;

	/**
	 * The renderer object
	 */
	private SerqlTupleExprRenderer mRenderer = new SerqlTupleExprRenderer();

	/**
	 * @inheritDoc
	 */
	public QueryLanguage getLanguage() {
		return QueryLanguage.SERQL;
	}

	/**
	 * @inheritDoc
	 */
	public String render(final ParsedQuery theQuery)
		throws Exception
	{
		mRenderer.reset();

		return mRenderer.render(theQuery.getTupleExpr());
	}
}
