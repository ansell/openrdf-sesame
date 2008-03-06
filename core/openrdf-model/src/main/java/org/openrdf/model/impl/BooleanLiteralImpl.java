/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import org.openrdf.model.vocabulary.XMLSchema;

/**
 * An extension of {@link LiteralImpl} that stores a boolean value to avoid
 * parsing.
 * 
 * @author David Huynh
 */
public class BooleanLiteralImpl extends LiteralImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7289753804000730608L;
	
	private boolean _b;

	/**
	 * Creates an xsd:boolean typed literal with the specified value.
	 */
	public BooleanLiteralImpl(boolean b) {
		super(Boolean.toString(b), XMLSchema.BOOLEAN);
		_b = b;
	}

	@Override
	public boolean booleanValue()
	{
		return _b;
	}
}
