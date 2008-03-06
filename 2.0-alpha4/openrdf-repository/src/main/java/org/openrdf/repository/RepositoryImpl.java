/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailInitializationException;

import org.openrdf.model.ValueFactory;


/**
 * A default implementation of the {@link Repository} interface. Creating a
 * repository object of this type is very easy. For example, the following code
 * creates and initializes a main-memory store with RDF Schema semantics:
 * 
 * <pre>
 * Repository repository = new RepositoryImpl(
 *     new MemoryStoreRDFSInferencer(
 *     new MemoryStore()));
 * 
 * repository.initialize();
 * </pre>
 * 
 * Or, alternatively:
 * 
 * <pre>
 * Sail sailStack = new MemoryStore();
 * sailStack = new MemoryStoreRDFSInferencer(sailStack);
 * 
 * Repository repository = new Repository(sailStack);
 * repository.initialize();
 * </pre>
 */
public class RepositoryImpl implements Repository {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Sail _sail;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new repository object that operates on the supplied Sail.
	 * 
	 * @param sail
	 *        A Sail object.
	 */
	public RepositoryImpl(Sail sail) {
		_sail = sail;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void initialize()
		throws SailInitializationException
	{
		_sail.initialize();
	}

	public void shutDown() {
		_sail.shutDown();
	}

	public Sail getSail() {
		return _sail;
	}

	public boolean isWritable() {
		return _sail.isWritable();
	}

	public ValueFactory getValueFactory() {
		return _sail.getValueFactory();
	}

	public Connection getConnection()
		throws SailException
	{
		return new ConnectionImpl(this);
	}
}
