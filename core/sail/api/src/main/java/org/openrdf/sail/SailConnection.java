/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
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
import org.openrdf.store.Isolation;
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
	 * Retrieves this connection's current transaction isolation level.
	 * 
	 * @return The current transaction isolation level.
	 * @exception StoreException
	 *            If an access error occurs or this method is called on a closed
	 *            connection
	 * @see #setTransactionIsolation
	 */
	public Isolation getTransactionIsolation()
		throws StoreException;

	/**
	 * Attempts to change the transaction isolation level for this connection to
	 * the specified value.
	 * <P>
	 * <B>Note:</B> If this method is called during a transaction, the result is
	 * implementation-defined.
	 * 
	 * @param isolation
	 *        Any Isolation except for {@link Isolation#NONE NONE}, since that
	 *        indicates that transactions are not supported.
	 * @exception StoreException
	 *            If an access error occurs, this method is called on a closed
	 *            connection
	 * @see #getTransactionIsolation
	 */
	public void setTransactionIsolation(Isolation isolation)
		throws StoreException;

	/**
	 * Indicates whether this connection is in read-only mode.
	 * 
	 * @return <tt>true</tt> if this Connection object is read-only;
	 *         <tt>false</tt> otherwise.
	 * @throws StoreException
	 *         If a repository access error occurs.
	 */
	public boolean isReadOnly()
		throws StoreException;

	/**
	 * Puts this connection in read-only mode as a hint to the driver to enable
	 * repository optimizations.
	 * <p>
	 * <b>Note:</b> This method cannot be called during a transaction.
	 * 
	 * @param readOnly
	 *        <tt>true</tt> enables read-only mode; <tt>false</tt> disables it
	 * @throws StoreException
	 *         If a repository access error occurs or this method is called
	 *         during a transaction.
	 */
	public void setReadOnly(boolean readOnly)
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
	 * @param query
	 *        The query to evaluate.
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
	public Cursor<? extends BindingSet> evaluate(QueryModel query, BindingSet bindings, boolean includeInferred)
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
	 *        vararg and as such is optional. If no contexts are specified the
	 *        method operates on the entire repository. A <tt>null</tt> value can
	 *        be used to match context-less statements.
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
	 * Indicates if the connection is in auto-commit mode. The connection is
	 * <em>not</em> in auto-commit when {@link #begin()} has been called but
	 * {@link #commit()} or {@link #rollback()} still has to be called to finish
	 * the transaction.
	 * 
	 * @throws StoreException
	 *         If a repository access error occurs.
	 */
	public boolean isAutoCommit()
		throws StoreException;

	/**
	 * Begins a transaction requiring {@link #commit()} or {@link #rollback()} to
	 * be called to close the transaction.
	 * 
	 * @throws StoreException
	 *         If the connection could not start a transaction, or if it already
	 *         has an active transaction.
	 * @see #isAutoCommit()
	 */
	public void begin()
		throws StoreException;

	/**
	 * Commits any updates that have been performed since the last time
	 * {@link #commit()} or {@link #rollback()} was called.
	 * 
	 * @throws StoreException
	 *         If the SailConnection could not be committed, or if the connection
	 *         does not have an active connection.
	 */
	public void commit()
		throws StoreException;

	/**
	 * Rolls back the SailConnection, discarding any uncommitted changes that
	 * have been made in this SailConnection.
	 * 
	 * @throws StoreException
	 *         If the SailConnection could not be rolled back, or if the
	 *         connection does not have an active connection.
	 */
	public void rollback()
		throws StoreException;

	/**
	 * Adds a statement to the store.
	 * 
	 * @param subj
	 *        The subject of the statement to add.
	 * @param pred
	 *        The predicate of the statement to add.
	 * @param obj
	 *        The object of the statement to add.
	 * @param contexts
	 *        The context(s) to add the statement to. Note that this parameter is
	 *        a vararg and as such is optional. If no contexts are specified, a
	 *        context-less statement will be added.
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
	 *        specified the method operates on the entire repository. A
	 *        <tt>null</tt> value can be used to match context-less statements.
	 * @throws StoreException
	 *         If the statement could not be removed.
	 */
	public void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException;

	/**
	 * Gets the namespaces relevant to the data contained in this Sail object.
	 * 
	 * @return An iterator over the relevant namespaces, should not contain any
	 *         duplicates.
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
