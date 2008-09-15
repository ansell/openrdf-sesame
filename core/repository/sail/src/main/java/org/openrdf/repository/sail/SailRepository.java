/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import java.io.File;

import org.openrdf.StoreException;
import org.openrdf.model.LiteralFactory;
import org.openrdf.model.URIFactory;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryMetaData;
import org.openrdf.sail.Sail;

/**
 * An implementation of the {@link Repository} interface that operates on a
 * (stack of) {@link Sail Sail} object(s). The behaviour of the repository is
 * determined by the Sail stack that it operates on; for example, the repository
 * will only support RDF Schema or OWL semantics if the Sail stack includes an
 * inferencer for this.
 * <p>
 * Creating a repository object of this type is very easy. For example, the
 * following code creates and initializes a main-memory store with RDF Schema
 * semantics:
 * 
 * <pre>
 * Repository repository = new RepositoryImpl(new ForwardChainingRDFSInferencer(new MemoryStore()));
 * repository.initialize();
 * </pre>
 * 
 * Or, alternatively:
 * 
 * <pre>
 * Sail sailStack = new MemoryStore();
 * sailStack = new ForwardChainingRDFSInferencer(sailStack);
 * 
 * Repository repository = new Repository(sailStack);
 * repository.initialize();
 * </pre>
 * 
 * @author Arjohn Kampman
 */
public class SailRepository implements Repository {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final Sail sail;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new repository object that operates on the supplied Sail.
	 * 
	 * @param sail
	 *        A Sail object.
	 */
	public SailRepository(Sail sail) {
		this.sail = sail;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public RepositoryMetaData getRepositoryMetaData() {
		return new SailRepositoryMetaData(sail.getSailMetaData());
	}

	public File getDataDir() {
		return sail.getDataDir();
	}

	public void setDataDir(File dataDir) {
		sail.setDataDir(dataDir);
	}

	public void initialize()
		throws StoreException
	{
		sail.initialize();
	}

	public void shutDown()
		throws StoreException
	{
		sail.shutDown();
	}

	/**
	 * Gets the Sail object that is on top of the Sail stack that this repository
	 * operates on.
	 * 
	 * @return A Sail object.
	 */
	public Sail getSail() {
		return sail;
	}

	public boolean isWritable()
		throws StoreException
	{
		return sail.isWritable();
	}

	public LiteralFactory getLiteralFactory() {
		return sail.getValueFactory();
	}

	public URIFactory getURIFactory() {
		return sail.getValueFactory();
	}

	public ValueFactory getValueFactory() {
		return sail.getValueFactory();
	}

	public SailRepositoryConnection getConnection()
		throws StoreException
	{
		return new SailRepositoryConnection(this, sail.getConnection());
	}
}
