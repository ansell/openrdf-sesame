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
package org.openrdf.workbench.commands;

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

import org.junit.Before;
import org.junit.Test;

import info.aduna.io.ResourceUtil;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.QueryStorage;
import org.openrdf.workbench.util.WorkbenchRequest;

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
