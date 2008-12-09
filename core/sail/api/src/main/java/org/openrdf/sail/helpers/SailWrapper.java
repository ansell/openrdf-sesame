/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.io.File;

import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailMetaData;
import org.openrdf.sail.StackableSail;
import org.openrdf.store.StoreException;

/**
 * An implementation of the StackableSail interface that wraps another Sail
 * object and forwards any relevant calls to the wrapped Sail.
 * 
 * @author Arjohn Kampman
 */
public class SailWrapper implements StackableSail {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The base Sail for this SailWrapper.
	 */
	private Sail baseSail;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new SailWrapper. The base Sail for the created SailWrapper can
	 * be set later using {@link #setBaseSail}.
	 */
	public SailWrapper() {
	}

	/**
	 * Creates a new SailWrapper that wraps the supplied Sail.
	 */
	public SailWrapper(Sail baseSail) {
		setBaseSail(baseSail);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public SailMetaData getSailMetaData()
		throws StoreException
	{
		return baseSail.getSailMetaData();
	}

	public void setBaseSail(Sail baseSail) {
		this.baseSail = baseSail;
	}

	public Sail getBaseSail() {
		return baseSail;
	}

	protected void verifyBaseSailSet() {
		if (baseSail == null) {
			throw new IllegalStateException("No base Sail has been set");
		}
	}

	public File getDataDir() {
		return baseSail.getDataDir();
	}

	public void setDataDir(File dataDir) {
		baseSail.setDataDir(dataDir);
	}

	public void initialize()
		throws StoreException
	{
		verifyBaseSailSet();
		baseSail.initialize();
	}

	public void shutDown()
		throws StoreException
	{
		verifyBaseSailSet();
		baseSail.shutDown();
	}

	public boolean isWritable()
		throws StoreException
	{
		verifyBaseSailSet();
		return baseSail.isWritable();
	}

	public SailConnection getConnection()
		throws StoreException
	{
		verifyBaseSailSet();
		return baseSail.getConnection();
	}

	public ValueFactory getValueFactory() {
		verifyBaseSailSet();
		return baseSail.getValueFactory();
	}
}
