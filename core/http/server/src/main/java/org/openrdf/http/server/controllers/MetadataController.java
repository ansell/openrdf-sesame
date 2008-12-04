/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.LiteralFactory;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.URIFactory;
import org.openrdf.model.impl.ModelImpl;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryMetaData;
import org.openrdf.rio.RDFFormat;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
@Controller
public class MetadataController {

	@ModelAttribute
	@RequestMapping(method = { GET, HEAD }, value = "/repositories/*/metadata")
	public Model list(HttpServletRequest request)
		throws StoreException, IntrospectionException, IllegalArgumentException, IllegalAccessException,
		InvocationTargetException
	{
		BeanInfo info = Introspector.getBeanInfo(RepositoryMetaData.class);
		PropertyDescriptor[] properties = info.getPropertyDescriptors();

		Repository repository = RepositoryInterceptor.getRepository(request);
		RepositoryMetaData data = repository.getRepositoryMetaData();
		URIFactory uf = repository.getURIFactory();
		LiteralFactory lf = repository.getLiteralFactory();
		URI subj = uf.createURI(request.getRequestURL().toString());

		Model model = new ModelImpl();
		for (PropertyDescriptor p : properties) {
			Object o = p.getReadMethod().invoke(data);
			if (o instanceof Object[]) {
				for (Object e : (Object[]) o) {
					add(model, subj, uf, p.getName(), lf, e);
				}
			}
			else {
				add(model, subj, uf, p.getName(), lf, o);
			}
		}

		return model;
	}

	private void add(Model model, URI subj, URIFactory uf, String name, LiteralFactory lf, Object o) {
		if (o == null)
			return;
		URI pred = uf.createURI(Protocol.METADATA_NAMESPACE, name);
		if (o instanceof String) {
			model.add(subj, pred, lf.createLiteral((String)o));
		}
		else if (o instanceof Boolean) {
			model.add(subj, pred, lf.createLiteral((Boolean)o));
		}
		else if (o instanceof Integer) {
			Integer i = (Integer)o;
			if (i.intValue() != 0) {
				model.add(subj, pred, lf.createLiteral(i));
			}
		}
		else if (o instanceof URL) {
			model.add(subj, pred, uf.createURI(((URL)o).toExternalForm()));
		}
		else if (o instanceof QueryLanguage) {
			model.add(subj, pred, uf.createURI(Protocol.METADATA_NAMESPACE, ((QueryLanguage)o).getName()));
		}
		else if (o instanceof RDFFormat) {
			model.add(subj, pred, uf.createURI(Protocol.METADATA_NAMESPACE, ((RDFFormat)o).getName()));
		} else {
			throw new AssertionError("Unsupported type: " + o.getClass());
		}
	}
}
