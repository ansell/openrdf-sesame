/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.evaluation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.cursor.Cursor;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.sail.rdbms.RdbmsTripleRepository;
import org.openrdf.sail.rdbms.RdbmsValueFactory;
import org.openrdf.sail.rdbms.algebra.ColumnVar;
import org.openrdf.sail.rdbms.algebra.SelectProjection;
import org.openrdf.sail.rdbms.algebra.SelectQuery;
import org.openrdf.sail.rdbms.algebra.SelectQuery.OrderElem;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;
import org.openrdf.sail.rdbms.iteration.RdbmsBindingCursor;
import org.openrdf.sail.rdbms.schema.IdSequence;
import org.openrdf.store.StoreException;

/**
 * Extends the default strategy by accepting {@link SelectQuery} and evaluating
 * them on a database.
 * 
 * @author James Leigh
 */
public class RdbmsEvaluation extends EvaluationStrategyImpl {

	private Logger logger = LoggerFactory.getLogger(RdbmsEvaluation.class);

	private QueryBuilderFactory factory;

	private RdbmsValueFactory vf;

	private RdbmsTripleRepository triples;

	private IdSequence ids;

	public RdbmsEvaluation(QueryBuilderFactory factory, RdbmsTripleRepository triples, QueryModel query,
			IdSequence ids)
	{
		super(new RdbmsTripleSource(triples), query);
		this.factory = factory;
		this.triples = triples;
		this.vf = triples.getValueFactory();
		this.ids = ids;
	}

	@Override
	public Cursor<BindingSet> evaluate(TupleExpr expr, BindingSet bindings)
		throws StoreException
	{
		if (expr instanceof SelectQuery) {
			return evaluate((SelectQuery)expr, bindings);
		}
		return super.evaluate(expr, bindings);
	}

	private Cursor<BindingSet> evaluate(SelectQuery qb, BindingSet b)
		throws UnsupportedRdbmsOperatorException, RdbmsException
	{
		List<Object> parameters = new ArrayList<Object>();
		QueryBindingSet bindings = new QueryBindingSet(b);
		String query = toQueryString(qb, bindings, parameters);
		try {
			Connection conn = triples.getConnection();
			PreparedStatement stmt = conn.prepareStatement(query);
			int p = 0;
			for (Object o : parameters) {
				stmt.setObject(++p, o);
			}
			Collection<ColumnVar> proj = qb.getProjections();
			RdbmsBindingCursor result = new RdbmsBindingCursor(stmt);
			result.setProjections(proj);
			result.setBindings(bindings);
			result.setValueFactory(vf);
			result.setIdSequence(ids);
			return result;
		}
		catch (SQLException e) {
			throw new RdbmsException(e.toString() + "\n" + query, e);
		}
	}

	private String toQueryString(SelectQuery qb, QueryBindingSet bindings, List<Object> parameters)
		throws RdbmsException, UnsupportedRdbmsOperatorException
	{
		QueryBuilder query = factory.createQueryBuilder();
		if (qb.isDistinct()) {
			query.distinct();
		}
		query.from(qb.getFrom());
		for (ColumnVar var : qb.getVars()) {
			for (String name : qb.getBindingNames(var)) {
				if (var.getValue() == null && bindings.hasBinding(name)) {
					query.filter(var, bindings.getValue(name));
				}
				else if (var.getValue() != null && !bindings.hasBinding(name)
						&& qb.getBindingNames().contains(name))
				{
					bindings.addBinding(name, var.getValue());
				}
			}
		}
		int index = 0;
		for (SelectProjection proj : qb.getSqlSelectVar()) {
			ColumnVar var = proj.getVar();
			if (!var.isHiddenOrConstant()) {
				for (String name : qb.getBindingNames(var)) {
					if (!bindings.hasBinding(name)) {
						var.setIndex(index);
						query.select(proj.getId());
						query.select(proj.getStringValue());
						index += 2;
						if (var.getTypes().isLiterals()) {
							query.select(proj.getLanguage());
							query.select(proj.getDatatype());
							index += 2;
						}
					}
				}
			}
		}
		for (OrderElem by : qb.getOrderElems()) {
			query.orderBy(by.sqlExpr, by.isAscending);
			if (qb.isDistinct()) {
				query.select(by.sqlExpr);
			}
		}
		if (qb.getLimit() != null) {
			query.limit(qb.getLimit());
		}
		if (qb.getOffset() != null) {
			query.offset(qb.getOffset());
		}
		parameters.addAll(query.getParameters());
		if (logger.isDebugEnabled()) {
			logger.debug(query.toString());
			logger.debug(parameters.toString());
		}
		return query.toString();
	}
}
