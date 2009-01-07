/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.result.impl;

import org.openrdf.cursor.Cursor;
import org.openrdf.model.Resource;
import org.openrdf.result.ContextResult;

/**
 * @author Jabmes Leigh
 */
public class ContextResultImpl extends ResultImpl<Resource> implements ContextResult {

	public ContextResultImpl(Cursor<? extends Resource> delegate) {
		super(delegate);
	}
}
