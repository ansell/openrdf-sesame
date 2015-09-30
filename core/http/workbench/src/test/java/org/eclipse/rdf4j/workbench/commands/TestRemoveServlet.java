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

import static org.eclipse.rdf4j.workbench.base.TransformationServlet.CONTEXT;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.workbench.commands.RemoveServlet;
import org.eclipse.rdf4j.workbench.exceptions.BadRequestException;
import org.eclipse.rdf4j.workbench.util.WorkbenchRequest;
import org.junit.Test;

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