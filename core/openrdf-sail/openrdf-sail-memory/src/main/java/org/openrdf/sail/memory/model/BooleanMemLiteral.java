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

	/**
	 * 
	 */
	private static final long serialVersionUID = 8061173551677475700L;

	private boolean b;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BooleanMemLiteral(Object creator, boolean b) {
		this(creator, Boolean.toString(b), b);
	}

	public BooleanMemLiteral(Object creator, String label, boolean b) {
		super(creator, label, XMLSchema.BOOLEAN);
		this.b = b;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public boolean booleanValue()
	{
		return b;
	}
}
