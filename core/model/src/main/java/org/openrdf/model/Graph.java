/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.util.GraphUtil;

/**
 * An RDF graph, represented as a collection of {@link Statement}s.
 * 
 * @see GraphUtil
 * @author Arjohn Kampman
 */
@Deprecated
public interface Graph extends Collection<Statement>, Serializable {

	/**
	 * Gets the value factory for this graph.
	 */
	public ValueFactory getValueFactory();

	/**
	 * Adds one or more statements to the graph. This method creates a statement
	 * for each specified context and adds those to the graph. If no contexts are
	 * specified, a single statement with no associated context is added.
	 * 
	 * @param subj
	 *        The statement's subject, must not be <tt>null</tt>.
	 * @param pred
	 *        The statement's predicate, must not be <tt>null</tt>.
	 * @param obj
	 *        The statement's object, must not be <tt>null</tt>.
	 * @param contexts
	 *        The contexts to add statements to.
	 */
	public boolean add(Resource subj, URI pred, Value obj, Resource... contexts);

	/**
	 * Gets the statements with the specified subject, predicate, object and
	 * (optionally) context. The <tt>subject</tt>, <tt>predicate</tt> and
	 * <tt>object</tt> parameters can be <tt>null</tt> to indicate wildcards. The
	 * <tt>contexts</tt> parameter is a wildcard and accepts zero or more values.
	 * If no contexts are specified, statements will match disregarding their
	 * context. If one or more contexts are specified, statements with a context
	 * matching one of these will match. Note: to match statements without an
	 * associated context, specify the value <tt>null</tt> and explicitly cast it
	 * to type <tt>Resource</tt>.
	 * <p>
	 * Examples: <tt>graph.match(s1, null, null)</tt> matches all statements that
	 * have subject <tt>s1</tt>,<br>
	 * <tt>graph.match(null, null, null, c1)</tt> matches all statements that
	 * have context <tt>c1</tt>,<br>
	 * <tt>graph.match(null, null, null, (Resource)null)</tt> matches all
	 * statements that have no associated context,<br>
	 * <tt>graph.match(null, null, null, c1, c2, c3)</tt> matches all statements
	 * that have context <tt>c1</tt>, <tt>c2</tt> or <tt>c3</tt>.
	 * 
	 * @param subj
	 *        The subject of the statements to match, <tt>null</tt> to match
	 *        statements with any subject.
	 * @param pred
	 *        The predicate of the statements to match, <tt>null</tt> to match
	 *        statements with any predicate.
	 * @param obj
	 *        The object of the statements to match, <tt>null</tt> to match
	 *        statements with any object.
	 * @param contexts
	 *        The contexts of the statements to match. If no contexts are
	 *        specified, statements will match disregarding their context. If one
	 *        or more contexts are specified, statements with a context matching
	 *        one of these will match.
	 * @return The statements that match the specified pattern.
	 */
	public Iterator<Statement> match(Resource subj, URI pred, Value obj, Resource... contexts);
}
