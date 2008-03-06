/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.modify.add;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import org.openrdf.http.webclient.properties.RDFFormatPropertyEditor;
import org.openrdf.http.webclient.repository.RepositoryInfo;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

/**
 * @author Herko ter Horst
 */
public class AddController extends SimpleFormController {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
		throws ServletException
	{
		binder.registerCustomEditor(RDFFormat.class, new RDFFormatPropertyEditor());
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors)
		throws ServletException, IOException
	{
		logger.info("Uploading data...");
		RDFUpload rdfUpload = (RDFUpload)command;
		InputStream in = rdfUpload.getInputStream();

		RepositoryInfo repoInfo = (RepositoryInfo)request.getSession().getAttribute(
				RepositoryInfo.REPOSITORY_KEY);
		RepositoryConnection conn = null;
		try {
			conn = repoInfo.getRepository().getConnection();
			conn.add(in, rdfUpload.getBaseUri(), rdfUpload.getFormat());
			conn.commit();
			logger.info("Upload committed.");
		}
		catch (RDFParseException e) {
			logger.error("Unable to upload file", e);
			// FIXME: it's probably better to just return and display a failure
			// message.
			throw new ServletException(e);
		}
		catch (RepositoryException e) {
			logger.error("Unable to upload file", e);
			// FIXME: it's probably better to just return and display a failure
			// message.
			throw new ServletException(e);
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

		return new ModelAndView(getSuccessView(), "actionResult", rdfUpload.getI18n());
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest arg0)
		throws Exception
	{
		Map<String, Object> result = new HashMap<String, Object>();

		Map<String, String> rdfFormats = new TreeMap<String, String>();
		for (RDFFormat format : RDFFormat.values()) {
			rdfFormats.put(format.getName(), format.getName());
		}
		result.put("formats", rdfFormats);

		return result;
	}

}
