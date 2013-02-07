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

import static org.openrdf.query.QueryLanguage.SERQL;

import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class DeleteServlet extends TransformationServlet {

	/**
	 * Query that yields the context of a specific repository configuration.
	 */
	public static final String CONTEXT_QUERY;

	static {
		StringBuilder query = new StringBuilder(256);
		query.append("SELECT C ");
		query.append("FROM CONTEXT C ");
		query.append("   {} rdf:type {sys:Repository};");
		query.append("      sys:repositoryID {ID} ");
		query.append("USING NAMESPACE sys = <http://www.openrdf.org/config/repository#>");
		CONTEXT_QUERY = query.toString();
	}

	@Override
	protected void doPost(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception
	{
		dropRepository(req.getParameter("id"));
		resp.sendRedirect("../");
	}

	private void dropRepository(String id)
		throws Exception
	{

		manager.removeRepository(id);

		/*
		Repository systemRepo = manager.getSystemRepository();
		RepositoryConnection con = systemRepo.getConnection();
		try {
			Resource context = findContext(id, con);
			manager.getRepository(id).shutDown();
			con.clear(context);
		} finally {
			con.close();
		}
		*/
	}

	private Resource findContext(String id, RepositoryConnection con)
		throws RepositoryException, MalformedQueryException, QueryEvaluationException, BadRequestException
	{
		TupleQuery query = con.prepareTupleQuery(SERQL, CONTEXT_QUERY);
		query.setBinding("ID", vf.createLiteral(id));
		TupleQueryResult result = query.evaluate();
		try {
			if (!result.hasNext())
				throw new BadRequestException("Cannot find repository of id: " + id);
			BindingSet bindings = result.next();
			Resource context = (Resource)bindings.getValue("C");
			if (result.hasNext())
				throw new BadRequestException("Multiple contexts found for repository '" + id + "'");
			return context;
		}
		finally {
			result.close();
		}
	}

	@Override
	public void service(TupleResultBuilder builder, String xslPath)
		throws RepositoryException, QueryResultHandlerException
	{
		// TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.transform(xslPath, "delete.xsl");
		builder.start("readable", "writeable", "id", "description", "location");
		builder.link(Arrays.asList(INFO));
		for (RepositoryInfo info : manager.getAllRepositoryInfos()) {
			builder.result(info.isReadable(), info.isWritable(), info.getId(), info.getDescription(),
					info.getLocation());
		}
		builder.end();
	}

}
