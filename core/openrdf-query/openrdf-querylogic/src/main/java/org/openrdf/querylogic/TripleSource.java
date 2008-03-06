/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylogic;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.util.iterator.CloseableIterator;

/**
 * A triple source that can be queried for (the existence of) certain triples in
 * certain contexts. This interface defines the methods that are needed by the
 * Sail Query Model to be able to evaluate itself.
 */
public interface TripleSource {

	/**
	 * Gets all statements from all contexts that have a specific subject,
	 * predicate and/or object. All three parameters may be null to indicate
	 * wildcards.
	 * 
	 * @param subj
	 *        A Resource specifying the subject, or <tt>null</tt> for a
	 *        wildcard.
	 * @param pred
	 *        A URI specifying the predicate, or <tt>null</tt> for a wildcard.
	 * @param obj
	 *        A Value specifying the object, or <tt>null</tt> for a wildcard.
	 * @return An iterator over the relevant statements.
	 */
	public CloseableIterator<? extends Statement> getStatements(Resource subj, URI pred, Value obj);

	/**
	 * Gets all statements from the null context that have a specific subject,
	 * predicate and/or object. All three parameters may be null to indicate
	 * wildcards.
	 * 
	 * @param subj
	 *        A Resource specifying the subject, or <tt>null</tt> for a
	 *        wildcard.
	 * @param pred
	 *        A URI specifying the predicate, or <tt>null</tt> for a wildcard.
	 * @param obj
	 *        A Value specifying the object, or <tt>null</tt> for a wildcard.
	 * @return An iterator over the relevant statements.
	 */
	public CloseableIterator<? extends Statement> getNullContextStatements(Resource subj, URI pred, Value obj);

	/**
	 * Gets all statements from the named contexts (excluding the null context)
	 * that have a specific subject, predicate, object and/or context. All four
	 * parameters may be null to indicate wildcards.
	 * 
	 * @param subj
	 *        A Resource specifying the subject, or <tt>null</tt> for a
	 *        wildcard.
	 * @param pred
	 *        A URI specifying the predicate, or <tt>null</tt> for a wildcard.
	 * @param obj
	 *        A Value specifying the object, or <tt>null</tt> for a wildcard.
	 * @param context
	 *        A Resource specifying the context, or <tt>null</tt> for a
	 *        wildcard.
	 * @return An iterator over the relevant statements.
	 */
	public CloseableIterator<? extends Statement> getNamedContextStatements(Resource subj, URI pred,
			Value obj, Resource context);

	/**
	 * Gets a ValueFactory object that can be used to create URI-, blank node-
	 * and literal objects.
	 * 
	 * @return a ValueFactory object for this TripleSource.
	 */
	public ValueFactory getValueFactory();
}
