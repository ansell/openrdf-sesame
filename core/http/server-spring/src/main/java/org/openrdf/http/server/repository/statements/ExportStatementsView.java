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

import info.aduna.iteration.CloseableIteration;

import org.openrdf.http.server.ServerHTTPException;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.store.StoreException;

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

	public static final String LIMIT = "limit";

	private static final ExportStatementsView INSTANCE = new ExportStatementsView();

	public static ExportStatementsView getInstance() {
		return INSTANCE;
	}

	private ExportStatementsView() {
	}

	public String getContentType() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);

		Resource subj = (Resource)model.get(SUBJECT_KEY);
		URI pred = (URI)model.get(PREDICATE_KEY);
		Value obj = (Value)model.get(OBJECT_KEY);
		Resource[] contexts = (Resource[])model.get(CONTEXTS_KEY);
		boolean useInferencing = (Boolean)model.get(USE_INFERENCING_KEY);
		Integer limit = (Integer)model.get(LIMIT);

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

			exportStatements(repositoryCon, subj, pred, obj, useInferencing, rdfWriter, contexts, limit);

			out.close();
		}
		catch (RDFHandlerException e) {
			throw new ServerHTTPException("Serialization error: " + e.getMessage(), e);
		}
		catch (StoreException e) {
			throw new ServerHTTPException("Repository error: " + e.getMessage(), e);
		}
	}

	/**
	 * @param repositoryCon
	 * @param subj
	 * @param pred
	 * @param obj
	 * @param useInferencing
	 * @param rdfWriter
	 * @param contexts
	 */
	private void exportStatements(RepositoryConnection con, Resource subj, URI pred, Value obj,
			boolean includeInferred, RDFWriter handler, Resource[] contexts, Integer limit)
	throws StoreException, RDFHandlerException
	{

		handler.startRDF();

		// Export namespace information
		CloseableIteration<? extends Namespace, StoreException> nsIter = con.getNamespaces();
		try {
			while (nsIter.hasNext()) {
				Namespace ns = nsIter.next();
				handler.handleNamespace(ns.getPrefix(), ns.getName());
			}
		}
		finally {
			nsIter.close();
		}

		// Export statements
		CloseableIteration<? extends Statement, StoreException> stIter = con.getStatements(subj, pred, obj,
				includeInferred, contexts);

		try {
			for (int i = 0; stIter.hasNext() && (limit == null || i < limit.intValue()); i++) {
				handler.handleStatement(stIter.next());
			}
		}
		finally {
			stIter.close();
		}

		handler.endRDF();
	}
}
