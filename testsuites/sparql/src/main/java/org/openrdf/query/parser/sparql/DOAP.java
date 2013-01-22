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
package org.openrdf.query.parser.sparql;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Constants for DOAP primitives and for the DOAP namespace.
 */
public class DOAP {

	public static final String NAMESPACE = "http://usefulinc.com/ns/doap#";

	public final static URI PROJECT;

	public final static URI NAME;

	public final static URI RELEASE;

	public final static URI VERSION;

	public final static URI CREATED;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		PROJECT = factory.createURI(DOAP.NAMESPACE, "Project");
		NAME = factory.createURI(DOAP.NAMESPACE, "name");
		RELEASE = factory.createURI(DOAP.NAMESPACE, "release");
		VERSION = factory.createURI(DOAP.NAMESPACE, "Version");
		CREATED = factory.createURI(DOAP.NAMESPACE, "created");
	}
}
