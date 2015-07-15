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
	boolean rcc8dntppi(Shape s1, Shape s2);
}
