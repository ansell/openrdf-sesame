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
package org.openrdf.repository.contextaware.config;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * @author James Leigh
 */
public class ContextAwareSchema {

	/**
	 * The ContextAwareRepository schema namespace (
	 * <tt>http://www.openrdf.org/config/repository/contextaware#</tt>).
	 */
	public static final String NAMESPACE = "http://www.openrdf.org/config/repository/contextaware#";

	/** <tt>http://www.openrdf.org/config/repository/contextaware#includeInferred</tt> */
	public final static IRI INCLUDE_INFERRED;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#maxQueryTime</tt> */
	public final static IRI MAX_QUERY_TIME;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#queryLanguage</tt> */
	public final static IRI QUERY_LANGUAGE;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#base</tt> */
	public final static IRI BASE_URI;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#readContext</tt> */
	public final static IRI READ_CONTEXT;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#addContext</tt> */
	@Deprecated
	public final static IRI ADD_CONTEXT;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#removeContext</tt> */
	public final static IRI REMOVE_CONTEXT;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#archiveContext</tt> */
	@Deprecated
	public final static IRI ARCHIVE_CONTEXT;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#insertContext</tt> */
	public final static IRI INSERT_CONTEXT;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		INCLUDE_INFERRED = factory.createIRI(NAMESPACE, "includeInferred");
		QUERY_LANGUAGE = factory.createIRI(NAMESPACE, "ql");
		BASE_URI = factory.createIRI(NAMESPACE, "base");
		READ_CONTEXT = factory.createIRI(NAMESPACE, "readContext");
		ADD_CONTEXT = factory.createIRI(NAMESPACE, "addContext");
		REMOVE_CONTEXT = factory.createIRI(NAMESPACE, "removeContext");
		ARCHIVE_CONTEXT = factory.createIRI(NAMESPACE, "archiveContext");
		INSERT_CONTEXT = factory.createIRI(NAMESPACE, "insertContext");
		MAX_QUERY_TIME = factory.createIRI(NAMESPACE, "maxQueryTime");
	}
}
