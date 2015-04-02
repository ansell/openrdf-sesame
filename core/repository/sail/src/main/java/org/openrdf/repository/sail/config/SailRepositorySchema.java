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
package org.openrdf.repository.sail.config;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.repository.sail.SailRepository;

/**
 * Defines constants for the SailRepository schema which is used by
 * {@link SailRepositoryFactory}s to initialize {@link SailRepository}s.
 * 
 * @author Arjohn Kampman
 */
public class SailRepositorySchema {

	/** The SailRepository schema namespace (<tt>http://www.openrdf.org/config/repository/sail#</tt>). */
	public static final String NAMESPACE = "http://www.openrdf.org/config/repository/sail#";

	/** <tt>http://www.openrdf.org/config/repository/sail#sailImpl</tt> */
	public final static IRI SAILIMPL;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		SAILIMPL = factory.createIRI(NAMESPACE, "sailImpl");
	}
}
