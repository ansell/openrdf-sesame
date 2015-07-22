/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.model.impl;

import org.openrdf.model.vocabulary.XMLSchema;

/**
 * An extension of {@link SimpleLiteral} that stores a boolean value to avoid
 * parsing.
 * 
 * @author David Huynh
 * @author Arjohn Kampman
 */
public class BooleanLiteral extends SimpleLiteral {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = -3610638093719366795L;

	public static final BooleanLiteral TRUE = new BooleanLiteral(true);

	public static final BooleanLiteral FALSE = new BooleanLiteral(false);

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates an xsd:boolean typed literal with the specified value.
	 */
	public BooleanLiteral(boolean value) {
		super(Boolean.toString(value), XMLSchema.BOOLEAN);
		this.value = value;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public boolean booleanValue()
	{
		return value;
	}

	/**
	 * Returns a {@link BooleanLiteral} for the specified value. This method
	 * uses the constants {@link #TRUE} and {@link #FALSE} as result values,
	 * preventing the often unnecessary creation of new
	 * {@link BooleanLiteral} objects.
	 */
	public static BooleanLiteral valueOf(boolean value) {
		return value ? TRUE : FALSE;
	}
}
