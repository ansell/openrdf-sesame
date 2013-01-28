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
package org.openrdf.sail.rdbms.config;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary for the RDBMS configuration.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsStoreSchema {

	public static final String NAMESPACE = "http://www.openrdf.org/config/sail/rdbms#";

	public final static URI JDBC_DRIVER;

	public final static URI URL;

	public final static URI USER;

	public final static URI PASSWORD;

	public final static URI MAX_TRIPLE_TABLES;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		JDBC_DRIVER = factory.createURI(NAMESPACE, "jdbcDriver");
		URL = factory.createURI(NAMESPACE, "url");
		USER = factory.createURI(NAMESPACE, "user");
		PASSWORD = factory.createURI(NAMESPACE, "password");
		MAX_TRIPLE_TABLES = factory.createURI(NAMESPACE, "maxTripleTables");
	}
}
