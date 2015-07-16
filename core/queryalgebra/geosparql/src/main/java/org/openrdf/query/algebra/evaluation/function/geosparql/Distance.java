package org.openrdf.query.algebra.evaluation.function.geosparql;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.GEOF;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Point;

/**
 * The GeoSPARQL {@link Function} geof:distance,
 * as defined in <a href="http://www.opengeospatial.org/standards/geosparql">OGC GeoSPARQL - A Geographic Query Language for RDF Data</a>.
 */
public class Distance implements Function {
	@Override
	public String getURI() {
		return GEOF.DISTANCE.stringValue();
	}

	@Override
	public Value evaluate(ValueFactory valueFactory, Value... args)
			throws ValueExprEvaluationException {
		if(args.length != 3) {
			throw new ValueExprEvaluationException(getURI()+" requires exactly 3 arguments, got " + args.length);
		}

		SpatialContext geoContext = SpatialSupport.getSpatialContext();
		Point p1 = FunctionArguments.getPoint(this, args[0], geoContext);
		Point p2 = FunctionArguments.getPoint(this, args[1], geoContext);
		URI units = FunctionArguments.getUnits(this, args[2]);

		double distDegs = geoContext.calcDistance(p1, p2);
		double distUom = FunctionArguments.convertFromDegrees(distDegs, units);

		return valueFactory.createLiteral(distUom);
	}
}