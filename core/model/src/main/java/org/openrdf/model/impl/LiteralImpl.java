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

import org.openrdf.model.IRI;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

/**
 * @author Jeen Broekstra
 * @deprecated since 4.0. Use {@link SimpleLiteral} for extending, and
 *             instantiate using a {@link ValueFactory}.
 */
@Deprecated
public class LiteralImpl extends SimpleLiteral {

	/**
	 * @deprecated since 4.0. Use {@link ValueFactory#createLiteral(String)}
	 *             instead.
	 */
	@Deprecated
	public LiteralImpl(String label) {
		super(label);
	}

	/**
	 * @deprecated since 4.0. Use
	 *             {@link ValueFactory#createLiteral(String, String)} instead.
	 */
	@Deprecated
	public LiteralImpl(String label, String language) {
		super(label, language);
	}

	/**
	 * @deprecated since 4.0. Use {@link ValueFactory#createLiteral(String, IRI)}
	 *             instead.
	 */
	@Deprecated
	public LiteralImpl(String label, URI datatype) {
		super(label, (IRI)datatype);
	}
}
