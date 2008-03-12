/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.iteration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.sail.rdbms.RdbmsValueFactory;
import org.openrdf.sail.rdbms.algebra.ColumnVar;
import org.openrdf.sail.rdbms.exceptions.RdbmsQueryEvaluationException;
import org.openrdf.sail.rdbms.iteration.base.RdbmIterationBase;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.openrdf.sail.rdbms.schema.IdCode;
import org.openrdf.sail.rdbms.schema.ValueTable;

/**
 * Converts a {@link ResultSet} into a {@link BindingSet} in an iteration.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsBindingIteration extends
		RdbmIterationBase<BindingSet, QueryEvaluationException> {
	private BindingSet bindings;
	private Collection<ColumnVar> projections;
	private RdbmsValueFactory vf;

	public RdbmsBindingIteration(PreparedStatement stmt) throws SQLException {
		super(stmt);
	}

	public void setBindings(BindingSet bindings) {
		this.bindings = bindings;
	}

	public void setProjections(Collection<ColumnVar> proj) {
		this.projections = proj;
	}

	public void setValueFactory(RdbmsValueFactory vf) {
		this.vf = vf;
	}

	@Override
	protected BindingSet convert(ResultSet rs) throws SQLException {
		QueryBindingSet result = new QueryBindingSet(bindings);
		for (ColumnVar var : projections) {
			String name = var.getName();
			if (var != null && !result.hasBinding(name)) {
				Value value = var.getValue();
				if (value == null) {
					value = createValue(rs, var.getIndex() + 1);
				}
				if (value != null) {
					result.addBinding(var.getName(), value);
				}
			}
		}
		return result;
	}

	@Override
	protected QueryEvaluationException convertSQLException(SQLException e) {
		return new RdbmsQueryEvaluationException(e);
	}

	private RdbmsResource createResource(ResultSet rs, int index)
			throws SQLException {
		long id = rs.getLong(index);
		if (id == ValueTable.NIL_ID)
			return null;
		return vf.getRdbmsResource(id, rs.getString(index + 1));
	}

	private RdbmsValue createValue(ResultSet rs, int index) throws SQLException {
		long id = rs.getLong(index);
		if (IdCode.valueOf(id).isLiteral()) {
			String label = rs.getString(index + 1);
			String language = rs.getString(index + 2);
			String datatype = rs.getString(index + 3);
			return vf.getRdbmsLiteral(id, label, language, datatype);
		}
		return createResource(rs, index);
	}

}
