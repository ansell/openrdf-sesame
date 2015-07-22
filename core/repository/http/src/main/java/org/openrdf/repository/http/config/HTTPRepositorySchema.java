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
package org.openrdf.repository.http.config;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.repository.http.HTTPRepository;

/**
 * Defines constants for the HTTPRepository schema which is used by
 * {@link HTTPRepositoryFactory}s to initialize {@link HTTPRepository}s.
 * 
 * @author Arjohn Kampman
 */
public class HTTPRepositorySchema {

	/** The HTTPRepository schema namespace (<tt>http://www.openrdf.org/config/repository/http#</tt>). */
	public static final String NAMESPACE = "http://www.openrdf.org/config/repository/http#";

	/** <tt>http://www.openrdf.org/config/repository/http#repositoryURL</tt> */
	public final static IRI REPOSITORYURL;

	/** <tt>http://www.openrdf.org/config/repository/http#username</tt> */
	public final static IRI USERNAME;

	/** <tt>http://www.openrdf.org/config/repository/http#password</tt> */
	public final static IRI PASSWORD;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		REPOSITORYURL = factory.createIRI(NAMESPACE, "repositoryURL");
		USERNAME = factory.createIRI(NAMESPACE, "username");
		PASSWORD = factory.createIRI(NAMESPACE, "password");
	}
}
