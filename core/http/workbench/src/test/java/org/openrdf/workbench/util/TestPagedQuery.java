/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.workbench.util;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import info.aduna.io.ResourceUtil;

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
		PagedQuery pagedQuery = new PagedQuery(
				"select * {?s ?p ?o } LIMIT 10",
				QueryLanguage.SPARQL,
				100,
				0);
		assertThat(pagedQuery.toString().toLowerCase(),
				is(equalTo("select * {?s ?p ?o } limit 10")));
	}
	
	/**
	 * Check that inner query limits do not affect the paging parameters.
	 * @throws IOException 
	 */
	@Test
	public final void testSES2307regression() throws IOException{
		PagedQuery pagedQuery = new PagedQuery(
				ResourceUtil.getString("ses2307.rq"),
				QueryLanguage.SPARQL,
				100,
				0);
		assertThat(pagedQuery.getLimit(), is(equalTo(100)));
	}
}
