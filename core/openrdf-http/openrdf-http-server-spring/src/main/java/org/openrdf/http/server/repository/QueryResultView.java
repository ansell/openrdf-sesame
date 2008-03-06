/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import info.aduna.lang.FileFormat;

/**
 * Base class for rendering query results.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public abstract class QueryResultView implements View {

	/**
	 * Key by which the query result is stored in the model.
	 */
	public static final String QUERY_RESULT_KEY = "queryResult";

	/**
	 * Key by which the query result writer factory is stored in the model.
	 */
	public static final String FACTORY_KEY = "factory";

	/**
	 * Key by which a filename hint is stored in the model. The filename hint may
	 * be used to present the client with a suggestion for a filename to use for
	 * storing the result.
	 */
	public static final String FILENAME_HINT_KEY = "filenameHint";

	protected void setContentType(HttpServletResponse response, FileFormat fileFormat)
		throws IOException
	{
		String mimeType = fileFormat.getDefaultMIMEType();
		if (fileFormat.hasCharset()) {
			Charset charset = fileFormat.getCharset();
			mimeType += "; charset=" + charset.name();
		}
		response.setContentType(mimeType);
	}

	@SuppressWarnings("unchecked")
	protected void setContentDisposition(Map model, HttpServletResponse response, FileFormat fileFormat)
		throws IOException
	{
		// Report as attachment to make use in browser more convenient
		String filename = (String)model.get(FILENAME_HINT_KEY);

		if (filename == null || filename.length() == 0) {
			filename = "result";
		}

		if (fileFormat.getDefaultFileExtension() != null) {
			filename += "." + fileFormat.getDefaultFileExtension();
		}

		response.setHeader("Content-Disposition", "attachment; filename=" + filename);
	}
}