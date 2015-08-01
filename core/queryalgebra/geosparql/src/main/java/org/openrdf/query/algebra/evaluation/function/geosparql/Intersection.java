package org.openrdf.query.algebra.evaluation.function.geosparql;

import org.openrdf.model.vocabulary.GEOF;
import org.openrdf.query.algebra.evaluation.function.Function;

import com.spatial4j.core.shape.Shape;

/**
 * The GeoSPARQL {@link Function} geof:intersection, as defined in <a
 * href="http://www.opengeospatial.org/standards/geosparql">OGC GeoSPARQL - A
 * Geographic Query Language for RDF Data</a>.
 */
public class Intersection extends GeometricBinaryFunction {

	@Override
	public String getURI() {
		return GEOF.INTERSECTION.stringValue();
	}

	@Override
	protected Shape operation(Shape s1, Shape s2) {
		return SpatialSupport.getSpatialAlgebra().intersection(s1, s2);
	}
}
