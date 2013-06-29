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
package org.openrdf.sail.inferencer.fc.config;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.sail.inferencer.fc.CustomGraphQueryInferencer;

/**
 * Configuration schema URI's for {@link CustomGraphQueryInferencer}.
 * 
 * @author Dale Visser
 */
public class CustomGraphQueryInferencerSchema {

	/**
	 * The CustomGraphQueryInferencer schema namespace (
	 * <tt>http://www.openrdf.org/config/sail/customGraphQueryInferencer#</tt>).
	 */
	public static final String NAMESPACE = "http://www.openrdf.org/config/sail/customGraphQueryInferencer#";

	/** <tt>http://www.openrdf.org/config/sail/customGraphQueryInferencer#queryLanguage</tt> */
	public final static URI QUERY_LANGUAGE;

	/** <tt>http://www.openrdf.org/config/sail/customGraphQueryInferencer#ruleQuery</tt> */
	public final static URI RULE_QUERY;

	/** <tt>http://www.openrdf.org/config/sail/customGraphQueryInferencer#matcherQuery</tt> */
	public final static URI MATCHER_QUERY;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		QUERY_LANGUAGE = factory.createURI(NAMESPACE, "queryLanguage");
		RULE_QUERY = factory.createURI(NAMESPACE, "ruleQuery");
		MATCHER_QUERY = factory.createURI(NAMESPACE, "matcherQuery");
	}
}