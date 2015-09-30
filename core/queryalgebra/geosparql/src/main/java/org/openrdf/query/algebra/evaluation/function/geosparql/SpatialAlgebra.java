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
package org.openrdf.query.algebra.evaluation.function.geosparql;

import com.spatial4j.core.shape.Shape;

public interface SpatialAlgebra {

	Shape convexHull(Shape s);

	Shape boundary(Shape s);

	Shape envelope(Shape s);

	Shape union(Shape s1, Shape s2);

	Shape intersection(Shape s1, Shape s2);

	Shape symDifference(Shape s1, Shape s2);

	Shape difference(Shape s1, Shape s2);

	boolean relate(Shape s1, Shape s2, String intersectionPattern);

	boolean equals(Shape s1, Shape s2);

	boolean sfDisjoint(Shape s1, Shape s2);

	boolean sfIntersects(Shape s1, Shape s2);

	boolean sfTouches(Shape s1, Shape s2);

	boolean sfCrosses(Shape s1, Shape s2);

	boolean sfWithin(Shape s1, Shape s2);

	boolean sfContains(Shape s1, Shape s2);

	boolean sfOverlaps(Shape s1, Shape s2);

	boolean ehDisjoint(Shape s1, Shape s2);

	boolean ehMeet(Shape s1, Shape s2);

	boolean ehOverlap(Shape s1, Shape s2);

	boolean ehCovers(Shape s1, Shape s2);

	boolean ehCoveredBy(Shape s1, Shape s2);

	boolean ehInside(Shape s1, Shape s2);

	boolean ehContains(Shape s1, Shape s2);

	boolean rcc8dc(Shape s1, Shape s2);

	boolean rcc8ec(Shape s1, Shape s2);

	boolean rcc8po(Shape s1, Shape s2);

	boolean rcc8tppi(Shape s1, Shape s2);

	boolean rcc8tpp(Shape s1, Shape s2);

	boolean rcc8ntpp(Shape s1, Shape s2);

	boolean rcc8ntppi(Shape s1, Shape s2);
}
