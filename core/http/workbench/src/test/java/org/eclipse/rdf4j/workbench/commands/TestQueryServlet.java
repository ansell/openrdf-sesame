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
package org.eclipse.rdf4j.workbench.commands;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletException;

import org.eclipse.rdf4j.OpenRDFException;
import org.eclipse.rdf4j.common.io.ResourceUtil;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.workbench.commands.QueryServlet;
import org.eclipse.rdf4j.workbench.exceptions.BadRequestException;
import org.eclipse.rdf4j.workbench.util.QueryStorage;
import org.eclipse.rdf4j.workbench.util.WorkbenchRequest;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Dale Visser
 */
public class TestQueryServlet {

	private static final String SHORT_QUERY = "select * {?s ?p ?o .}";

	private final QueryServlet servlet = new QueryServlet();

	private String longQuery;

	@Before
	public void setUp()
		throws IOException
	{
		longQuery = ResourceUtil.getString("long.rq");
	}

	@Test
	public final void testLongQuery()
		throws ServletException, IOException
	{
		assertThat(servlet.shouldWriteQueryCookie(longQuery), is(equalTo(false)));
	}

	@Test
	public final void testShortQuery()
		throws ServletException, IOException
	{
		assertThat(servlet.shouldWriteQueryCookie(SHORT_QUERY), is(equalTo(true)));
	}

	@Test
	public final void testNoQuery()
		throws ServletException, IOException
	{
		assertThat(servlet.shouldWriteQueryCookie(null), is(equalTo(true)));
	}

	@Test
	public void testGetQueryTextRefText()
		throws BadRequestException, OpenRDFException
	{
		WorkbenchRequest request = mock(WorkbenchRequest.class);
		when(request.isParameterPresent(QueryServlet.QUERY)).thenReturn(true);
		when(request.getParameter(QueryServlet.QUERY)).thenReturn(SHORT_QUERY);
		when(request.isParameterPresent(QueryServlet.REF)).thenReturn(true);
		when(request.getParameter(QueryServlet.REF)).thenReturn("text");
		assertThat(servlet.getQueryText(request), is(equalTo(SHORT_QUERY)));
	}

	@Test
	public void testGetQueryNoQuery()
		throws BadRequestException, OpenRDFException
	{
		WorkbenchRequest request = mock(WorkbenchRequest.class);
		when(request.isParameterPresent(QueryServlet.QUERY)).thenReturn(false);
		assertThat(servlet.getQueryText(request), is(equalTo("")));
	}
	
	@Test
	public void testGetQueryTextUnrecognizedRef()
		throws BadRequestException, OpenRDFException
	{
		WorkbenchRequest request = mock(WorkbenchRequest.class);
		when(request.isParameterPresent(QueryServlet.QUERY)).thenReturn(true);
		when(request.getParameter(QueryServlet.QUERY)).thenReturn(SHORT_QUERY);
		when(request.isParameterPresent(QueryServlet.REF)).thenReturn(true);
		when(request.getParameter(QueryServlet.REF)).thenReturn("junk");
		assertThat(servlet.getQueryText(request), is(equalTo(SHORT_QUERY)));
	}

	@Test
	public void testGetQueryTextNoRef()
		throws BadRequestException, OpenRDFException
	{
		WorkbenchRequest request = mock(WorkbenchRequest.class);
		when(request.isParameterPresent(QueryServlet.QUERY)).thenReturn(true);
		when(request.getParameter(QueryServlet.QUERY)).thenReturn(SHORT_QUERY);
		when(request.isParameterPresent(QueryServlet.REF)).thenReturn(false);
		assertThat(servlet.getQueryText(request), is(equalTo(SHORT_QUERY)));
	}

	@Test
	public void testGetQueryTextRefHash()
		throws BadRequestException, OpenRDFException
	{
		WorkbenchRequest request = mock(WorkbenchRequest.class);
		when(request.isParameterPresent(QueryServlet.QUERY)).thenReturn(true);
		String hash = String.valueOf(longQuery.hashCode());
		when(request.getParameter(QueryServlet.QUERY)).thenReturn(hash);
		when(request.isParameterPresent(QueryServlet.REF)).thenReturn(true);
		when(request.getParameter(QueryServlet.REF)).thenReturn("hash");
		QueryServlet.substituteQueryCache(Collections.singletonMap(hash, longQuery));
		assertThat(servlet.getQueryText(request), is(equalTo(longQuery)));
	}

	@Test
	public void testGetQueryTextRefHashNoEntry()
		throws BadRequestException, OpenRDFException
	{
		WorkbenchRequest request = mock(WorkbenchRequest.class);
		when(request.isParameterPresent(QueryServlet.QUERY)).thenReturn(true);
		String hash = String.valueOf(longQuery.hashCode());
		when(request.getParameter(QueryServlet.QUERY)).thenReturn(hash);
		when(request.isParameterPresent(QueryServlet.REF)).thenReturn(true);
		when(request.getParameter(QueryServlet.REF)).thenReturn("hash");
		QueryServlet.substituteQueryCache(Collections.<String, String> emptyMap());
		assertThat(servlet.getQueryText(request), is(equalTo("")));
	}

	@Test
	public void testGetQueryTextRefId()
		throws BadRequestException, OpenRDFException
	{
		WorkbenchRequest request = mock(WorkbenchRequest.class);
		when(request.isParameterPresent(QueryServlet.QUERY)).thenReturn(true);
		when(request.getParameter(QueryServlet.QUERY)).thenReturn("test save name");
		when(request.isParameterPresent(QueryServlet.REF)).thenReturn(true);
		when(request.getParameter(QueryServlet.REF)).thenReturn("id");
		QueryStorage storage = mock(QueryStorage.class);
		when(storage.getQueryText(any(HTTPRepository.class), anyString(), eq("test save name"))).thenReturn(
				longQuery);
		servlet.substituteQueryStorage(storage);
		assertThat(servlet.getQueryText(request), is(equalTo(longQuery)));
	}
}
