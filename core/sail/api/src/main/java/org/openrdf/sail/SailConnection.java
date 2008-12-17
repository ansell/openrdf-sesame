/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

import org.openrdf.cursor.Cursor;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.store.StoreException;

/**
 * A connection to an RDF Sail object. A SailConnection is active from the
 * moment it is created until it is closed. Care should be taken to properly
 * close SailConnections as they might block concurrent queries and/or updates
 * on the Sail while active, depending on the Sail-implementation that is being
 * used.
 * 
 * @author jeen
 * @author Arjohn Kampman
 */
public interface SailConnection {

	/**
	 * Checks whether this SailConnection is open. A SailConnection is open from
	 * the moment it is created until it is closed.
	 * 
	 * @see SailConnection#close
	 */
	public boolean isOpen()
		throws StoreException;

	/**
	 * Closes the connection. Any updates that haven't been committed yet will be
	 * rolled back. The connection can no longer be used once it is closed.
	 */
	public void close()
		throws StoreException;

	/**
	 * Gets a ValueFactory object that can be used to create URI-, blank node-,
	 * literal- and statement objects.
	 * 
	 * @return a ValueFactory object for this Sail object.
	 */
	public ValueFactory getValueFactory();

	/**
	 * Evaluates the supplied TupleExpr on the data contained in this Sail
	 * object, using the (optional) dataset and supplied bindings as input
	 * parameters.
	 * 
	 * @param tupleQuery
	 *        The TupleQuery to evaluate.
	 * @param dataset
	 *        The dataset to use for evaluating the query, <tt>null</tt> to use
	 *        the Sail's default dataset.
	 * @param bindings
	 *        A set of input parameters for the query evaluation. The keys
	 *        reference variable names that should be bound to the value they map
	 *        to.
	 * @param includeInferred
	 *        Indicates whether inferred triples are to be considered in the
	 *        query result. If false, no inferred statements are returned; if
	 *        true, inferred statements are returned if available
	 * @return The TupleQueryResult.
	 * @throws StoreException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 */
	public Cursor<? extends BindingSet> evaluate(QueryModel query, BindingSet bindings,
			boolean includeInferred)
		throws StoreException;

	/**
	 * Returns the set of all unique context identifiers that are used to store
	 * statements.
	 * 
	 * @return An iterator over the context identifiers, should not contain any
	 *         duplicates.
	 */
	public Cursor<? extends Resource> getContextIDs()
		throws StoreException;

	/**
	 * Gets all statements from the specified contexts that have a specific
	 * subject, predicate and/or object. All three parameters may be null to
	 * indicate wildcards. The <tt>includeInferred</tt> parameter can be used to
	 * control which statements are fetched: all statements or only the
	 * statements that have been added explicitly.
	 * 
	 * @param subj
	 *        A Resource specifying the subject, or <tt>null</tt> for a wildcard.
	 * @param pred
	 *        A URI specifying the predicate, or <tt>null</tt> for a wildcard.
	 * @param obj
	 *        A Value specifying the object, or <tt>null</tt> for a wildcard.
	 * @param includeInferred
	 *        if false, no inferred statements are returned; if true, inferred
	 *        statements are returned if available
	 * @param contexts
	 *        The context(s) to get the data from. Note that this parameter is a
	 *        vararg and as such is optional. If no contexts are supplied the
	 *        method operates on the entire repository.
	 * @return The statements matching the specified pattern.
	 * @throws StoreException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 */
	public Cursor<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws StoreException;

	/**
	 * Returns the number of statements matching the specified pattern.
	 * 
	 * @param subj
	 *        A Resource specifying the subject, or <tt>null</tt> for a wildcard.
	 * @param pred
	 *        A URI specifying the predicate, or <tt>null</tt> for a wildcard.
	 * @param obj
	 *        A Value specifying the object, or <tt>null</tt> for a wildcard.
	 * @param includeInferred
	 *        Indicates whether inferred statements should be counted.
	 * @param contexts
	 *        The context(s) to get the data from. Note that this parameter is a
	 *        vararg and as such is optional. If no contexts are supplied the
	 *        method operates on the entire repository.
	 * @return The number of explicit statements in this Sail.
	 */
	public long size(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException;

	/**
	 * Begins a transaction requiring {@link #commit()} or {@link #rollback()} to
	 * be called to close the transaction.
	 * 
	 * @throws StoreException
	 *         If the SailConnection could not start a transaction.
	 */
	public void begin()
		throws StoreException;

	/**
	 * Commits any updates that have been performed since the last time
	 * {@link #commit()} or {@link #rollback()} was called.
	 * 
	 * @throws StoreException
	 *         If the SailConnection could not be committed.
	 */
	public void commit()
		throws StoreException;

	/**
	 * Rolls back the SailConnection, discarding any uncommitted changes that
	 * have been made in this SailConnection.
	 * 
	 * @throws StoreException
	 *         If the SailConnection could not be rolled back.
	 */
	public void rollback()
		throws StoreException;

	/**
	 * Adds a statement to each context in the specified contexts.
	 * 
	 * @param subj
	 *        The subject of the statement to add.
	 * @param pred
	 *        The predicate of the statement to add.
	 * @param obj
	 *        The object of the statement to add.
	 * @param contexts
	 *        The context(s) to add the statement to. Note that this parameter is
	 *        a vararg and as such is optional. If no contexts are supplied the
	 *        method operates on the entire repository.
	 * @throws StoreException
	 *         If the statement could not be added.
	 */
	public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException;

	/**
	 * Removes all statements matching the specified subject, predicate and
	 * object from the repository. All three parameters may be null to indicate
	 * wildcards.
	 * 
	 * @param subj
	 *        The subject of the statement that should be removed, or
	 *        <tt>null</tt> to indicate a wildcard.
	 * @param pred
	 *        The predicate of the statement that should be removed, or
	 *        <tt>null</tt> to indicate a wildcard.
	 * @param obj
	 *        The object of the statement that should be removed , or
	 *        <tt>null</tt> to indicate a wildcard. *
	 * @param contexts
	 *        The context(s) from which to remove the statement. Note that this
	 *        parameter is a vararg and as such is optional. If no contexts are
	 *        supplied the method operates on the entire repository.
	 * @throws StoreException
	 *         If the statement could not be removed.
	 */
	public void removeStatements(Resource subj, URI pred, Value obj, Resource... context)
		throws StoreException;

	/**
	 * Gets the namespaces relevant to the data contained in this Sail object.
	 * 
	 * @returns An iterator over the relevant namespaces, should not contain any
	 *          duplicates.
	 * @throws StoreException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 */
	public Cursor<? extends Namespace> getNamespaces()
		throws StoreException;

	/**
	 * Gets the namespace that is mapped to the specified prefix.
	 * 
	 * @param prefix
	 *        A namespace prefix.
	 * @return The namespace name that the specified prefix maps to.
	 */
	public String getNamespace(String prefix)
		throws StoreException;

	/**
	 * Sets the prefix of a namespace.
	 * 
	 * @param prefix
	 *        The new prefix.
	 * @param name
	 *        The namespace name that the prefix maps to.
	 */
	public void setNamespace(String prefix, String name)
		throws StoreException;

	/**
	 * Removes a namespace declaration by removing the association between a
	 * prefix and a namespace name.
	 * 
	 * @param prefix
	 *        The namespace prefix of which the assocation with a namespace name
	 *        is to be removed.
	 * @throws StoreException
	 *         If the namespace prefix could not be removed.
	 */
	public void removeNamespace(String prefix)
		throws StoreException;

	/**
	 * Removes all namespace declarations from the repository.
	 * 
	 * @throws StoreException
	 *         If the namespaces could not be removed.
	 */
	public void clearNamespaces()
		throws StoreException;

}
