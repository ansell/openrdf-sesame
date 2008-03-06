/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository.statements;

import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.openrdf.http.protocol.Protocol.CONTEXT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.INCLUDE_INFERRED_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.OBJECT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.PREDICATE_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.SUBJECT_PARAM_NAME;

import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import org.openrdf.http.server.ClientRequestException;
import org.openrdf.http.server.InternalServerException;
import org.openrdf.http.server.ProtocolUtil;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.UnsupportedRDFormatException;

/**
 * View used to export statements. Renders the statements as RDF using a
 * serialization specified using a parameter or Accept header.
 * 
 * @author Herko ter Horst
 */
public class ExportStatementsView implements View {

	private static final ExportStatementsView INSTANCE = new ExportStatementsView();

	public static ExportStatementsView getInstance() {
		return INSTANCE;
	}

	private ExportStatementsView() {
	}

	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		Repository repository = RepositoryInterceptor.getRepository(request);
		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);

		ValueFactory vf = repository.getValueFactory();

		Resource subj = ProtocolUtil.parseResourceParam(request, SUBJECT_PARAM_NAME, vf);
		URI pred = ProtocolUtil.parseURIParam(request, PREDICATE_PARAM_NAME, vf);
		Value obj = ProtocolUtil.parseValueParam(request, OBJECT_PARAM_NAME, vf);
		Resource[] contexts = ProtocolUtil.parseContextParam(request, CONTEXT_PARAM_NAME, vf);
		boolean useInferencing = ProtocolUtil.parseBooleanParam(request, INCLUDE_INFERRED_PARAM_NAME, true);

		RDFWriterFactory rdfWriterFactory = ProtocolUtil.getAcceptableRDFWriterFactory(request, response);
		RDFFormat rdfFormat = rdfWriterFactory.getRDFFormat();

		try {
			OutputStream out = response.getOutputStream();
			RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);

			response.setStatus(SC_OK);
			response.setContentType(rdfFormat.getMIMEType());
			response.setHeader("Content-Disposition", "attachment; filename=statements."
					+ rdfFormat.getFileExtension());

			repositoryCon.exportStatements(subj, pred, obj, useInferencing, rdfWriter, contexts);

			out.close();
		}
		catch (UnsupportedRDFormatException e) {
			throw new ClientRequestException(SC_NOT_ACCEPTABLE, "No RDF writer available for format: "
					+ rdfFormat.getName());
		}
		catch (RDFHandlerException e) {
			throw new InternalServerException("Serialization error: " + e.getMessage(), e);
		}
		catch (RepositoryException e) {
			throw new InternalServerException("Repository error: " + e.getMessage(), e);
		}
	}

}
