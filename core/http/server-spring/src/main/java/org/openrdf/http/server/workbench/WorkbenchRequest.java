/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.workbench;

import static java.lang.System.currentTimeMillis;
import static java.net.URLDecoder.decode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.StoreException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public class WorkbenchRequest extends HttpServletRequestWrapper {

	private static final String LAST_MODIFIED_COOKIE = "Last-Modified";

	private static final String HEADER_IFMODSINCE = "If-Modified-Since";

	private static final String HEADER_LASTMOD = "Last-Modified";

	private static final String UTF_8 = "UTF-8";

	private Logger logger = LoggerFactory.getLogger(WorkbenchRequest.class);

	private Map<String, String> parameters;

	private Map<String, String> defaults;

	private ValueFactory vf = new ValueFactoryImpl();

	private InputStream content;

	private HttpServletResponse response;

	private boolean controlCookieAdded;

	private int cookieMaxAge = 2592000;

	public WorkbenchRequest(HttpServletRequest req, HttpServletResponse resp, Map<String, String> defaults)
		throws StoreException, IOException, FileUploadException
	{
		super(req);
		this.defaults = defaults;
		this.response = resp;
		String url = req.getRequestURL().toString();
		if (ServletFileUpload.isMultipartContent(this)) {
			parameters = getMultipartParameterMap();
		}
		else if (req.getQueryString() == null && url.contains(";")) {
			parameters = getUrlParameterMap(url);
		}
	}

	public InputStream getContentParameter() {
		return content;
	}

	public String setting(String name) {
		String value = getParameter(name);
		if (isParameterPresent(name)) {
			setCookieValue(name, value);
		}
		return value;
	}

	/**
	 * This method hides the If-Modified-Since header if the browser's cookies
	 * have changed since this page was cached.
	 */
	@Override
	public long getDateHeader(String name) {
		long value = super.getDateHeader(name);
		if (HEADER_IFMODSINCE.equals(name)) {
			if (!"GET".equals(getMethod()))
				return -1;
			// MSIE does not cache different url parameters separately
			if (getRequestURL().toString().indexOf(';') > 0)
				return -1;
			Cookie[] cookies = getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (LAST_MODIFIED_COOKIE.equals(cookie.getName())) {
						long lastModified = Long.parseLong(cookie.getValue());
						if (lastModified > value) {
							// cookies have changed since this cache
							return -1;
						}
					}
				}
			}
		}
		return value;
	}

	/**
	 * The Last-Modified will include the last time the cookies changed for
	 * this browser.
	 */
	public void setDateHeader(String name, long date) {
		if (HEADER_LASTMOD.equals(name)) {
			String cookie = getCookieValue(LAST_MODIFIED_COOKIE);
			if (cookie != null) {
				long lastModified = Long.parseLong(cookie);
				if (lastModified > date) {
					// cookies have changed since, use that instead
					response.setDateHeader(name, lastModified);
					return;
				}
			}
		}
		response.setDateHeader(name, date);
	}

	public int getInt(String name)
		throws BadRequestException
	{
		String limit = getParameter(name);
		if (limit == null || limit.length() == 0)
			return 0;
		try {
			return Integer.parseInt(limit);
		}
		catch (NumberFormatException exc) {
			throw new BadRequestException(exc.getMessage(), exc);
		}
	}

	@Override
	public String getParameter(String name) {
		if (parameters != null && parameters.containsKey(name))
			return parameters.get(name);
		String[] values = super.getParameterValues(name);
		if (values != null && values.length > 0)
			// use the last one as it maybe appended in js
			return values[values.length - 1];
		String cookie = getCookieValue(name);
		if (cookie != null) {
			return cookie;
		}
		if (defaults != null && defaults.containsKey(name))
			return defaults.get(name);
		return null;
	}

	@Override
	public String[] getParameterValues(String name) {
		if (parameters != null && parameters.containsKey(name))
			return new String[] { parameters.get(name) };
		return super.getParameterValues(name);
	}

	public boolean isParameterPresent(String name) {
		if (parameters != null && parameters.get(name) != null)
			return parameters.get(name).length() > 0;
		String[] values = super.getParameterValues(name);
		if (values != null && values.length > 0)
			// use the last one as it maybe appended in js
			return values[values.length - 1].length() > 0;
		return false;
	}

	public Resource getResource(String name)
		throws BadRequestException, StoreException
	{
		Value value = decodeValue(getParameter(name));
		if (value == null || value instanceof Resource)
			return (Resource)value;
		throw new BadRequestException("Not a BNode or URI: " + value);
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getSingleParameterMap() {
		Map<String, String[]> map = super.getParameterMap();
		Map<String, String> parameters = new HashMap<String, String>(map.size());
		for (String name : map.keySet()) {
			if (isParameterPresent(name)) {
				parameters.put(name, getParameter(name));
			}
		}
		if (this.parameters != null) {
			parameters.putAll(this.parameters);
		}
		return parameters;
	}

	public String getTypeParameter() {
		return getParameter("type");
	}

	public URI getURI(String name)
		throws BadRequestException, StoreException
	{
		Value value = decodeValue(getParameter(name));
		if (value == null || value instanceof URI)
			return (URI)value;
		throw new BadRequestException("Not a URI: " + value);
	}

	public URL getUrl(String name)
		throws StoreException, BadRequestException, IOException
	{
		String url = getParameter(name);
		try {
			return new URL(url);
		}
		catch (MalformedURLException exc) {
			throw new BadRequestException(exc.getMessage());
		}
	}

	public Value getValue(String name)
		throws BadRequestException, StoreException
	{
		return decodeValue(getParameter(name));
	}

	private Value decodeValue(String string)
		throws StoreException, BadRequestException
	{
		try {
			if (string == null)
				return null;
			String value = string.trim();
			if (value.length() == 0 || value.equals("null"))
				return null;
			if (value.startsWith("_:")) {
				String label = value.substring("_:".length());
				return vf.createBNode(label);
			}
			else if (value.startsWith("<") && value.endsWith(">")) {
				String label = value.substring(1, value.length() - 1);
				return vf.createURI(label);
			}
			else if (value.charAt(0) == '"') {
				String label = value.substring(1, value.lastIndexOf('"'));
				String rest = value.substring(label.length() + 2);
				if (rest.startsWith("^^")) {
					Value datatype = decodeValue(rest.substring(2));
					if (datatype instanceof URI)
						return vf.createLiteral(label, (URI)datatype);
					throw new BadRequestException("Malformed datatype: " + value);
				}
				else if (rest.startsWith("@")) {
					return vf.createLiteral(label, rest.substring(1));
				}
				else {
					return vf.createLiteral(label);
				}
			}
			else {
				String prefix = value.substring(0, value.indexOf(':'));
				String localPart = value.substring(prefix.length() + 1);
				String ns = getNamespace(prefix);
				if (ns == null)
					throw new BadRequestException("Undefined prefix: " + value);
				return vf.createURI(ns, localPart);
			}
		}
		catch (Exception exc) {
			logger.warn(exc.toString(), exc);
			throw new BadRequestException("Malformed value: " + string, exc);
		}
	}

	private String getCookieValue(String name) {
		Cookie[] cookies = getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (name.equals(cookie.getName())) {
					response.setHeader("Vary", "Cookie");
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	private void setCookieValue(String name, String value) {
		// if using the defaults, skip setting the cookie
		if (getCookieValue(name) == null && value.equals(defaults.get(name)))
			return;
		Cookie cookie = new Cookie(name, value);
		if (getContextPath() != null) {
			cookie.setPath(getContextPath());
		}
		else {
			cookie.setPath("/");
		}
		cookie.setMaxAge(cookieMaxAge);
		addCookie(cookie);
	}

	/**
	 * This method tells the browser to track the last time the cookies
	 * changed.
	 */
	private void addCookie(Cookie c) {
		boolean changed = true;
		String existing = getCookieValue(c.getName());
		if (existing != null) {
			changed = !c.getValue().equals(existing);
		}
		response.addCookie(c);
		if (!controlCookieAdded && changed) {
			long date = currentTimeMillis() / 1000 * 1000;
			String value = String.valueOf(date);
			Cookie cookie = new Cookie(LAST_MODIFIED_COOKIE, value);
			String contextPath = getContextPath();
			if (contextPath == null) {
				cookie.setPath("/");
			}
			else {
				cookie.setPath(contextPath);
			}
			response.addCookie(cookie);
			response.setDateHeader(HEADER_LASTMOD, date);
			controlCookieAdded = true;
		}
	}

	private String firstLine(FileItemStream item)
		throws IOException
	{
		InputStream in = item.openStream();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			return reader.readLine();
		}
		finally {
			in.close();
		}
	}

	private String getNamespace(String prefix)
		throws StoreException
	{
		return null;
	}

	private Map<String, String> getMultipartParameterMap()
		throws StoreException, IOException, FileUploadException
	{
		Map<String, String> parameters = new HashMap<String, String>();
		ServletFileUpload upload = new ServletFileUpload();
		FileItemIterator iter = upload.getItemIterator(this);
		while (iter.hasNext()) {
			FileItemStream item = iter.next();
			String name = item.getFieldName();
			if ("content".equals(name)) {
				content = item.openStream();
				return parameters;
			}
			else {
				String firstLine = firstLine(item);
				parameters.put(name, firstLine);
			}
		}
		return parameters;
	}

	private Map<String, String> getUrlParameterMap(String url)
		throws UnsupportedEncodingException
	{
		String qry = url.substring(url.indexOf(';') + 1);
		Map<String, String> parameters = new HashMap<String, String>();
		for (String param : qry.split("&")) {
			int idx = param.indexOf('=');
			String name = decode(param.substring(0, idx), UTF_8);
			String value = decode(param.substring(idx + 1), UTF_8);
			parameters.put(name, value);
		}
		return parameters;
	}

}
