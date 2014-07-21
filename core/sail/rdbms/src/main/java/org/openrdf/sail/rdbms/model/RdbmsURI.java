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

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * Wraps a {@link URIImpl} providing an internal id and version.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsURI extends RdbmsResource implements URI {

	private static final long serialVersionUID = 3317398596013196032L;

	private URI uri;

	public RdbmsURI(URI uri) {
		this.uri = uri;
	}

	public RdbmsURI(Number id, Integer version, URI uri) {
		super(id, version);
		this.uri = uri;
	}

	public String getLocalName() {
		return uri.getLocalName();
	}

	public String getNamespace() {
		return uri.getNamespace();
	}

	public String stringValue() {
		return uri.stringValue();
	}

	@Override
	public String toString() {
		return uri.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		return uri.equals(o);
	}

	@Override
	public int hashCode() {
		return uri.hashCode();
	}

	@Override
	public String ntriplesString() {
		return uri.ntriplesString();
	}

	@Override
	public String getIRIString() {
		return uri.getIRIString();
	}
}
