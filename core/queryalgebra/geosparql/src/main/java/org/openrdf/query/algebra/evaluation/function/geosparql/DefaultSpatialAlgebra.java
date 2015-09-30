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

import java.util.Arrays;
import java.util.Collections;

import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.ShapeCollection;
import com.spatial4j.core.shape.SpatialRelation;
import com.spatial4j.core.shape.impl.BufferedLineString;

final class DefaultSpatialAlgebra implements SpatialAlgebra {

	private <T> T notSupported() {
		throw new UnsupportedOperationException(
				"Not supported due to licensing issues. Feel free to provide your own implementation by using something like JTS.");
	}

	private Shape createEmptyPoint() {
		return SpatialSupport.getSpatialContext().makePoint(Double.NaN, Double.NaN);
	}

	private Shape createEmptyGeometry() {
		return new ShapeCollection<Shape>(Collections.<Shape> emptyList(), SpatialSupport.getSpatialContext());
	}

	@Override
	public Shape convexHull(Shape s) {
		if (s instanceof Point) {
			return s;
		}
		else if (s instanceof ShapeCollection<?>) {
			return new BufferedLineString((ShapeCollection<Point>)s, 0.0, SpatialSupport.getSpatialContext());
		}
		return notSupported();
	}

	@Override
	public Shape boundary(Shape s) {
		if (s instanceof Point) {
			// points have no boundary so return empty shape
			return createEmptyGeometry();
		}
		else if (s instanceof ShapeCollection<?>) {
			ShapeCollection<?> col = (ShapeCollection<?>)s;
			if (col.isEmpty()) {
				return createEmptyGeometry();
			}
			for (Shape p : col) {
				if (!(p instanceof Point)) {
					return notSupported();
				}
			}
			return createEmptyGeometry();
		}
		return notSupported();
	}

	@Override
	public Shape envelope(Shape s) {
		if (s instanceof Point) {
			return s;
		}
		return notSupported();
	}

	@Override
	public Shape union(Shape s1, Shape s2) {
		if (s1 instanceof Point && s2 instanceof Point) {
			Point p1 = (Point)s1;
			Point p2 = (Point)s2;
			int diff = compare(p2, p1);
			if (diff == 0) {
				return s1;
			}
			else if (diff < 0) {
				p1 = p2;
				p2 = (Point)s1;
			}
			return new ShapeCollection<Point>(Arrays.asList(p1, p2), SpatialSupport.getSpatialContext());
		}
		return notSupported();
	}

	private int compare(Point p1, Point p2) {
		int diff = Double.compare(p1.getX(), p2.getX());
		if (diff == 0) {
			diff = Double.compare(p1.getY(), p2.getY());
		}
		return diff;
	}

	@Override
	public Shape intersection(Shape s1, Shape s2) {
		if (s1 instanceof Point && s2 instanceof Point) {
			Point p1 = (Point)s1;
			Point p2 = (Point)s2;
			int diff = compare(p2, p1);
			if (diff == 0) {
				return s1;
			}
			else {
				return createEmptyPoint();
			}
		}
		return notSupported();
	}

	@Override
	public Shape symDifference(Shape s1, Shape s2) {
		if (s1 instanceof Point && s2 instanceof Point) {
			Point p1 = (Point)s1;
			Point p2 = (Point)s2;
			int diff = compare(p2, p1);
			if (diff == 0) {
				return createEmptyPoint();
			}
			else if (diff < 0) {
				p1 = p2;
				p2 = (Point)s1;
			}
			return new ShapeCollection<Point>(Arrays.asList(p1, p2), SpatialSupport.getSpatialContext());
		}
		return notSupported();
	}

	@Override
	public Shape difference(Shape s1, Shape s2) {
		if (s1 instanceof Point && s2 instanceof Point) {
			Point p1 = (Point)s1;
			Point p2 = (Point)s2;
			int diff = compare(p2, p1);
			if (diff == 0) {
				return createEmptyPoint();
			}
			return s1;
		}
		return notSupported();
	}

	@Override
	public boolean relate(Shape s1, Shape s2, String intersectionPattern) {
		return notSupported();
	}

	@Override
	public boolean equals(Shape s1, Shape s2) {
		return s1.equals(s2);
	}

	@Override
	public boolean sfDisjoint(Shape s1, Shape s2) {
		return SpatialRelation.DISJOINT == s1.relate(s2);
	}

	@Override
	public boolean sfIntersects(Shape s1, Shape s2) {
		return SpatialRelation.INTERSECTS == s1.relate(s2);
	}

	@Override
	public boolean sfTouches(Shape s1, Shape s2) {
		return notSupported();
	}

	@Override
	public boolean sfCrosses(Shape s1, Shape s2) {
		return notSupported();
	}

	@Override
	public boolean sfWithin(Shape s1, Shape s2) {
		return notSupported();
	}

	@Override
	public boolean sfContains(Shape s1, Shape s2) {
		return notSupported();
	}

	@Override
	public boolean sfOverlaps(Shape s1, Shape s2) {
		return notSupported();
	}

	@Override
	public boolean ehDisjoint(Shape s1, Shape s2) {
		return SpatialRelation.DISJOINT == s1.relate(s2);
	}

	@Override
	public boolean ehMeet(Shape s1, Shape s2) {
		return notSupported();
	}

	@Override
	public boolean ehOverlap(Shape s1, Shape s2) {
		return notSupported();
	}

	@Override
	public boolean ehCovers(Shape s1, Shape s2) {
		return SpatialRelation.CONTAINS == s1.relate(s2);
	}

	@Override
	public boolean ehCoveredBy(Shape s1, Shape s2) {
		return SpatialRelation.WITHIN == s1.relate(s2);
	}

	@Override
	public boolean ehInside(Shape s1, Shape s2) {
		return notSupported();
	}

	@Override
	public boolean ehContains(Shape s1, Shape s2) {
		return notSupported();
	}

	@Override
	public boolean rcc8dc(Shape s1, Shape s2) {
		return notSupported();
	}

	@Override
	public boolean rcc8ec(Shape s1, Shape s2) {
		return notSupported();
	}

	@Override
	public boolean rcc8po(Shape s1, Shape s2) {
		return notSupported();
	}

	@Override
	public boolean rcc8tppi(Shape s1, Shape s2) {
		return notSupported();
	}

	@Override
	public boolean rcc8tpp(Shape s1, Shape s2) {
		return notSupported();
	}

	@Override
	public boolean rcc8ntpp(Shape s1, Shape s2) {
		return notSupported();
	}

	@Override
	public boolean rcc8ntppi(Shape s1, Shape s2) {
		return notSupported();
	}
}