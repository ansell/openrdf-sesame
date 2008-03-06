/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol;

import org.openrdf.repository.RepositoryException;

/**
 * @author Herko ter Horst
 */
public class NotAllowedException extends RepositoryException {

	private static final long serialVersionUID = -1753268902269152319L;

	public NotAllowedException() {
	}

	public NotAllowedException(String msg) {
		super(msg);
	}

	public NotAllowedException(Throwable t) {
		super(t);
	}

	public NotAllowedException(String msg, Throwable t) {
		super(msg, t);
	}
}
