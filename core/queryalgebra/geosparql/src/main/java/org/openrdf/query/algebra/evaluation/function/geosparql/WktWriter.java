package org.openrdf.query.algebra.evaluation.function.geosparql;

import java.io.IOException;

import com.spatial4j.core.shape.Shape;

public interface WktWriter {
	String toWkt(Shape shape) throws IOException;
}
