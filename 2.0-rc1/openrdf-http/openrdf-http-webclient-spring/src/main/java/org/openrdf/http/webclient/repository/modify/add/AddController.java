/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.modify.add;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import org.openrdf.http.webclient.SessionKeys;
import org.openrdf.http.webclient.properties.RDFFormatPropertyEditor;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFParserRegistry;

/**
 * @author Herko ter Horst
 */
public class AddController extends SimpleFormController {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
		binder.registerCustomEditor(RDFFormat.class, new RDFFormatPropertyEditor());
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors)
		throws Exception, IOException
	{
		ModelAndView result = null;

		logger.info("Uploading data...");
		RDFUpload rdfUpload = (RDFUpload)command;
		InputStream in = rdfUpload.getInputStream();

		HTTPRepository repo = (HTTPRepository)request.getSession().getAttribute(SessionKeys.REPOSITORY_KEY);
		RepositoryConnection conn = null;
		try {
			conn = repo.getConnection();
			conn.add(in, rdfUpload.getBaseUri(), rdfUpload.getFormat());
			conn.commit();
			logger.info("Upload committed.");
		}
		catch (RDFParseException e) {
			logger.warn("Unable to upload file {}", e.getMessage());
			errors.rejectValue("contents", "repository.modify.add.error.parsed", new Object[] {
					e.getLineNumber(),
					e.getColumnNumber(),
					e.getMessage() }, "PARSE ERROR");
		}
		catch (RepositoryException e) {
			logger.error("Unable to upload file", e);
			errors.reject("repository.error");
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				}
				catch (RepositoryException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				in.close();
			}
		}

		if (errors.hasErrors()) {
			result = showForm(request, response, errors, errors.getModel());
		}
		else {
			result = new ModelAndView(getSuccessView(), "actionResult", rdfUpload.getI18n());
		}

		return result;
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest arg0)
		throws Exception
	{
		Map<String, Object> result = new HashMap<String, Object>();

		Map<String, String> rdfFormats = new TreeMap<String, String>();

		for (RDFParserFactory factory : RDFParserRegistry.getInstance().getAll()) {
			RDFFormat format = factory.getRDFFormat();
			rdfFormats.put(format.getName(), format.getName());
		}

		result.put("formats", rdfFormats);

		return result;
	}
}
