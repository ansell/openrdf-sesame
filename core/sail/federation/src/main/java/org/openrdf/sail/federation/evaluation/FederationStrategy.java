/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import static org.openrdf.sail.federation.query.QueryModelSerializer.LANGUAGE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Cursor;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.parser.TupleQueryModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryMetaData;
import org.openrdf.sail.SailMetaData;
import org.openrdf.sail.federation.algebra.OwnedTupleExpr;
import org.openrdf.sail.federation.query.QueryModelSerializer;
import org.openrdf.store.StoreException;


/**
 *
 * @author James Leigh
 */
public class FederationStrategy extends EvaluationStrategyImpl {

	private Logger logger = LoggerFactory.getLogger(FederationStrategy.class);

	private SailMetaData metadata;

	public FederationStrategy(SailMetaData md, TripleSource tripleSource, QueryModel query) {
		super(tripleSource, query);
		this.metadata = md;
	}

	@Override
	public Cursor<BindingSet> evaluate(UnaryTupleOperator expr, BindingSet bindings)
		throws StoreException
	{
		if (expr instanceof OwnedTupleExpr) {
			return evaluate((OwnedTupleExpr) expr, bindings);
		} else {
			return super.evaluate(expr, bindings);
		}
	}

	public Cursor<BindingSet> evaluate(OwnedTupleExpr expr, BindingSet bindings)
		throws StoreException
	{
		RepositoryConnection owner = expr.getOwner();
		QueryModel query = createQueryModel(expr);
		if (isRemoteQueryModelSupported(owner)) {
			try {
				String qry = new QueryModelSerializer().writeQueryModel(query, "");
				TupleQuery pqry = owner.prepareTupleQuery(LANGUAGE, qry);
				for (String name : bindings.getBindingNames()) {
					pqry.setBinding(name, bindings.getValue(name));
				}
				pqry.setDataset(dataset);
				TupleQueryResult result = pqry.evaluate();
				return new TupleQueryResultCursor(result, bindings);
			} catch (MalformedQueryException e) {
				// remote QueryModel does not work
				logger.warn(e.toString(), e);
			}
		}
		TripleSource source = new RepositoryTripleSource(owner);
		EvaluationStrategyImpl eval = new EvaluationStrategyImpl(source, query);
		return eval.evaluate(query, bindings);
	}

	private QueryModel createQueryModel(OwnedTupleExpr expr) {
		TupleQueryModel query = new TupleQueryModel(expr.getArg());
		if (dataset != null) {
			query.setDefaultGraphs(dataset.getDefaultGraphs());
			query.setNamedGraphs(dataset.getNamedGraphs());
		}
		return query;
	}

	private boolean isRemoteQueryModelSupported(RepositoryConnection owner)
		throws StoreException
	{
		RepositoryMetaData md = owner.getRepository().getRepositoryMetaData();
		if (metadata.getSesameMajorVersion() != md.getSesameMajorVersion())
			return false;
		if (metadata.getSesameMinorVersion() != md.getSesameMinorVersion())
			return false;
		for (QueryLanguage ql : md.getQueryLanguages()) {
			if (ql.equals(QueryModelSerializer.LANGUAGE))
				return true;
		}
		return false;
	}

}
