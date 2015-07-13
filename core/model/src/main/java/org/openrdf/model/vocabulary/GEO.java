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
 * @since 2.8.5
 * @version 1.0
 * @see http://www.opengeospatial.org/standards/geosparql
 */
public class GEO {

	public static String NAMESPACE = "http://www.opengis.net/ont/geosparql#";

	public static URI AS_WKT;

	public static URI WKT_LITERAL;

	public static final String DEFAULT_SRID = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		AS_WKT = factory.createURI(NAMESPACE, "asWKT");
		WKT_LITERAL = factory.createURI(NAMESPACE, "wktLiteral");
	}
}
