/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.results.impl;

import org.openrdf.model.Resource;
import org.openrdf.results.ContextResult;
import org.openrdf.results.Cursor;
import org.openrdf.results.base.ResultImpl;

/**
 * @author Jabmes Leigh
 */
public class ContextResultImpl extends ResultImpl<Resource> implements ContextResult {

	public ContextResultImpl(Cursor<? extends Resource> delegate) {
		super(delegate);
	}

}
