/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.querymodel.GraphQuery;
import org.openrdf.querymodel.TupleQuery;
import org.openrdf.queryresult.GraphQueryResult;
import org.openrdf.queryresult.TupleQueryResult;
import org.openrdf.util.iterator.CloseableIterator;

/**
 * A connection to an RDF Sail object. A SailConnection is active from the
 * moment it is created until it is closed. Care should be taken to properly
 * close SailConnections as they might block concurrent queries and/or updates
 * on the Sail while active, depending on the Sail-implementation that is being
 * used.
 * 
 * @author jeen
 */
public interface SailConnection {

	/**
	 * Checks whether this SailConnection is open. A SailConnection is open from
	 * the moment it is created until it is closed.
	 * 
	 * @see SailConnection#close
	 */
	public boolean isOpen();

	/**
	 * Closes the connection. Any updates that haven't been committed yet will be
	 * rolled back. The connection can no longer be used once it is closed.
	 */
	public void close()
		throws SailException;

	/**
	 * Evaluates the supplied TupleQuery on the data contained in this Sail
	 * object.
	 * 
	 * @param tupleQuery
	 *        The TupleQuery to evaluate.
	 * @param includeInferred
	 *        Indicates whether inferred triples are to be considered in the
	 *        query result. If false, no inferred statements are returned; if
	 *        true, inferred statements are returned if available
	 * @return The TupleQueryResult.
	 * @throws SailInternalException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 */
	public TupleQueryResult evaluate(TupleQuery tupleQuery, boolean includeInferred);

	/**
	 * Evaluates the supplied query on the data contained in this Sail object.
	 * 
	 * @param graphQuery
	 *        The graph query to evaluate.
	 * @param includeInferred
	 *        Indicates whether inferred triples are to be considered in the
	 *        query result. If false, no inferred statements are returned; if
	 *        true, inferred statements are returned if available
	 * @return The query result.
	 * @throws SailInternalException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 */
	public GraphQueryResult evaluate(GraphQuery graphQuery, boolean includeInferred);

	/**
	 * Gets all resources that are used as content identifiers.
	 * 
	 * @return An iterator over the context identifiers, should not contain any
	 *         duplicates.
	 */
	public CloseableIterator<? extends Resource> getContextIDs();

	/**
	 * Gets all statements from all contexts that have a specific subject,
	 * predicate and/or object. All three parameters may be null to indicate
	 * wildcards. The <tt>useInference</tt> can be used to control which
	 * statements are fetched: all statements or only the statements that have
	 * been added explicitly.
	 * 
	 * @param subj
	 *        A Resource specifying the subject, or <tt>null</tt> for a
	 *        wildcard.
	 * @param pred
	 *        A URI specifying the predicate, or <tt>null</tt> for a wildcard.
	 * @param obj
	 *        A Value specifying the object, or <tt>null</tt> for a wildcard.
	 * @param includeInferred
	 *        if false, no inferred statements are returned; if true, inferred
	 *        statements are returned if available
	 * @return An iterator over the relevant statements.
	 * @throws SailInternalException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 */
	public CloseableIterator<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred);

	/**
	 * Gets all statements from the null context that have a specific subject,
	 * predicate and/or object. All three parameters may be null to indicate
	 * wildcards. The <tt>useInference</tt> can be used to control which
	 * statements are fetched: all statements or only the statements that have
	 * been added explicitly.
	 * 
	 * @param subj
	 *        A Resource specifying the subject, or <tt>null</tt> for a
	 *        wildcard.
	 * @param pred
	 *        A URI specifying the predicate, or <tt>null</tt> for a wildcard.
	 * @param obj
	 *        A Value specifying the object, or <tt>null</tt> for a wildcard.
	 * @param includeInferred
	 *        if false, no inferred statements are returned; if true, inferred
	 *        statements are returned if available
	 * @return An iterator over the relevant statements.
	 * @throws SailInternalException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 */
	public CloseableIterator<? extends Statement> getNullContextStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred);

	/**
	 * Gets all statements from the named contexts (excluding the null context)
	 * that have a specific subject, predicate, object and/or context. All four
	 * parameters may be null to indicate wildcards. The <tt>useInference</tt>
	 * can be used to control which statements are fetched: all statements or
	 * only the statements that have been added explicitly.
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
	 * @param includeInferred
	 *        if false, no inferred statements are returned; if true, inferred
	 *        statements are returned if available
	 * @return An iterator over the relevant statements.
	 * @throws SailInternalException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 */
	public CloseableIterator<? extends Statement> getNamedContextStatements(Resource subj, URI pred,
			Value obj, Resource context, boolean useInference);

	/**
	 * Gets the namespaces relevant to the data contained in this Sail object.
	 * 
	 * @returns An iterator over the relevant namespaces, should not contain any
	 *          duplicates.
	 * @throws SailInternalException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 */
	public CloseableIterator<? extends Namespace> getNamespaces();

	/**
	 * Commits any updates that have been performed since the last time
	 * {@link #commit()} or {@link #rollback()} was called.
	 * 
	 * @throws SailException
	 *         If the SailConnection could not be committed.
	 * @throws SailInternalException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 */
	public void commit()
		throws SailException;

	/**
	 * Rolls back the SailConnection, discarding any uncommitted changes that
	 * have been made in this SailConnection.
	 * 
	 * @throws SailException
	 *         If the SailConnection could not be rolled back.
	 * @throws SailInternalException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 */
	public void rollback()
		throws SailException;

	/**
	 * Adds a statement to a specific context.
	 * 
	 * @param subj
	 *        The subject of the statement to add.
	 * @param pred
	 *        The predicate of the statement to add.
	 * @param obj
	 *        The object of the statement to add.
	 * @param context
	 *        A resource identifying the named context to add the statement to,
	 *        or <tt>null</tt> to add the statement to the null context.
	 * @throws SailException
	 *         If the statement could not be added.
	 * @throws SailInternalException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 */
	public void addStatement(Resource subj, URI pred, Value obj, Resource context)
		throws SailException;

	/**
	 * Removes a statement from a specific context.
	 * 
	 * @param subj
	 *        The subject of the statement that should be removed.
	 * @param pred
	 *        The predicate of the statement that should be removed.
	 * @param obj
	 *        The object of the statement that should be removed.
	 * @param context
	 *        A resource identifying the named context to remove the statement
	 *        from, or <tt>null</tt> to remove the statement from the null
	 *        context.
	 * @throws SailException
	 *         If the statement could not be removed.
	 * @throws SailInternalException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 */
	public void removeStatement(Resource subj, URI pred, Value obj, Resource context)
		throws SailException;

	/**
	 * Removes all statements from all contexts. A faster alternative for
	 * clearing a repository if no SailConnection management is needed is
	 * {@link Sail#clear}.
	 * 
	 * @throws SailException
	 *         If the statements could not be removed.
	 * @throws SailInternalException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 * @see Sail#clear
	 */
	public void clear()
		throws SailException;

	/**
	 * Removes all statements from a specific context.
	 * 
	 * @param context
	 *        A resource identifying the named context to clear, or <tt>null</tt>
	 *        to clear the null context.
	 * @throws SailException
	 *         If the statements could not be removed.
	 * @throws SailInternalException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 */
	public void clearContext(Resource context)
		throws SailException;

	/**
	 * Sets the prefix of a namespace. The new prefix must be unique; it is not
	 * allowed to be associated with any other namespace.
	 * 
	 * @param prefix
	 *        The new prefix.
	 * @param name
	 *        The namespace name for which the associated prefix should be
	 *        changed.
	 * @throws SailException
	 *         If the namespace prefix could not be changed, possibly because the
	 *         prefix is already used for another namespace.
	 * @throws SailInternalException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 */
	public void setNamespace(String prefix, String name)
		throws SailException;

	/**
	 * Removes a namespace declaration by removing the association between a
	 * prefix and a namespace name.
	 * 
	 * @param prefix
	 *        The namespace prefix of which the assocation with a namespace name
	 *        is to be removed.
	 * @throws SailException
	 *         If the namespace prefix could not be removed.
	 */
	public void removeNamespace(String prefix)
		throws SailException;

	/**
	 * Registers a SailConnection listener with this SailConnection. The listener
	 * should be notified of any statements that are added or removed as part of
	 * this SailConnection.
	 * 
	 * @param listener
	 *        A SailConnectionListener.
	 */
	public void addConnectionListener(SailConnectionListener listener);

	/**
	 * Deregisters a SailConnection listener with this SailConnection.
	 * 
	 * @param listener
	 *        A SailConnectionListener.
	 */
	public void removeConnectionListener(SailConnectionListener listener);

}
