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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openrdf.workbench.base.TransformationServlet.CONTEXT;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import org.openrdf.model.IRI;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.WorkbenchRequest;

/**
 * Unit and regression tests for {@link RemoteServlet}.
 * 
 * @author Dale Visser
 */
public class TestRemoveServlet {

	private final RemoveServlet servlet = new RemoveServlet();

	@Test
	public void testSES1958regression()
		throws RepositoryException, QueryResultHandlerException, IOException, BadRequestException
	{
		WorkbenchRequest request = mock(WorkbenchRequest.class);
		when(request.isParameterPresent(CONTEXT)).thenReturn(true);
		IRI context = SimpleValueFactory.getInstance().createIRI("<http://foo.org/bar>");
		when(request.getResource(CONTEXT)).thenReturn(context);
		Repository repository = mock(Repository.class);
		servlet.setRepository(repository);
		RepositoryConnection connection = mock(RepositoryConnection.class);
		when(repository.getConnection()).thenReturn(connection);
		servlet.doPost(request, mock(HttpServletResponse.class), "");
		verify(connection).clear(eq(context));
	}
}