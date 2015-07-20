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
}
