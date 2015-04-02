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

import org.openrdf.model.Namespace;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleNamespace;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Defines constants for the Sesame QName schema namespace.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class SESAMEQNAME {

	/**
	 * The Sesame QName Schema namespace (
	 * <tt>http://www.openrdf.org/schema/qname#</tt>).
	 */
	public static final String NAMESPACE = "http://www.openrdf.org/schema/qname#";

	/**
	 * Recommended prefix for the Sesame QName Schema namespace: "q"
	 */
	public static final String PREFIX = "q";

	/**
	 * An immutable {@link Namespace} constant that represents the Sesame QName
	 * namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

	/** <tt>http://www.openrdf.org/schema/qname#qname</tt> */
	public final static IRI QNAME;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		QNAME = factory.createIRI(SESAMEQNAME.NAMESPACE, "qname");
	}
}
