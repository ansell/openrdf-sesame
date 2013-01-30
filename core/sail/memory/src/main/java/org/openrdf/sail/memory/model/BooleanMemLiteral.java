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
package org.openrdf.sail.memory.model;

import org.openrdf.model.vocabulary.XMLSchema;

/**
 * An extension of MemLiteral that stores a boolean value to avoid parsing.
 * 
 * @author David Huynh
 * @author Arjohn Kampman
 */
public class BooleanMemLiteral extends MemLiteral {

	private static final long serialVersionUID = 8061173551677475700L;

	/*-----------*
	 * Variables *
	 *-----------*/

	private final boolean b;

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
	public boolean booleanValue() {
		return b;
	}
}
