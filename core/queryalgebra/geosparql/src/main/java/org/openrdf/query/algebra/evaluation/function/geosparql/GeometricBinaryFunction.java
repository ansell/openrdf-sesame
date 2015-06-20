package org.openrdf.query.algebra.evaluation.function.geosparql;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.Shape;
import com.vividsolutions.jts.geom.Geometry;

public abstract class GeometricBinaryFunction implements Function {
	@Override
	public Value evaluate(ValueFactory valueFactory, Value... args)
			throws ValueExprEvaluationException {
		if(args.length != 2) {
			throw new ValueExprEvaluationException(getURI()+" requires exactly 2 arguments, got " + args.length);
		}

		JtsSpatialContext geoContext = JtsSpatialContext.GEO;
		Shape geom1 = FunctionArguments.getShape(this, args[0], geoContext);
		Shape geom2 = FunctionArguments.getShape(this, args[1], geoContext);
		Geometry result = operation(geoContext.getGeometryFrom(geom1), geoContext.getGeometryFrom(geom2));

		return FunctionArguments.createWktLiteral(result, valueFactory);
	}

	protected abstract Geometry operation(Geometry g1, Geometry g2);
}
