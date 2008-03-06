/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sesame.webclient.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Literal;
import org.openrdf.queryresult.TupleQueryResult;
import org.openrdf.queryresult.TupleQueryResultFormat;
import org.openrdf.queryresult.TupleQueryResultHandlerException;
import org.openrdf.queryresult.TupleQueryResultParseException;
import org.openrdf.queryresult.TupleQueryResultUtil;
import org.openrdf.queryresult.Solution;
import org.openrdf.queryresult.UnsupportedQueryResultFormatException;
import org.openrdf.sesame.webclient.repository.RepositoryInfo;

public class SesameServer {

	/** Logger for this class and subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	private Map<String, RepositoryInfo> _repositories;

	private String _serverURL;

	public static final String SERVER_URL_KEY = "sesameServerURL";

	public SesameServer(String serverURL) {
		_serverURL = serverURL;

		if (!_serverURL.endsWith("/")) {
			_serverURL += "/";
		}

		retrieveRepositories();
	}

	private void retrieveRepositories() {
		TupleQueryResult result = null;

		GetMethod getRepositoryListRequest = new GetMethod(_serverURL + "repositories");
		getRepositoryListRequest.addRequestHeader("Accept", TupleQueryResultFormat.BINARY.getMIMEType());

		HttpClient client = new HttpClient();

		int responseCode = 0;
		try {
			responseCode = client.executeMethod(getRepositoryListRequest);
		}
		catch (HttpException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (responseCode != HttpStatus.SC_OK && // 200 OK
				responseCode != HttpStatus.SC_NO_CONTENT) // 204 NO CONTENT
		{
			logger.trace("response code = " + responseCode);
		}
		else {
			try {
				result = TupleQueryResultUtil.parse(getRepositoryListRequest.getResponseBodyAsStream(),
						TupleQueryResultFormat.BINARY);
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (TupleQueryResultParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (TupleQueryResultHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedQueryResultFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		getRepositoryListRequest.releaseConnection();

		_repositories = new HashMap<String, RepositoryInfo>();
		for (Solution solution : result) {
			RepositoryInfo rep = new RepositoryInfo();
			boolean readable = Boolean.parseBoolean(((Literal)solution.getValue("readable")).getLabel());
			if (readable) {
				rep.setLocation(solution.getValue("uri").toString());
				rep.setDescription(solution.getValue("title").toString());
				_repositories.put(rep.getLocation(), rep);
			}
		}
	}

	public String getServerURL() {
		return _serverURL;
	}

	public Map getRepositories() {
		return _repositories;
	}

}
