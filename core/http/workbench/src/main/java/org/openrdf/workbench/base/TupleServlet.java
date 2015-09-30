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
package org.openrdf.workbench.base;

import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import info.aduna.iteration.Iterations;

import org.openrdf.model.Namespace;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public abstract class TupleServlet extends TransformationServlet {

	protected String xsl;

	protected String[] variables;

	public TupleServlet(String xsl, String... variables) {
		super();
		this.xsl = xsl;
		this.variables = variables;
	}

	@Override
	protected void service(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception
	{
		TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
		RepositoryConnection con = repository.getConnection();
		con.setParserConfig(NON_VERIFYING_PARSER_CONFIG);
		try {
			for (Namespace ns : Iterations.asList(con.getNamespaces())) {
				builder.prefix(ns.getPrefix(), ns.getName());
			}
			if (xsl != null) {
				builder.transform(xslPath, xsl);
			}
			builder.start(variables);
			builder.link(Arrays.asList("info"));
			this.service(req, resp, builder, con);
			builder.end();
		}
		finally {
			con.close();
		}
	}

	protected void service(WorkbenchRequest req, HttpServletResponse resp, TupleResultBuilder builder,
			RepositoryConnection con)
		throws Exception
	{
		service(builder, con);
	}

	protected void service(TupleResultBuilder builder, RepositoryConnection con)
		throws Exception
	{
	}
}