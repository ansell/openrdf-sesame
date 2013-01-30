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
package org.openrdf.query.dawg;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * @author Arjohn Kampman
 */
public class DAWGTestResultSetSchema {

	public static final String NAMESPACE = "http://www.w3.org/2001/sw/DataAccess/tests/result-set#";

	public static final URI RESULTSET;

	public static final URI RESULTVARIABLE;

	public static final URI SOLUTION;

	public static final URI BINDING;

	public static final URI VALUE;

	public static final URI VARIABLE;

	public static final URI BOOLEAN;

	public static final Literal TRUE;

	public static final Literal FALSE;

	static {
		ValueFactory vf = ValueFactoryImpl.getInstance();
		RESULTSET = vf.createURI(NAMESPACE, "ResultSet");
		RESULTVARIABLE = vf.createURI(NAMESPACE, "resultVariable");
		SOLUTION = vf.createURI(NAMESPACE, "solution");
		BINDING = vf.createURI(NAMESPACE, "binding");
		VALUE = vf.createURI(NAMESPACE, "value");
		VARIABLE = vf.createURI(NAMESPACE, "variable");
		BOOLEAN = vf.createURI(NAMESPACE, "boolean");
		TRUE = vf.createLiteral(true);
		FALSE = vf.createLiteral(false);
	}
}
