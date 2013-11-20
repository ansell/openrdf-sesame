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
package org.openrdf.workbench.util;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import org.openrdf.query.QueryLanguage;

/**
 * Regression test suite for {@link org.openrdf.workbench.util.PagedQuery
 * PagedQuery}.
 * 
 * @author Dale Visser
 */
public class TestPagedQuery {

	@Test
	public final void testSES1895regression() {
		PagedQuery pagedQuery = new PagedQuery("select * {?s ?p ?o } LIMIT 10", QueryLanguage.SPARQL, 100, 0);
		assertThat(pagedQuery.toString().toLowerCase(), is(equalTo("select * {?s ?p ?o } limit 10")));
	}
}
