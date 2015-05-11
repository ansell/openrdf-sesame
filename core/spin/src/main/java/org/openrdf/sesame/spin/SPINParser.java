package org.openrdf.sesame.spin;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedDescribeQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;

public class SPINParser {
	public ParsedQuery parseQuery(Resource queryResource, TripleSource store) throws OpenRDFException {
		return parse(queryResource, null, store);
	}

	public ParsedGraphQuery parseConstructQuery(Resource queryResource, TripleSource store) throws OpenRDFException {
		return (ParsedGraphQuery) parse(queryResource, SP.CONSTRUCT_CLASS, store);
	}

	public ParsedTupleQuery parseSelectQuery(Resource queryResource, TripleSource store) throws OpenRDFException {
		return (ParsedTupleQuery) parse(queryResource, SP.SELECT_CLASS, store);
	}

	public ParsedBooleanQuery parseAskQuery(Resource queryResource, TripleSource store) throws OpenRDFException {
		return (ParsedBooleanQuery) parse(queryResource, SP.ASK_CLASS, store);
	}

	public ParsedDescribeQuery parseDescribeQuery(Resource queryResource, TripleSource store) throws OpenRDFException {
		return (ParsedDescribeQuery) parse(queryResource, SP.DESCRIBE_CLASS, store);
	}

	protected ParsedQuery parse(Resource queryResource, URI queryType, TripleSource store) throws OpenRDFException {
		// first try sp:text
		Statement textStmt = single(queryResource, SP.TEXT_PROPERTY, null, store);
		if(textStmt != null) {
			return QueryParserUtil.parseQuery(QueryLanguage.SPARQL, textStmt.getObject().stringValue(), null);
		}

		if(queryType == null) {
			Statement queryTypeStmt = requireSingle(queryResource, RDF.TYPE, null, store);
			queryType = (URI) queryTypeStmt.getObject();
		}
		throw new UnsupportedOperationException("TO DO");
	}

	private static Statement single(Resource subj, URI pred, Value obj, TripleSource store) throws OpenRDFException {
		Statement stmt;
		CloseableIteration<? extends Statement,QueryEvaluationException> stmts = store.getStatements(subj, pred, obj);
		try {
			if(stmts.hasNext()) {
				stmt = stmts.next();
				if(stmts.hasNext()) {
					throw new MalformedQueryException("Multiple statements for pattern "+subj+" "+pred+" "+obj);
				}
			}
			else {
				stmt = null;
			}
		}
		finally {
			stmts.close();
		}
		return stmt;
	}

	private static Statement requireSingle(Resource subj, URI pred, Value obj, TripleSource store) throws OpenRDFException {
		Statement stmt = single(subj, pred, obj, store);
		if(stmt == null) {
			throw new MalformedQueryException("Missing statement for pattern "+subj+" "+pred+" "+obj);
		}
		return stmt;
	}
}
