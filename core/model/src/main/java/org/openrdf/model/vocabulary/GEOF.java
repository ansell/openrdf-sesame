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
 * @since 2.8.14
 * @version 1.0
 * @see http://www.opengeospatial.org/standards/geosparql
 */
public class GEOF {

	private static final String NAMESPACE = "http://www.opengis.net/def/function/geosparql/";

	public static final URI DISTANCE;
	public static final URI BUFFER;
	public static final URI CONVEX_HULL;
	public static final URI INTERSECTION;
	public static final URI UNION;
	public static final URI DIFFERENCE;
	public static final URI SYM_DIFFERENCE;
	public static final URI ENVELOPE;
	public static final URI BOUNDARY;
	public static final URI GET_SRID;

	public static final String UOM_NAMESPACE = "http://www.opengis.net/def/uom/OGC/1.0/";
	public static final URI UOM_DEGREE;
	public static final URI UOM_RADIAN;
	public static final URI UOM_UNITY;
	public static final URI UOM_METRE;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		DISTANCE = factory.createURI(NAMESPACE, "distance");
		BUFFER = factory.createURI(NAMESPACE, "buffer");
		CONVEX_HULL = factory.createURI(NAMESPACE, "convexHull");
		INTERSECTION = factory.createURI(NAMESPACE, "intersection");
		UNION = factory.createURI(NAMESPACE, "union");
		DIFFERENCE = factory.createURI(NAMESPACE, "difference");
		SYM_DIFFERENCE = factory.createURI(NAMESPACE, "symDifference");
		ENVELOPE = factory.createURI(NAMESPACE, "envelope");
		BOUNDARY = factory.createURI(NAMESPACE, "boundary");
		GET_SRID = factory.createURI(NAMESPACE, "getSRID");

		UOM_DEGREE = factory.createURI(UOM_NAMESPACE, "degree");
		UOM_RADIAN = factory.createURI(UOM_NAMESPACE, "radian");
		UOM_UNITY = factory.createURI(UOM_NAMESPACE, "unity");
		UOM_METRE = factory.createURI(UOM_NAMESPACE, "metre");
	}
}
