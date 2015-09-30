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
package org.openrdf.workbench.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import info.aduna.io.IOUtil;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.util.Models;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.ConfigTemplate;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.SystemRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.runtime.RepositoryManagerFederator;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class CreateServlet extends TransformationServlet {

	private RepositoryManagerFederator rmf;

	@Override
	public void init(final ServletConfig config)
		throws ServletException
	{
		super.init(config);
		this.rmf = new RepositoryManagerFederator(manager);
	}

	/**
	 * POST requests to this servlet come from the various specific create-* form
	 * submissions.
	 */
	@Override
	protected void doPost(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
		throws ServletException
	{
		try {
			resp.sendRedirect("../" + createRepositoryConfig(req) + "/summary");
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
	}

	/**
	 * GET requests to this servlet come from the Workbench side bar or from
	 * create.xsl form submissions.
	 * 
	 * @throws RepositoryException
	 * @throws QueryResultHandlerException
	 */
	@Override
	protected void service(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
		throws IOException, RepositoryException, QueryResultHandlerException
	{
		final TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
		boolean federate;
		if (req.isParameterPresent("type")) {
			final String type = req.getTypeParameter();
			federate = "federate".equals(type);
			builder.transform(xslPath, "create-" + type + ".xsl");
		}
		else {
			federate = false;
			builder.transform(xslPath, "create.xsl");
		}
		builder.start(federate ? new String[] { "id", "description", "location" } : new String[] {});
		builder.link(Arrays.asList(INFO));
		if (federate) {
			for (RepositoryInfo info : manager.getAllRepositoryInfos()) {
				String identity = info.getId();
				if (!SystemRepository.ID.equals(identity)) {
					builder.result(identity, info.getDescription(), info.getLocation());
				}
			}
		}
		builder.end();
	}

	private String createRepositoryConfig(final WorkbenchRequest req)
		throws IOException, OpenRDFException
	{
		String type = req.getTypeParameter();
		String newID;
		if ("federate".equals(type)) {
			newID = req.getParameter("Local repository ID");
			rmf.addFed(newID, req.getParameter("Repository title"),
					Arrays.asList(req.getParameterValues("memberID")),
					Boolean.parseBoolean(req.getParameter("readonly")),
					Boolean.parseBoolean(req.getParameter("distinct")));
		}
		else {
			newID = updateRepositoryConfig(getConfigTemplate(type).render(req.getSingleParameterMap())).getID();
		}
		return newID;
	}

	private RepositoryConfig updateRepositoryConfig(final String configString)
		throws IOException, OpenRDFException
	{
		final Repository systemRepo = manager.getSystemRepository();
		final Model graph = new LinkedHashModel();
		final RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE, systemRepo.getValueFactory());
		rdfParser.setRDFHandler(new StatementCollector(graph));
		rdfParser.parse(new StringReader(configString), RepositoryConfigSchema.NAMESPACE);

		Resource res = Models.subject(
				graph.filter(null, RDF.TYPE, RepositoryConfigSchema.REPOSITORY)).orElseThrow(
						() -> new RepositoryException("could not find instance of Repository class in config"));
		final RepositoryConfig repConfig = RepositoryConfig.create(graph, res);
		repConfig.validate();
		RepositoryConfigUtil.updateRepositoryConfigs(systemRepo, repConfig);
		return repConfig;
	}

	private ConfigTemplate getConfigTemplate(final String type)
		throws IOException
	{
		final InputStream ttlInput = RepositoryConfig.class.getResourceAsStream(type + ".ttl");
		try {
			final String template = IOUtil.readString(new InputStreamReader(ttlInput, "UTF-8"));
			return new ConfigTemplate(template);
		}
		finally {
			ttlInput.close();
		}
	}
}
