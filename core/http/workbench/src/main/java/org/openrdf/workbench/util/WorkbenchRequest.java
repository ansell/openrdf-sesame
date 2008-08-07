package org.openrdf.workbench.util;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchRequest extends HttpServletRequestWrapper {
	private static final String UTF_8 = "UTF-8";
	private Logger logger = LoggerFactory.getLogger(WorkbenchRequest.class);
	private Map<String, String> parameters;
	private Repository repository;
	private ValueFactory vf;
	private InputStream content;

	public WorkbenchRequest(Repository repository, HttpServletRequest request)
			throws RepositoryException, IOException, FileUploadException {
		super(request);
		this.repository = repository;
		this.vf = repository.getValueFactory();
		String url = request.getRequestURL().toString();
		if (ServletFileUpload.isMultipartContent(this)) {
			parameters = getMultipartParameterMap();
		} else if (request.getQueryString() == null && url.contains(";")) {
			parameters = getUrlParameterMap(url);
		}
	}

	public InputStream getContentParameter() throws RepositoryException,
			BadRequestException, IOException, FileUploadException {
		return content;
	}

	public int getInt(String name) throws BadRequestException {
		String limit = getParameter(name);
		if (limit == null || limit.length() == 0)
			return 0;
		try {
			return Integer.parseInt(limit);
		} catch (NumberFormatException exc) {
			throw new BadRequestException(exc.getMessage(), exc);
		}
	}

	@Override
	public String getParameter(String name) {
		if (parameters != null)
			return parameters.get(name);
		return super.getParameter(name);
	}

	@Override
	public String[] getParameterValues(String name) {
		if (parameters != null)
			return new String[] { parameters.get(name) };
		return super.getParameterValues(name);
	}

	public Resource getResource(String name) throws BadRequestException,
			RepositoryException {
		Value value = decodeValue(getParameter(name));
		if (value == null || value instanceof Resource)
			return (Resource) value;
		throw new BadRequestException("Not a BNode or URI: " + value);
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getSingleParameterMap() {
		if (parameters != null)
			return parameters;
		Map<String, String[]> map = super.getParameterMap();
		Map<String, String> parameters = new HashMap<String, String>(map.size());
		for (String key : map.keySet()) {
			String[] values = map.get(key);
			// use the last one as it maybe appended in js
			String value = values[values.length - 1];
			if (value.trim().length() > 0) {
				parameters.put(key, value);
			}
		}
		return parameters;
	}

	public String getTypeParameter() {
		return getParameter("type");
	}

	public URI getURI(String name) throws BadRequestException,
			RepositoryException {
		Value value = decodeValue(getParameter(name));
		if (value == null || value instanceof URI)
			return (URI) value;
		throw new BadRequestException("Not a URI: " + value);
	}

	public URL getUrl(String name) throws RepositoryException,
			BadRequestException, IOException, FileUploadException {
		String url = getParameter(name);
		try {
			return new URL(url);
		} catch (MalformedURLException exc) {
			throw new BadRequestException(exc.getMessage());
		}
	}

	public Value getValue(String name) throws BadRequestException,
			RepositoryException {
		return decodeValue(getParameter(name));
	}

	public boolean isParameterPresent(String name) {
		String value = getParameter(name);
		return value != null && value.length() > 0;
	}

	private Value decodeValue(String string) throws RepositoryException,
			BadRequestException {
		try {
			if (string == null)
				return null;
			String value = string.trim();
			if (value.length() == 0 || value.equals("null"))
				return null;
			if (value.startsWith("_:")) {
				String label = value.substring("_:".length());
				return vf.createBNode(label);
			}else if (value.startsWith("<") && value.endsWith(">")) {
				String label = value.substring(1, value.length() - 1);
				return vf.createURI(label);
			} else if (value.charAt(0) == '"') {
				String label = value.substring(1, value.lastIndexOf('"'));
				String rest = value.substring(label.length() + 2);
				if (rest.startsWith("^^")) {
					Value datatype = decodeValue(rest.substring(2));
					if (datatype instanceof URI)
						return vf.createLiteral(label, (URI) datatype);
					throw new BadRequestException("Malformed datatype: "
							+ value);
				} else if (rest.startsWith("@")) {
					return vf.createLiteral(label, rest.substring(1));
				} else {
					return vf.createLiteral(label);
				}
			} else {
				String prefix = value.substring(0, value.indexOf(':'));
				String localPart = value.substring(prefix.length() + 1);
				String ns = getNamespace(prefix);
				if (ns == null)
					throw new BadRequestException("Undefined prefix: " + value);
				return vf.createURI(ns, localPart);
			}
		} catch (Exception exc) {
			logger.warn(exc.toString(), exc);
			throw new BadRequestException("Malformed value: " + string, exc);
		}
	}

	private String firstLine(FileItemStream item) throws IOException {
		InputStream in = item.openStream();
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			return reader.readLine();
		} finally {
			in.close();
		}
	}

	private String getNamespace(String prefix) throws RepositoryException {
		RepositoryConnection con = repository.getConnection();
		try {
			String ns = con.getNamespace(prefix);
			if (ns != null)
				return ns;
			for (Namespace n : con.getNamespaces().asList()) {
				if (prefix.equals(n.getPrefix()))
					ns = n.getName();
			}
			if (ns != null) {
				logger.error("Namespace could not be found, but it does exist");
			}
			return ns;
		} finally {
			con.close();
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> getMultipartParameterMap()
			throws RepositoryException, IOException, FileUploadException {
		Map<String, String> parameters = new HashMap<String, String>();
		ServletFileUpload upload = new ServletFileUpload();
		FileItemIterator iter = upload.getItemIterator(this);
		while (iter.hasNext()) {
			FileItemStream item = iter.next();
			String name = item.getFieldName();
			if ("content".equals(name)) {
				content = item.openStream();
				return parameters;
			} else {
				String firstLine = firstLine(item);
				parameters.put(name, firstLine);
			}
		}
		return parameters;
	}

	private Map<String, String> getUrlParameterMap(String url)
			throws UnsupportedEncodingException {
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
