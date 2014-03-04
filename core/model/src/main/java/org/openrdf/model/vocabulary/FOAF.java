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
 * Constants for FOAF primitives and for the FOAF namespace.<br>
 * TODO: Currently only a minimal set. Needs expanding.
 */
public class FOAF {

	/**
	 * The FOAF namespace: http://xmlns.com/foaf/0.1/
	 */
	public static final String NAMESPACE = "http://xmlns.com/foaf/0.1/";

	/**
	 * The recommended prefix for the FOAF namespace: "foaf"
	 */
	public static final String PREFIX = "foaf";

	public final static URI PERSON;

	public final static URI NAME;

	public final static URI KNOWS;

	public static final URI MBOX;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		PERSON = factory.createURI(FOAF.NAMESPACE, "Person");

		NAME = factory.createURI(FOAF.NAMESPACE, "name");

		KNOWS = factory.createURI(FOAF.NAMESPACE, "knows");

		MBOX = factory.createURI(FOAF.NAMESPACE, "mbox");
	}
}
