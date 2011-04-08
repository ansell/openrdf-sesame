/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository.statements;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import org.openrdf.http.server.ServerHTTPException;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

/**
 * View used to export statements. Renders the statements as RDF using a
 * serialization specified using a parameter or Accept header.
 * 
 * @author Herko ter Horst
 */
public class ExportStatementsView implements View {

	public static final String SUBJECT_KEY = "subject";

	public static final String PREDICATE_KEY = "predicate";

	public static final String OBJECT_KEY = "object";

	public static final String CONTEXTS_KEY = "contexts";

	public static final String USE_INFERENCING_KEY = "useInferencing";

	public static final String FACTORY_KEY = "factory";

	private static final ExportStatementsView INSTANCE = new ExportStatementsView();

	public static ExportStatementsView getInstance() {
		return INSTANCE;
	}

	private ExportStatementsView() {
	}

	public String getContentType() {
		return null;
	}

	@SuppressWarnings("rawtypes")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);

		Resource subj = (Resource)model.get(SUBJECT_KEY);
		URI pred = (URI)model.get(PREDICATE_KEY);
		Value obj = (Value)model.get(OBJECT_KEY);
		Resource[] contexts = (Resource[])model.get(CONTEXTS_KEY);
		boolean useInferencing = (Boolean)model.get(USE_INFERENCING_KEY);

		RDFWriterFactory rdfWriterFactory = (RDFWriterFactory)model.get(FACTORY_KEY);

		RDFFormat rdfFormat = rdfWriterFactory.getRDFFormat();

		try {
			OutputStream out = response.getOutputStream();
			RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);

			response.setStatus(SC_OK);

			String mimeType = rdfFormat.getDefaultMIMEType();
			if (rdfFormat.hasCharset()) {
				Charset charset = rdfFormat.getCharset();
				mimeType += "; charset=" + charset.name();
			}
			response.setContentType(mimeType);

			String filename = "statements";
			if (rdfFormat.getDefaultFileExtension() != null) {
				filename += "." + rdfFormat.getDefaultFileExtension();
			}
			response.setHeader("Content-Disposition", "attachment; filename=" + filename);

			repositoryCon.exportStatements(subj, pred, obj, useInferencing, rdfWriter, contexts);

			out.close();
		}
		catch (RDFHandlerException e) {
			throw new ServerHTTPException("Serialization error: " + e.getMessage(), e);
		}
		catch (RepositoryException e) {
			throw new ServerHTTPException("Repository error: " + e.getMessage(), e);
		}
	}
}
