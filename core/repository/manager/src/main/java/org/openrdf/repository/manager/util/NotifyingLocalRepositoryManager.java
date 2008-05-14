/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.util;

import java.io.File;
import java.util.ArrayList;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.manager.LocalRepositoryManager;

/**
 * NotifyingLocalRepositoryManager extends LocalRepositoryManager with support
 * for registering listeners.
 * 
 * In time this class is likely to become redundant as RepositoryManager may be
 * extended with listener support.
 * 
 * This functionality can currently not be implemented as a wrapper around any
 * existing RepositoryManager due to the fact that RepositoryManager defines
 * abstract protected methods. A wrapper class cannot implement these methods in
 * a meaningful way by itself and, because of the protected access, cannot
 * invoke it on the wrapped RepositoryManager either.
 */
public class NotifyingLocalRepositoryManager extends LocalRepositoryManager {

	private ArrayList<RepositoryManagerListener> listeners;
	
	public NotifyingLocalRepositoryManager(File baseDir) {
		super(baseDir);
		listeners = new ArrayList<RepositoryManagerListener>();
	}

	public void addRepositoryManagerListener(RepositoryManagerListener listener) {
		listeners.add(listener);
	}
	
	public void removeRepositoryManagerListener(RepositoryManagerListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public void initialize() throws RepositoryException {
		super.initialize();
		fireInitialized();
	}
	
	@Override
	public void refresh() {
		super.refresh();
		fireRefreshed();
	}
	
	@Override
	public void shutDown() {
		super.shutDown();
		fireShutDown();
	}
	
	private void fireInitialized() {
		for (RepositoryManagerListener listener : listeners) {
			listener.initialized(this);
		}
	}
	
	private void fireRefreshed() {
		for (RepositoryManagerListener listener : listeners) {
			listener.refreshed(this);
		}
	}
	
	private void fireShutDown() {
		for (RepositoryManagerListener listener : listeners) {
			listener.shutDown(this);
		}
	}
}
