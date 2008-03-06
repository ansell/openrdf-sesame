/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.extract;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import org.openrdf.http.webclient.properties.RDFFormatPropertyEditor;
import org.openrdf.http.webclient.properties.ResourcePropertyEditor;
import org.openrdf.http.webclient.properties.UriPropertyEditor;
import org.openrdf.http.webclient.properties.ValuePropertyEditor;
import org.openrdf.http.webclient.repository.RepositoryInfo;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.Rio;

public class ExtractionController extends SimpleFormController {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
		throws ServletException
	{
		HttpSession session = request.getSession();
		RepositoryInfo repInfo = (RepositoryInfo)session.getAttribute(RepositoryInfo.REPOSITORY_KEY);

		binder.registerCustomEditor(Resource.class, new ResourcePropertyEditor(
				repInfo.getRepository().getValueFactory()));
		binder.registerCustomEditor(URI.class, new UriPropertyEditor(repInfo.getRepository().getValueFactory()));
		binder.registerCustomEditor(Value.class, new ValuePropertyEditor(
				repInfo.getRepository().getValueFactory()));

		binder.registerCustomEditor(RDFFormat.class, new RDFFormatPropertyEditor());
	}

	@Override
	public ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors)
		throws ServletException
	{
		RepositoryInfo repoInfo = (RepositoryInfo)request.getSession().getAttribute(
				RepositoryInfo.REPOSITORY_KEY);

		ExtractionSettings settings = (ExtractionSettings)command;
		RDFFormat format = settings.getResultFormat();

		RepositoryConnection conn = null;
		try {
			response.setContentType(format.getMIMEType());
			response.setHeader("Content-Disposition", "attachment; filename=extract."
					+ format.getFileExtension());

			RDFWriter writer = Rio.createWriter(format, response.getOutputStream());

			conn = repoInfo.getRepository().getConnection();
			conn.exportStatements(settings.getSubject(), settings.getPredicate(), settings.getObject(),
					settings.isIncludeInferred(), writer, settings.getContexts());
		}
		catch (RepositoryException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				response.getOutputStream().close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			if (conn != null) {
				try {
					conn.close();
				}
				catch (RepositoryException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request)
		throws Exception
	{
		Map<String, Object> result = new HashMap<String, Object>();

		Map<String, String> resultFormats = new TreeMap<String, String>();

		for (RDFWriterFactory factory : Rio.getRDFWriterRegistry().getAll()) {
			RDFFormat resultFormat = factory.getRDFFormat();
			resultFormats.put(resultFormat.getName(), resultFormat.getName());
		}

		result.put("resultFormats", resultFormats);

		return result;
	}
}
