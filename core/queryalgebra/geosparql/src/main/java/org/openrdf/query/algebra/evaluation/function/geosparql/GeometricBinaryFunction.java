package org.openrdf.query.algebra.evaluation.function.geosparql;

import java.io.IOException;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.GEO;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;

abstract class GeometricBinaryFunction implements Function {

	@Override
	public Value evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 2) {
			throw new ValueExprEvaluationException(getURI() + " requires exactly 2 arguments, got "
					+ args.length);
		}

		SpatialContext geoContext = SpatialSupport.getSpatialContext();
		Shape geom1 = FunctionArguments.getShape(this, args[0], geoContext);
		Shape geom2 = FunctionArguments.getShape(this, args[1], geoContext);
		Shape result = operation(geom1, geom2);

		String wkt;
		try {
			wkt = SpatialSupport.getWktWriter().toWkt(result);
		}
		catch (IOException ioe) {
			throw new ValueExprEvaluationException(ioe);
		}
		return valueFactory.createLiteral(wkt, GEO.WKT_LITERAL);
	}

	protected abstract Shape operation(Shape g1, Shape g2);
}
