/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.URIFactory;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryMetaData;
import org.openrdf.rio.RDFFormat;
import org.openrdf.store.Isolation;
import org.openrdf.store.StoreException;

class HTTPRepositoryMetaData implements InvocationHandler {

	private static final String EMBEDDED = "embedded";

	private static final String READ_ONLY = "readOnly";

	private static final String SUPPORTS_ISOLATION = "supportsIsolation";

	public static RepositoryMetaData create(HTTPRepository repository, Model model)
		throws StoreException
	{
		ClassLoader cl = HTTPRepositoryMetaData.class.getClassLoader();
		Class<?>[] interfaces = new Class<?>[] { RepositoryMetaData.class };
		try {
			BeanInfo info = Introspector.getBeanInfo(RepositoryMetaData.class);
			PropertyDescriptor[] properties = info.getPropertyDescriptors();
			HTTPRepositoryMetaData h = new HTTPRepositoryMetaData(repository, model, properties);
			return (RepositoryMetaData)Proxy.newProxyInstance(cl, interfaces, h);
		}
		catch (IntrospectionException e) {
			throw new StoreException(e);
		}
	}

	private Model model;

	private HTTPRepository repository;

	private PropertyDescriptor[] properties;

	public HTTPRepositoryMetaData(HTTPRepository repository, Model model, PropertyDescriptor[] properties) {
		this.repository = repository;
		this.model = model;
		this.properties = properties;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
		throws MalformedURLException
	{
		if (SUPPORTS_ISOLATION.equals(method.getName())) {
			return supportsIsolation((Isolation)args[0]);
		}
		String property = getPropertyName(method);
		Class<?> type = method.getReturnType();
		if (READ_ONLY.equals(property)) {
			return repository.isReadOnly();
		}
		if (EMBEDDED.equals(property)) {
			return false;
		}
		if (type.isArray()) {
			return getArrayOf(type.getComponentType(), property);
		}
		return getArrayOf(type, property)[0];
	}

	private boolean supportsIsolation(Isolation isolation)
		throws MalformedURLException
	{
		return Arrays.asList(getArrayOf(Isolation.class, SUPPORTS_ISOLATION)).contains(isolation);
	}

	private String getPropertyName(Method method) {
		for (PropertyDescriptor p : properties) {
			if (method.equals(p.getReadMethod())) {
				return p.getName();
			}
		}
		throw new AssertionError("No such property");
	}

	private Object[] getArrayOf(Class<?> type, String localName)
		throws MalformedURLException
	{
		URIFactory uf = repository.getURIFactory();
		URI pred = uf.createURI(Protocol.METADATA_NAMESPACE, localName);
		if (type.isAssignableFrom(String.class)) {
			return getString(pred);
		}
		else if (type.isAssignableFrom(Boolean.TYPE)) {
			return getBoolean(pred);
		}
		else if (type.isAssignableFrom(Integer.TYPE)) {
			return getInteger(pred);
		}
		else if (type.isAssignableFrom(URL.class)) {
			return getURL(pred);
		}
		else if (type.isAssignableFrom(QueryLanguage.class)) {
			return getQueryLanguage(pred);
		}
		else if (type.isAssignableFrom(RDFFormat.class)) {
			return getRDFFormat(pred);
		}
		else if (type.isAssignableFrom(Isolation.class)) {
			return getIsolation(pred);
		}
		else {
			throw new AssertionError("Unsupported type: " + type);
		}
	}

	private String[] getString(URI pred) {
		Set<Value> objects = get(pred);
		List<String> list = new ArrayList<String>(objects.size());
		for (Value obj : objects) {
			list.add(obj.stringValue());
		}
		return list.toArray(new String[list.size()]);
	}

	private Boolean[] getBoolean(URI pred) {
		Set<Value> objects = get(pred);
		List<Boolean> list = new ArrayList<Boolean>(objects.size());
		for (Value obj : objects) {
			list.add(((Literal)obj).booleanValue());
		}
		return list.toArray(new Boolean[list.size()]);
	}

	private Integer[] getInteger(URI pred) {
		Set<Value> objects = get(pred);
		List<Integer> list = new ArrayList<Integer>(objects.size());
		for (Value obj : objects) {
			list.add(((Literal)obj).intValue());
		}
		if (list.size() == 0) {
			list.add(0);
		}
		return list.toArray(new Integer[list.size()]);
	}

	private URL[] getURL(URI pred)
		throws MalformedURLException
	{
		Set<Value> objects = get(pred);
		List<URL> list = new ArrayList<URL>(objects.size());
		for (Value obj : objects) {
			list.add(new URL(obj.stringValue()));
		}
		return list.toArray(new URL[list.size()]);
	}

	private QueryLanguage[] getQueryLanguage(URI pred) {
		Set<Value> objects = get(pred);
		List<QueryLanguage> list = new ArrayList<QueryLanguage>(objects.size());
		for (Value obj : objects) {
			list.add(QueryLanguage.valueOf(((URI)obj).getLocalName()));
		}
		return list.toArray(new QueryLanguage[list.size()]);
	}

	private RDFFormat[] getRDFFormat(URI pred) {
		Set<Value> objects = get(pred);
		List<RDFFormat> list = new ArrayList<RDFFormat>(objects.size());
		for (Value obj : objects) {
			list.add(RDFFormat.valueOf(((URI)obj).getLocalName()));
		}
		return list.toArray(new RDFFormat[list.size()]);
	}

	private Isolation[] getIsolation(URI pred) {
		Set<Value> objects = get(pred);
		List<Isolation> list = new ArrayList<Isolation>(objects.size());
		for (Value obj : objects) {
			list.add(Isolation.valueOf(((URI)obj).getLocalName()));
		}
		return list.toArray(new Isolation[list.size()]);
	}

	private Set<Value> get(URI pred) {
		return model.filter(null, pred, null).objects();
	}

}
