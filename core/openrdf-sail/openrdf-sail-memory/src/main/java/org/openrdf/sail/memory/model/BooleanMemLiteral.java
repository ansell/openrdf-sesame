/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import org.openrdf.model.vocabulary.XMLSchema;

/**
 * An extension of MemLiteral that stores a boolean value to avoid parsing.
 * 
 * @author David Huynh
 * @author Arjohn Kampman
 */
public class BooleanMemLiteral extends MemLiteral {

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean _b;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BooleanMemLiteral(Object creator, boolean b) {
		super(creator, Boolean.toString(b), XMLSchema.BOOLEAN);
		_b = b;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public boolean booleanValue()
	{
		return _b;
	}
}
