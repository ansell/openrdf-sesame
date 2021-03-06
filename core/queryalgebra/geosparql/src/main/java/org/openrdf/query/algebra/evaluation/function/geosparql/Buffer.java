package org.openrdf.query.algebra.evaluation.function.geosparql;

import java.io.IOException;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.GEO;
import org.openrdf.model.vocabulary.GEOF;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;

/**
 * The GeoSPARQL {@link Function} geof:buffer, as defined in <a
 * href="http://www.opengeospatial.org/standards/geosparql">OGC GeoSPARQL - A
 * Geographic Query Language for RDF Data</a>.
 */
public class Buffer implements Function {

	@Override
	public String getURI() {
		return GEOF.BUFFER.stringValue();
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
		Shape geom = FunctionArguments.getShape(this, args[0], geoContext);
		double radiusUom = FunctionArguments.getDouble(this, args[1]);
		URI units = FunctionArguments.getUnits(this, args[2]);
		double radiusDegs = FunctionArguments.convertToDegrees(radiusUom, units);

		Shape buffered = geom.getBuffered(radiusDegs, geoContext);

		String wkt;
		try {
			wkt = SpatialSupport.getWktWriter().toWkt(buffered);
		}
		catch (IOException ioe) {
			throw new ValueExprEvaluationException(ioe);
		}
		return valueFactory.createLiteral(wkt, GEO.WKT_LITERAL);
	}
}
