/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORY;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import info.aduna.io.IOUtil;

import org.openrdf.StoreException;
import org.openrdf.console.Console;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ModelImpl;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.ConfigTemplate;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class CreateServlet extends TransformationServlet {

	@Override
	protected void doPost(WorkbenchRequest req, HttpServletResponse resp,
			String xslPath) throws Exception {
		try {
			String id = createRepositoryConfig(req);
			resp.sendRedirect("../" + id + "/summary");
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(WorkbenchRequest req, HttpServletResponse resp,
			String xslPath) throws Exception {
		resp.setContentType("application/xml");
		TupleResultBuilder builder = new TupleResultBuilder(resp.getWriter());
		if (req.isParameterPresent("type")) {
			String type = req.getTypeParameter();
			builder.transform(xslPath, "create-" + type + ".xsl");
		} else {
			builder.transform(xslPath, "create.xsl");
		}
		builder.start();
		builder.link("info");
		builder.end();
	}

	private String createRepositoryConfig(WorkbenchRequest req)
			throws Exception {
		String type = req.getTypeParameter();
		ConfigTemplate template = getConfigTemplate(type);
		String configString = template.render(req.getSingleParameterMap());
		RepositoryConfig repConfig = updateRepositoryConfig(configString);
		return repConfig.getID();
	}

	private RepositoryConfig updateRepositoryConfig(String configString)
			throws IOException, RDFParseException, RDFHandlerException,
			GraphUtilException, RepositoryConfigException, StoreException {
		Repository systemRepo = manager.getSystemRepository();

		ValueFactory vf = systemRepo.getValueFactory();

		Model model = new ModelImpl();

		RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE, vf);
		rdfParser.setRDFHandler(new StatementCollector(model));
		rdfParser.parse(new StringReader(configString),
				RepositoryConfigSchema.NAMESPACE);

		Resource repositoryNode = model.subjects(RDF.TYPE, REPOSITORY).iterator().next();
		RepositoryConfig repConfig = RepositoryConfig.create(model,
				repositoryNode);
		repConfig.validate();

		RepositoryConfigUtil.updateRepositoryConfigs(systemRepo, repConfig);
		return repConfig;
	}

	private ConfigTemplate getConfigTemplate(String type) throws IOException,
			UnsupportedEncodingException {
		InputStream in = Console.class.getResourceAsStream(type + ".ttl");
		try {
			String template = IOUtil.readString(new InputStreamReader(in,
					"UTF-8"));
			return new ConfigTemplate(template);
		} finally {
			in.close();
		}
	}

}
