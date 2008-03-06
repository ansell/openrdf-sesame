/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sesame.webclient;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.servlet.ServletException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.openrdf.queryresult.TupleQueryResult;
import org.openrdf.queryresult.TupleQueryResultFormat;
import org.openrdf.queryresult.TupleQueryResultHandlerException;
import org.openrdf.queryresult.TupleQueryResultParseException;
import org.openrdf.queryresult.TupleQueryResultUtil;
import org.openrdf.queryresult.Solution;
import org.openrdf.queryresult.UnsupportedQueryResultFormatException;

/**
 * Quick-n-dirty FOAF crawler that adds each harvested foaf profile to its own
 * context.
 * 
 * @author Jeen Broekstra
 */
public class SesameUploader {

	private static String repository = "http://demo.openrdf.org/sesame2/repositories/native-rdf";

	private static String[] foaf = {
			"http://www.openrdf.org/sesame-doap.rdf",
			"http://www.openrdf.org/people/foaf-jeen.rdf",
			"http://www.openrdf.org/people/foaf-arjohn.rdf" };

	private static String username = "username";

	private static String password = "password";

	private static HashMap<String, String> uploaded = new HashMap<String, String>();

	/**
	 * @param args
	 * @throws IOException
	 * @throws HttpException
	 * @throws ServletException
	 */
	public static void main(String[] args)
		throws HttpException, IOException
	{
		HttpClient client = new HttpClient();
		client.getState().setAuthenticationPreemptive(true);

		Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
		client.getState().setCredentials(null, "demo.openrdf.org", defaultcreds);

		PutMethod put = null;

		for (int i = 0; i < foaf.length; i++) {
			System.out.println("Retrieving " + foaf[i]);
			GetMethod get = new GetMethod(foaf[i]);

			int getResponse = 0;
			try {
				getResponse = client.executeMethod(get);
			}
			catch (HttpException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (getResponse == HttpStatus.SC_OK) {
				String foafFile = get.getResponseBodyAsString();

				put = new PutMethod(repository + "?context=" + URLEncoder.encode(foaf[i], "UTF-8"));

				put.setRequestBody(foafFile);

				try {
					int responseCode = client.executeMethod(put);

					System.out.println("Reponse code = " + responseCode);
					String response = put.getResponseBodyAsString();
					System.out.println("Response = " + response);
					put.releaseConnection();
					uploaded.put(foaf[i], foaf[i]);
				}
				catch (HttpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			get.releaseConnection();
		}

		TupleQueryResult result = _queryForFoaf(client);

		_uploadFoaf(client, result);

		result = _queryForFoaf(client);

		_uploadFoaf(client, result);

		System.out.println("Completed upload of " + uploaded.size() + " FOAF profiles.");
	}

	private static void _uploadFoaf(HttpClient client, TupleQueryResult result)
		throws IOException
	{
		for (Solution sol:result) {
			String url = sol.getValue("foafFile").toString();
			if (url.contains(" ")) {
				// skip for now
				continue;
			}
			if (uploaded.get(url) != null) {
				// skip already uploaded
				continue;
			}
			System.out.println("Retrieving " + url);
			GetMethod get = new GetMethod(url);

			int getResponse = 0;
			try {
				getResponse = client.executeMethod(get);
			}
			catch (HttpException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (getResponse == HttpStatus.SC_OK) {
				String foafFile;
				foafFile = get.getResponseBodyAsString();

				PutMethod put = new PutMethod(repository + "?context=" + URLEncoder.encode(url, "UTF-8"));

				put.setRequestBody(foafFile);

				try {
					int responseCode = client.executeMethod(put);

					System.out.println("Reponse code = " + responseCode);
					String response = put.getResponseBodyAsString();
					System.out.println("Response = " + response);
					put.releaseConnection();
					uploaded.put(url, url);
				}
				catch (HttpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			get.releaseConnection();
		}
	}

	private static TupleQueryResult _queryForFoaf(HttpClient client)
		throws HttpException, IOException
	{
		String query = "SELECT foafFile FROM {x} rdfs:seeAlso {foafFile}";

		System.out.println("Querying for new foaf files...");
		GetMethod queryGet = new GetMethod(repository + "?queryLn=SeRQL&query="
				+ URLEncoder.encode(query, "UTF-8"));
		queryGet.addRequestHeader("Accept", TupleQueryResultFormat.BINARY.getMIMEType());

		int responseCode = client.executeMethod(queryGet);

		TupleQueryResult result = null;
		if (responseCode != HttpStatus.SC_OK && // 200 OK
				responseCode != HttpStatus.SC_NO_CONTENT) // 204 NO CONTENT
		{
			System.out.println("Reponse code = " + responseCode);
		}
		else {
			try {
				result = TupleQueryResultUtil.parse(queryGet.getResponseBodyAsStream(), TupleQueryResultFormat.BINARY);
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
		queryGet.releaseConnection();
		return result;
	}

}
