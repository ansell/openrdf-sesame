package org.openrdf.query.algebra.evaluation.function.geosparql;

import org.openrdf.model.vocabulary.GEOF;
import org.openrdf.query.algebra.evaluation.function.Function;

import com.spatial4j.core.shape.Shape;

/**
 * The GeoSPARQL {@link Function} geof:sfIntersects,
 * as defined in <a href="http://www.opengeospatial.org/standards/geosparql">OGC GeoSPARQL - A Geographic Query Language for RDF Data</a>.
 */
public class SfIntersects extends GeometricRelationFunction {
	@Override
	public String getURI() {
		return GEOF.SF_INTERSECTS.stringValue();
	}

	@Override
	protected boolean relation(Shape s1, Shape s2) {
		return SpatialSupport.getSpatialAlgebra().sfIntersects(s1, s2);
	}
}
