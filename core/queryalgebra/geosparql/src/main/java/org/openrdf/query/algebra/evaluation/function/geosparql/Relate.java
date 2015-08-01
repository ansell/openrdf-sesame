package org.openrdf.query.algebra.evaluation.function.geosparql;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.GEOF;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;

/**
 * The GeoSPARQL {@link Function} geof:relate, as defined in <a
 * href="http://www.opengeospatial.org/standards/geosparql">OGC GeoSPARQL - A
 * Geographic Query Language for RDF Data</a>.
 */
public class Relate implements Function {

	@Override
	public String getURI() {
		return GEOF.RELATE.stringValue();
	}

	@Override
	public Value evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 3) {
			throw new ValueExprEvaluationException(getURI() + " requires exactly 3 arguments, got "
					+ args.length);
		}

		SpatialContext geoContext = SpatialSupport.getSpatialContext();
		Shape geom1 = FunctionArguments.getShape(this, args[0], geoContext);
		Shape geom2 = FunctionArguments.getShape(this, args[1], geoContext);
		String pattern = FunctionArguments.getString(this, args[2]);
		boolean result = SpatialSupport.getSpatialAlgebra().relate(geom1, geom2, pattern);

		return valueFactory.createLiteral(result);
	}
}
