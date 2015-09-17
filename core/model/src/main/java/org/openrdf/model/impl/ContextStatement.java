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

import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;

/**
 * An extension of {@link SimpleStatement} that adds a context field.
 */
public class ContextStatement extends SimpleStatement {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = -4747275587477906748L;

	/**
	 * The statement's context, if applicable.
	 */
	private final Resource context;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new Statement with the supplied subject, predicate and object
	 * for the specified associated context.
	 * 
	 * @param subject
	 *        The statement's subject, must not be <tt>null</tt>.
	 * @param predicate
	 *        The statement's predicate, must not be <tt>null</tt>.
	 * @param object
	 *        The statement's object, must not be <tt>null</tt>.
	 * @param context
	 *        The statement's context, <tt>null</tt> to indicate no context is
	 *        associated.
	 */
	protected ContextStatement(Resource subject, IRI predicate, Value object, Resource context) {
		super(subject, predicate, object);
		this.context = context;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public Resource getContext()
	{
		return context;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(256);

		sb.append(super.toString());
		sb.append(" [").append(getContext()).append("]");

		return sb.toString();
	}
}
