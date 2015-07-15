package org.openrdf.query.algebra.evaluation.function.geosparql;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.GEO;
import org.openrdf.model.vocabulary.GEOF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

/**
 * The GeoSPARQL {@link Function} geof:getSRID,
 * as defined in <a href="http://www.opengeospatial.org/standards/geosparql">OGC GeoSPARQL - A Geographic Query Language for RDF Data</a>.
 */
public class SRID implements Function {

	@Override
	public String getURI() {
		return GEOF.GET_SRID.stringValue();
	}

	@Override
	public Value evaluate(ValueFactory valueFactory, Value... args)
			throws ValueExprEvaluationException {
		if(args.length != 1) {
			throw new ValueExprEvaluationException(getURI()+" requires exactly 1 argument, got " + args.length);
		}

		Literal geom = FunctionArguments.getLiteral(this, args[0], GEO.WKT_LITERAL);
		String wkt = geom.getLabel();
		String srid;
		int sep = wkt.indexOf(' ');
		if(sep != -1 && wkt.charAt(0) == '<' && wkt.charAt(sep-1) == '>') {
			srid = wkt.substring(1, sep-1);
		}
		else {
			srid = GEO.DEFAULT_SRID;
		}

		return valueFactory.createLiteral(srid, XMLSchema.ANYURI);
	}
}
