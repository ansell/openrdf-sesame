/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.io.File;

import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailChangedListener;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.StackableSail;

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
	private Sail _baseSail;

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

	// Implements StackableSail.setBaseSail(...)
	public void setBaseSail(Sail baseSail) {
		_baseSail = baseSail;
	}

	// Implements StackableSail.getBaseSail()
	public Sail getBaseSail() {
		return _baseSail;
	}

	private void _verifyBaseSailSet() {
		if (_baseSail == null) {
			throw new IllegalStateException("No base Sail has been set");
		}
	}

	// Implements Sail.setParameter(...)
	public void setParameter(String key, String value) {
	}

	// Implements Sail.initialize()
	public void initialize()
		throws SailException
	{
		_verifyBaseSailSet();
		_baseSail.initialize();
	}

	// Implements Sail.shutDown()
	public void shutDown() throws SailException {
		_verifyBaseSailSet();
		_baseSail.shutDown();
	}

	// Implements Sail.isWritable()
	public boolean isWritable() throws SailException {
		_verifyBaseSailSet();
		return _baseSail.isWritable();
	}

	// Implements Sail.getConnection()
	public SailConnection getConnection()
		throws SailException
	{
		_verifyBaseSailSet();
		return _baseSail.getConnection();
	}

	// Implements Sail.getValueFactory()
	public ValueFactory getValueFactory() {
		_verifyBaseSailSet();
		return _baseSail.getValueFactory();
	}

	// Implements Sail.addSailChangedListener(...)
	public void addSailChangedListener(SailChangedListener listener) {
		_verifyBaseSailSet();
		_baseSail.addSailChangedListener(listener);
	}

	// Implements Sail.removeSailChangedListener(...)
	public void removeSailChangedListener(SailChangedListener listener) {
		_verifyBaseSailSet();
		_baseSail.removeSailChangedListener(listener);
	}

	public File getDataDir() {
		return _baseSail.getDataDir();
	}

	public void setDataDir(File dataDir) {
		_baseSail.setDataDir(dataDir);
	}
}
