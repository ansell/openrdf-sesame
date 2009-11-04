/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.net.URL;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.server.representations.ModelRepresentation;
import org.openrdf.http.server.resources.helpers.StatementResultResource;
import org.openrdf.model.LiteralFactory;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.URIFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryMetaData;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.store.Isolation;

/**
 * @author Arjohn Kampman
 */
public class MetaDataResource extends StatementResultResource {

	protected final Representation getRepresentation(RDFWriterFactory factory, MediaType mediaType)
		throws ResourceException
	{
		try {
			BeanInfo info = Introspector.getBeanInfo(RepositoryMetaData.class);
			PropertyDescriptor[] properties = info.getPropertyDescriptors();

			Repository repository = getRepository();
			RepositoryMetaData data = repository.getMetaData();
			URIFactory uf = repository.getURIFactory();
			LiteralFactory lf = repository.getLiteralFactory();

			URI subj = uf.createURI(getRequest().getResourceRef().toString(false, false));

			Model model = new LinkedHashModel();
			for (PropertyDescriptor p : properties) {
				Object o = p.getReadMethod().invoke(data);
				if (o instanceof Object[]) {
					for (Object e : (Object[])o) {
						add(model, subj, uf, p.getName(), lf, e);
					}
				}
				else {
					add(model, subj, uf, p.getName(), lf, o);
				}
			}

			for (Isolation isolation : Isolation.values()) {
				if (data.supportsIsolation(isolation)) {
					add(model, subj, uf, "supportsIsolation", lf, isolation);
				}
			}

			return new ModelRepresentation(model, factory, mediaType);
		}
		catch (Exception e) {
			throw new ResourceException(e);
		}
	}

	@Override
	protected String getFilenamePrefix() {
		return "metadata";
	}

	private void add(Model model, URI subj, URIFactory uf, String name, LiteralFactory lf, Object o) {
		if (o == null) {
			return;
		}
		URI pred = uf.createURI(Protocol.METADATA_NAMESPACE, name);
		if (o instanceof String) {
			model.add(subj, pred, lf.createLiteral((String)o));
		}
		else if (o instanceof Boolean) {
			model.add(subj, pred, lf.createLiteral((Boolean)o));
		}
		else if (o instanceof Integer) {
			model.add(subj, pred, lf.createLiteral((Integer)o));
		}
		else if (o instanceof URL) {
			model.add(subj, pred, uf.createURI(((URL)o).toExternalForm()));
		}
		else if (o instanceof QueryLanguage) {
			model.add(subj, pred, uf.createURI(Protocol.METADATA_NAMESPACE, ((QueryLanguage)o).getName()));
		}
		else if (o instanceof RDFFormat) {
			model.add(subj, pred, uf.createURI(Protocol.METADATA_NAMESPACE, ((RDFFormat)o).getName()));
		}
		else if (o instanceof Isolation) {
			model.add(subj, pred, uf.createURI(Protocol.METADATA_NAMESPACE, o.toString()));
		}
		else {
			throw new AssertionError("Unsupported type: " + o.getClass());
		}
	}
}
