/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
