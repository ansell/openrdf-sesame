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
package org.openrdf.repository.config;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Defines constants for the repository configuration schema that is used by
 * {@link org.openrdf.repository.manager.RepositoryManager}s.
 * 
 * @author Arjohn Kampman
 */
public class RepositoryConfigSchema {

	/** The HTTPRepository schema namespace (<tt>http://www.openrdf.org/config/repository#</tt>). */
	public static final String NAMESPACE = "http://www.openrdf.org/config/repository#";

	/** <tt>http://www.openrdf.org/config/repository#RepositoryContext</tt> */
	public final static IRI REPOSITORY_CONTEXT;

	/** <tt>http://www.openrdf.org/config/repository#Repository</tt> */
	public final static IRI REPOSITORY;

	/** <tt>http://www.openrdf.org/config/repository#repositoryID</tt> */
	public final static IRI REPOSITORYID;

	/** <tt>http://www.openrdf.org/config/repository#repositoryImpl</tt> */
	public final static IRI REPOSITORYIMPL;

	/** <tt>http://www.openrdf.org/config/repository#repositoryType</tt> */
	public final static IRI REPOSITORYTYPE;

	/** <tt>http://www.openrdf.org/config/repository#delegate</tt> */
	public final static IRI DELEGATE;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		REPOSITORY_CONTEXT = factory.createIRI(NAMESPACE, "RepositoryContext");
		REPOSITORY = factory.createIRI(NAMESPACE, "Repository");
		REPOSITORYID = factory.createIRI(NAMESPACE, "repositoryID");
		REPOSITORYIMPL = factory.createIRI(NAMESPACE, "repositoryImpl");
		REPOSITORYTYPE = factory.createIRI(NAMESPACE, "repositoryType");
		DELEGATE = factory.createIRI(NAMESPACE, "delegate");
	}
}
