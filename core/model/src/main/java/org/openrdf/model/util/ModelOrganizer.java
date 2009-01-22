/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * Reorganises a Model to follow a more logical flow.
 * 
 * @author James Leigh
 */
public class ModelOrganizer {

	static class ValueComparator implements Comparator<Value> {

		public int compare(Value o1, Value o2) {
			if (o1 instanceof Literal) {
				if (o2 instanceof Literal) {
					Literal l1 = (Literal)o1;
					Literal l2 = (Literal)o2;
					if (l1.getLanguage() != null && l2.getLanguage() != null) {
						int l = l1.getLanguage().compareTo(l2.getLanguage());
						return l != 0 ? l : o1.stringValue().compareTo(o2.stringValue());
					}
					else if (l1.getLanguage() != null) {
						return -1;
					}
					else if (l2.getLanguage() != null) {
						return 1;
					}
					if (l1.getDatatype() != null && l2.getDatatype() != null) {
						int d = l1.getDatatype().stringValue().compareTo(l2.getDatatype().stringValue());
						return d != 0 ? d : o1.stringValue().compareTo(o2.stringValue());
					}
					else if (l1.getDatatype() != null) {
						return -1;
					}
					else if (l2.getDatatype() != null) {
						return 1;
					}
				}
			}
			return o1.stringValue().compareTo(o2.stringValue());
		}

	}

	private static final String RDF_ = RDF.NAMESPACE + "_";

	private static final String PREDICATE_ORDER_RESOURCE = "META-INF/org.openrdf.model.order-predicates";

	private Logger logger = LoggerFactory.getLogger(ModelOrganizer.class);

	private ValueFactory vf = ValueFactoryImpl.getInstance();

	private Model source;

	private Model destination;

	private List<? extends Resource> subjectOrder = Collections.emptyList();

	private List<URI> predicateOrder = Arrays.asList(RDF.TYPE, RDFS.LABEL, RDFS.COMMENT);

	private Set<Resource> covered;

	public ModelOrganizer(Model model) {
		this.source = model;
		try {
			List<URI> uris = load(PREDICATE_ORDER_RESOURCE);
			if (!uris.isEmpty()) {
				predicateOrder = uris;
			}
		}
		catch (IOException e) {
			logger.warn(e.toString(), e);
		}
	}

	public void setSubjectOrder(Resource... order) {
		this.subjectOrder = Arrays.asList(order);
	}

	public void setPredicateOrder(URI... order) {
		this.predicateOrder = Arrays.asList(order);
	}

	/**
	 * Organises the Model using the subject and predicate order and its own
	 * built-in order for the rest.
	 * 
	 * @return a Model with a predictable order to the subjects and predicates
	 */
	public Model organize() {
		init();
		// subject order
		for (Resource resource : subjectOrder) {
			organize(resource);
		}
		organizedTheRest();
		return destination;
	}

	private void init() {
		covered = new HashSet<Resource>(source.size() / 2);
		destination = new LinkedHashModel(source.getNamespaces());
	}

	private void organizedTheRest() {
		// URI subjects by type
		for (Value type : sort(source.filter(null, RDF.TYPE, null).objects())) {
			for (Resource subj : sort(source.filter(null, RDF.TYPE, type).subjects())) {
				if (subj instanceof URI && !covered.contains(subj)) {
					organize(subj);
				}
			}
		}
		// URI subjects without a type
		for (Resource subj : source.subjects()) {
			if (subj instanceof URI && !covered.contains(subj)) {
				organize(subj);
			}
		}
		// BNode subjects by type
		for (Value type : source.filter(null, RDF.TYPE, null).objects()) {
			for (Resource subj : source.filter(null, RDF.TYPE, type).subjects()) {
				if (subj instanceof BNode && !covered.contains(subj)) {
					organize(subj);
				}
			}
		}
		// BNode subjects without a type
		for (Resource subj : source.subjects()) {
			if (subj instanceof BNode && !covered.contains(subj)) {
				organize(subj);
			}
		}
	}

	private void organize(Resource subj) {
		covered.add(subj);
		// predicate order
		for (URI pred : predicateOrder) {
			for (Statement st : source.filter(subj, pred, null)) {
				next(st);
			}
		}
		// container order
		Set<URI> container = null;
		Set<URI> rest = new TreeSet<URI>(new ValueComparator());
		for (Statement st : source.filter(subj, null, null)) {
			URI pred = st.getPredicate();
			if (pred.toString().startsWith(RDF_)) {
				if (container == null) {
					container = organizeContainer(subj);
				}
				if (!container.contains(pred)) {
					next(st);
				}
			}
			else if (!predicateOrder.contains(pred)) {
				rest.add(pred);
			}
		}
		// the rest
		for (URI pred : rest) {
			for (Value obj : sort(source.filter(subj, pred, null).objects())) {
				for (Statement st : source.filter(subj, pred, obj)) {
					next(st);
				}
			}
		}
	}

	private Set<URI> organizeContainer(Resource subj) {
		Set<URI> set = new HashSet<URI>();
		int idx = 1;
		URI pred = vf.createURI(RDF.NAMESPACE, "_" + idx++);
		while (source.contains(subj, pred, null)) {
			for (Statement st : source.filter(subj, pred, null)) {
				next(st);
			}
			set.add(pred);
			pred = vf.createURI(RDF.NAMESPACE, "_" + idx++);
		}
		return set;
	}

	private void next(Statement st) {
		destination.add(st);
		Value obj = st.getObject();
		if (obj instanceof BNode && !covered.contains(obj)) {
			organize((BNode)obj);
		}
	}

	private <T extends Value> Set<T> sort(Set<T> set) {
		TreeSet<T> result = new TreeSet<T>(new ValueComparator());
		result.addAll(set);
		return result;
	}

	private List<URI> load(String path)
		throws IOException
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		InputStream in = cl.getResourceAsStream(path);
		if (in == null)
			return Collections.emptyList();
		try {
			String line;
			List<URI> list = new ArrayList<URI>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#"))
					continue;
				list.add(vf.createURI(line));
			}
			return list;
		}
		finally {
			in.close();
		}
	}
}
