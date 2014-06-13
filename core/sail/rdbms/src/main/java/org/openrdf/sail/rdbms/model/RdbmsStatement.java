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
package org.openrdf.sail.rdbms.model;

import java.util.Optional;

import org.openrdf.model.Resource;
import org.openrdf.model.impl.ContextStatementImpl;

/**
 * Rdbms typed statement.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsStatement extends ContextStatementImpl {

	private static final long serialVersionUID = -5970166748706214658L;

	public RdbmsStatement(RdbmsResource subject, RdbmsURI predicate, RdbmsValue object) {
		this(subject, predicate, object, null);
	}

	public RdbmsStatement(RdbmsResource subject, RdbmsURI predicate, RdbmsValue object, Resource context)
	{
		super(subject, predicate, object, Optional.ofNullable(context));
	}

	@Override
	public RdbmsResource getSubject() {
		return (RdbmsResource)super.getSubject();
	}

	@Override
	public RdbmsURI getPredicate() {
		return (RdbmsURI)super.getPredicate();
	}

	@Override
	public RdbmsValue getObject() {
		return (RdbmsValue)super.getObject();
	}

	@Override
	public Optional<Resource> getContext() {
		return super.getContext();
	}

}
