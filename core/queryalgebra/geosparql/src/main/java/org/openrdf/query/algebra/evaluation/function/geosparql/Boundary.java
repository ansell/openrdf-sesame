package org.openrdf.query.algebra.evaluation.function.geosparql;

import org.openrdf.model.vocabulary.GEOF;
import org.openrdf.query.algebra.evaluation.function.Function;

import com.spatial4j.core.shape.Shape;

/**
 * The GeoSPARQL {@link Function} geof:boundary,
 * as defined in <a href="http://www.opengeospatial.org/standards/geosparql">OGC GeoSPARQL - A Geographic Query Language for RDF Data</a>.
 */
public class Boundary extends GeometricUnaryFunction {
	@Override
	public String getURI() {
		return GEOF.BOUNDARY.stringValue();
	}

	@Override
	protected Shape operation(Shape s) {
		return SpatialSupport.getSpatialAlgebra().boundary(s);
	}
}
