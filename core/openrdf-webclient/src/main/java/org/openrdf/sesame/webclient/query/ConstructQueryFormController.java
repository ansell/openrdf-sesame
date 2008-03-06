/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sesame.webclient.query;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.querymodel.QueryLanguage;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ConstructQueryFormController extends SimpleFormController {

	/** Logger for this class and subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	public ModelAndView onSubmit(Object command)
		throws ServletException
	{

		String query = ((QueryInfo)command).getQuery();

		String format = ((QueryInfo)command).getResultFormat();

		String queryLanguage = ((QueryInfo)command).getQueryLanguage();

		logger.info("query = " + query);

		String repositoryLocation = ((QueryInfo)command).getRepository();
		logger.info("repositoryLocation = " + repositoryLocation);

		String url = null;
		try {
			if (QueryLanguage.SERQL.equals(QueryLanguage.valueOf(queryLanguage))) {
				url = repositoryLocation + "?queryLn=" + QueryLanguage.SERQL + "&query="
						+ URLEncoder.encode(query, "UTF-8");
			}
			else {
				url = repositoryLocation + "?query=" + URLEncoder.encode(query, "UTF-8");
			}
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		HttpClient client = new HttpClient();
		HttpMethod get = new GetMethod(url);
		get.addRequestHeader("Accept", format);

		int responseCode = 0;
		try {
			responseCode = client.executeMethod(get);
		}
		catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String result = null;
		if (responseCode != HttpURLConnection.HTTP_OK && // 200 OK
				responseCode != HttpURLConnection.HTTP_NO_CONTENT) // 204 NO CONTENT
		{
			try {
				throw new ServletException(get.getResponseBodyAsString());
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			try {
				result = get.getResponseBodyAsString();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		get.releaseConnection();

		// Map myModel = new HashMap();
		// myModel.put("serqlResult", result);

		return new ModelAndView(getSuccessView(), "result", result);
	}
}
