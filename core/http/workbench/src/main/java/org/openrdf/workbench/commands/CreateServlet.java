/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import info.aduna.io.IOUtil;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Graph;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.ConfigTemplate;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class CreateServlet extends TransformationServlet {

	@Override
	protected void doPost(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
		throws Exception
	{
		try {
			resp.sendRedirect("../" + createRepositoryConfig(req) + "/summary");
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
		throws IOException
	{
		resp.setContentType("application/xml");
		final TupleResultBuilder builder = new TupleResultBuilder(resp.getWriter());
		if (req.isParameterPresent("type")) {
			final String type = req.getTypeParameter();
			builder.transform(xslPath, "create-" + type + ".xsl");
		}
		else {
			builder.transform(xslPath, "create.xsl");
		}
		builder.start();
		builder.link("info");
		builder.end();
	}

	private String createRepositoryConfig(final WorkbenchRequest req)
		throws IOException, OpenRDFException
	{
		final String type = req.getTypeParameter();
		final String configString = getConfigTemplate(type).render(req.getSingleParameterMap());
		final RepositoryConfig repConfig = updateRepositoryConfig(configString);
		return repConfig.getID();
	}

	private RepositoryConfig updateRepositoryConfig(final String configString)
		throws IOException, OpenRDFException
	{
		final Repository systemRepo = manager.getSystemRepository();
		final ValueFactory factory = systemRepo.getValueFactory();
		final Graph graph = new GraphImpl(factory);
		final RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE, factory);
		rdfParser.setRDFHandler(new StatementCollector(graph));
		rdfParser.parse(new StringReader(configString), RepositoryConfigSchema.NAMESPACE);
		final RepositoryConfig repConfig = RepositoryConfig.create(graph,
				GraphUtil.getUniqueSubject(graph, RDF.TYPE, RepositoryConfigSchema.REPOSITORY));
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