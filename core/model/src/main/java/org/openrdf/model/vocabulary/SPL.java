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
package org.openrdf.model.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * SPIN Standard Module library.
 */
public final class SPL {

	private SPL() {}

	/**
	 * http://spinrdf.org/spl
	 */
	public static final String NAMESPACE = "http://spinrdf.org/spl#";

	public static final String PREFIX = "spl";

	/**
	 * http://spinrdf.org/spl#Argument Provides metadata about an argument of a SPIN Function or Template.
	 */
	public static final URI ARGUMENT_TEMPLATE;

	/**
	 * http://spinrdf.org/spl#predicate
	 */
	public static final URI PREDICATE_PROPERTY;

	/**
	 * http://spinrdf.org/spl#valueType
	 */
	public static final URI VALUE_TYPE_PROPERTY;

	/**
	 * http://spinrdf.org/spl#optional
	 */
	public static final URI OPTIONAL_PROPERTY;

	/**
	 * http://spinrdf.org/spl#defaultValue
	 */
	public static final URI DEFAULT_VALUE_PROPERTY;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		ARGUMENT_TEMPLATE = factory.createURI(NAMESPACE, "Argument");
		PREDICATE_PROPERTY = factory.createURI(NAMESPACE, "predicate");
		VALUE_TYPE_PROPERTY = factory.createURI(NAMESPACE, "valueType");
		OPTIONAL_PROPERTY = factory.createURI(NAMESPACE, "optional");
		DEFAULT_VALUE_PROPERTY = factory.createURI(NAMESPACE, "defaultValue");
	}
}
