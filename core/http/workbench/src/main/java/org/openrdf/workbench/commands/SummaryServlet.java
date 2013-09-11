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

import java.util.Arrays;

import info.aduna.iteration.Iterations;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;

public class SummaryServlet extends TransformationServlet {

	@Override
	public void service(TupleResultBuilder builder, String xslPath)
		throws RepositoryException, QueryEvaluationException, MalformedQueryException,
		QueryResultHandlerException
	{
		builder.transform(xslPath, "summary.xsl");
		builder.start("id", "description", "location", "server", "size", "contexts");
		builder.link(Arrays.asList(INFO));
		RepositoryConnection con = repository.getConnection();
		try {
			builder.result(info.getId(), info.getDescription(), info.getLocation(), getServer(), con.size(),
					Iterations.asList(con.getContextIDs()).size());
			builder.end();
		}
		finally {
			con.close();
		}
	}

	private String getServer() {
		String result = null; // gracefully ignored by builder.result(...)
		if (manager instanceof LocalRepositoryManager) {
			result = ((LocalRepositoryManager)manager).getBaseDir().toString();
		}
		else if (manager instanceof RemoteRepositoryManager) {
			result = ((RemoteRepositoryManager)manager).getServerURL();
		}
		return result;
	}
}