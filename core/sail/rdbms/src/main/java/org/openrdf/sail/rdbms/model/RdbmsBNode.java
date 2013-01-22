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

import org.openrdf.model.BNode;
import org.openrdf.model.impl.BNodeImpl;

/**
 * Wraps a {@link BNodeImpl} providing an internal id and version.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsBNode extends RdbmsResource implements BNode {

	private static final long serialVersionUID = 861142250999359435L;

	private BNode bnode;

	public RdbmsBNode(BNode bnode) {
		this.bnode = bnode;
	}

	public RdbmsBNode(Number id, Integer version, BNode bnode) {
		super(id, version);
		this.bnode = bnode;
	}

	public String getID() {
		return bnode.getID();
	}

	public String stringValue() {
		return bnode.stringValue();
	}

	@Override
	public String toString() {
		return bnode.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		return bnode.equals(o);
	}

	@Override
	public int hashCode() {
		return bnode.hashCode();
	}

}
