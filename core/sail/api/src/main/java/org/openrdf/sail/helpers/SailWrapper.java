/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-208.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.io.File;

import org.openrdf.model.ValueFactory;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailChangedListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.StackableSail;

/**
 * An implementation of the StackableSail interface that wraps another Sail
 * object and forwards any relevant calls to the wrapped Sail.
 * 
 * @author Arjohn Kampman
 */
public class SailWrapper implements StackableSail, NotifyingSail {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The base Sail for this SailWrapper.
	 */
	private NotifyingSail baseSail;

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
	public SailWrapper(NotifyingSail baseSail) {
		setBaseSail(baseSail);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setBaseSail(Sail baseSail) {
		this.baseSail = (NotifyingSail) baseSail;
	}

	public Sail getBaseSail() {
		return baseSail;
	}

	private void verifyBaseSailSet() {
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
		throws SailException
	{
		verifyBaseSailSet();
		baseSail.initialize();
	}

	public void shutDown()
		throws SailException
	{
		verifyBaseSailSet();
		baseSail.shutDown();
	}

	public boolean isWritable()
		throws SailException
	{
		verifyBaseSailSet();
		return baseSail.isWritable();
	}

	public NotifyingSailConnection getConnection()
		throws SailException
	{
		verifyBaseSailSet();
		return baseSail.getConnection();
	}

	public ValueFactory getValueFactory() {
		verifyBaseSailSet();
		return baseSail.getValueFactory();
	}

	public void addSailChangedListener(SailChangedListener listener) {
		verifyBaseSailSet();
		baseSail.addSailChangedListener(listener);
	}

	public void removeSailChangedListener(SailChangedListener listener) {
		verifyBaseSailSet();
		baseSail.removeSailChangedListener(listener);
	}
}
