/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.model.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * @see http://www.opengeospatial.org/standards/geosparql
 */
public class GEOF {

	public static final String NAMESPACE = "http://www.opengis.net/def/function/geosparql/";

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

	public static final URI RELATE;

	public static final URI SF_EQUALS;
	public static final URI SF_DISJOINT;
	public static final URI SF_INTERSECTS;
	public static final URI SF_TOUCHES;
	public static final URI SF_CROSSES;
	public static final URI SF_WITHIN;
	public static final URI SF_CONTAINS;
	public static final URI SF_OVERLAPS;

	public static final URI EH_EQUALS;
	public static final URI EH_DISJOINT;
	public static final URI EH_MEET;
	public static final URI EH_OVERLAP;
	public static final URI EH_COVERS;
	public static final URI EH_COVERED_BY;
	public static final URI EH_INSIDE;
	public static final URI EH_CONTAINS;

	public static final URI RCC8_EQ;
	public static final URI RCC8_DC;
	public static final URI RCC8_EC;
	public static final URI RCC8_PO;
	public static final URI RCC8_TPPI;
	public static final URI RCC8_TPP;
	public static final URI RCC8_NTPP;
	public static final URI RCC8_NTPPI;

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

		RELATE = factory.createURI(NAMESPACE, "relate");

		SF_EQUALS = factory.createURI(NAMESPACE, "sfEquals");
		SF_DISJOINT = factory.createURI(NAMESPACE, "sfDisjoint");
		SF_INTERSECTS = factory.createURI(NAMESPACE, "sfIntersects");
		SF_TOUCHES = factory.createURI(NAMESPACE, "sfTouches");
		SF_CROSSES = factory.createURI(NAMESPACE, "sfCrosses");
		SF_WITHIN = factory.createURI(NAMESPACE, "sfWithin");
		SF_CONTAINS = factory.createURI(NAMESPACE, "sfContains");
		SF_OVERLAPS = factory.createURI(NAMESPACE, "sfOverlaps");

		EH_EQUALS = factory.createURI(NAMESPACE, "ehEquals");
		EH_DISJOINT = factory.createURI(NAMESPACE, "ehDisjoint");
		EH_MEET = factory.createURI(NAMESPACE, "ehMeet");
		EH_OVERLAP = factory.createURI(NAMESPACE, "ehOverlap");
		EH_COVERS = factory.createURI(NAMESPACE, "ehCovers");
		EH_COVERED_BY = factory.createURI(NAMESPACE, "ehCoveredBy");
		EH_INSIDE = factory.createURI(NAMESPACE, "ehInside");
		EH_CONTAINS = factory.createURI(NAMESPACE, "ehContains");

		RCC8_EQ = factory.createURI(NAMESPACE, "rcc8eq");
		RCC8_DC = factory.createURI(NAMESPACE, "rcc8dc");
		RCC8_EC = factory.createURI(NAMESPACE, "rcc8ec");
		RCC8_PO = factory.createURI(NAMESPACE, "rcc8po");
		RCC8_TPPI = factory.createURI(NAMESPACE, "rcc8tppi");
		RCC8_TPP = factory.createURI(NAMESPACE, "rcc8tpp");
		RCC8_NTPP = factory.createURI(NAMESPACE, "rcc8ntpp");
		RCC8_NTPPI = factory.createURI(NAMESPACE, "rcc8ntppi");

		UOM_DEGREE = factory.createURI(UOM_NAMESPACE, "degree");
		UOM_RADIAN = factory.createURI(UOM_NAMESPACE, "radian");
		UOM_UNITY = factory.createURI(UOM_NAMESPACE, "unity");
		UOM_METRE = factory.createURI(UOM_NAMESPACE, "metre");
	}
}
