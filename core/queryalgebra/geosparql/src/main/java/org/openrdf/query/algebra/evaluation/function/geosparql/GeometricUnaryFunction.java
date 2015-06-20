package org.openrdf.query.algebra.evaluation.function.geosparql;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.Shape;
import com.vividsolutions.jts.geom.Geometry;

public abstract class GeometricUnaryFunction implements Function {
	@Override
	public Value evaluate(ValueFactory valueFactory, Value... args)
			throws ValueExprEvaluationException {
		if(args.length != 1) {
			throw new ValueExprEvaluationException(getURI()+" requires exactly 1 argument, got " + args.length);
		}

		JtsSpatialContext geoContext = JtsSpatialContext.GEO;
		Shape geom = FunctionArguments.getShape(this, args[0], geoContext);
		Geometry result = operation(geoContext.getGeometryFrom(geom));

		return FunctionArguments.createWktLiteral(result, valueFactory);
	}

	protected abstract Geometry operation(Geometry g);
}
