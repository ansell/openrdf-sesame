/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailChangedEvent;
import org.openrdf.sail.SailChangedListener;

/**
 * @author Herko ter Horst
 */
public abstract class SailBase implements Sail {

	/**
	 * Directory to store information related to this sail in.
	 */
	private File _dataDir = null;

	/**
	 * Objects that should be notified of changes to the data in this Sail.
	 */
	private Set<SailChangedListener> _sailChangedListeners = new LinkedHashSet<SailChangedListener>(0);

	public void setParameter(String key, String value) {
		if (DATA_DIR_KEY.equals(key)) {
			setDataDir(new File(value));
		}
	}

	public void setDataDir(File dataDir) {
		_dataDir = dataDir;
	}

	public File getDataDir() {
		return _dataDir;
	}

	public void addSailChangedListener(SailChangedListener listener) {
		synchronized (_sailChangedListeners) {
			_sailChangedListeners.add(listener);
		}
	}

	public void removeSailChangedListener(SailChangedListener listener) {
		synchronized (_sailChangedListeners) {
			_sailChangedListeners.remove(listener);
		}
	}

	/**
	 * Notifies all registered SailChangedListener's of changes to the contents
	 * of this Sail.
	 */
	public void notifySailChanged(SailChangedEvent event) {
		synchronized (_sailChangedListeners) {
			for (SailChangedListener l : _sailChangedListeners) {
				l.sailChanged(event);
			}
		}
	}
}
