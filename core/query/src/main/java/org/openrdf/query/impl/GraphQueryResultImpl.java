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
package org.openrdf.query.impl;

import java.util.Map;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Statement;
import org.openrdf.query.QueryEvaluationException;

/**
 * @deprecated since 4.0. Use {@link IteratingGraphQueryResult} instead.
 * @author Jeen Broekstra
 */
@Deprecated
public class GraphQueryResultImpl extends IteratingGraphQueryResult {

	public GraphQueryResultImpl(Map<String, String> namespaces,
			CloseableIteration<? extends Statement, ? extends QueryEvaluationException> statementIter)
	{
		super(namespaces, statementIter);
	}

}
