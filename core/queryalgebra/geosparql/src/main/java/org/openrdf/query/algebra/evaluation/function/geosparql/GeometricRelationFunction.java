package org.openrdf.query.algebra.evaluation.function.geosparql;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;

abstract class GeometricRelationFunction implements Function {

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
		boolean result = relation(geom1, geom2);

		return valueFactory.createLiteral(result);
	}

	protected abstract boolean relation(Shape g1, Shape g2);
}
