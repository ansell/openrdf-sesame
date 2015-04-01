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
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * @author Arjohn Kampman
 */
public class DAWGTestResultSetSchema {

	public static final String NAMESPACE = "http://www.w3.org/2001/sw/DataAccess/tests/result-set#";

	public static final IRI RESULTSET;

	public static final IRI RESULTVARIABLE;

	public static final IRI SOLUTION;

	public static final IRI BINDING;

	public static final IRI VALUE;

	public static final IRI VARIABLE;

	public static final IRI BOOLEAN;

	public static final Literal TRUE;

	public static final Literal FALSE;

	static {
		ValueFactory vf = ValueFactoryImpl.getInstance();
		RESULTSET = vf.createIRI(NAMESPACE, "ResultSet");
		RESULTVARIABLE = vf.createIRI(NAMESPACE, "resultVariable");
		SOLUTION = vf.createIRI(NAMESPACE, "solution");
		BINDING = vf.createIRI(NAMESPACE, "binding");
		VALUE = vf.createIRI(NAMESPACE, "value");
		VARIABLE = vf.createIRI(NAMESPACE, "variable");
		BOOLEAN = vf.createIRI(NAMESPACE, "boolean");
		TRUE = vf.createLiteral(true);
		FALSE = vf.createLiteral(false);
	}
}
