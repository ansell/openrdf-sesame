/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.cursor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.sail.rdbms.RdbmsValueFactory;
import org.openrdf.sail.rdbms.algebra.ColumnVar;
import org.openrdf.sail.rdbms.cursor.base.RdbmCursorBase;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.openrdf.sail.rdbms.schema.IdSequence;
import org.openrdf.sail.rdbms.schema.ValueTable;

/**
 * Cursor that converts a {@link ResultSet} into a {@link BindingSet}.
 * 
 * @author James Leigh
 */
public class RdbmsBindingCursor extends RdbmCursorBase<BindingSet> {

	private BindingSet bindings;

	private Collection<ColumnVar> projections;

	private RdbmsValueFactory vf;

	private IdSequence ids;

	public RdbmsBindingCursor(PreparedStatement stmt)
		throws SQLException
	{
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

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	@Override
	protected BindingSet convert(ResultSet rs)
		throws SQLException
	{
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

	private RdbmsResource createResource(ResultSet rs, int index)
		throws SQLException
	{
		Number id = ids.idOf(rs.getLong(index));
		if (id.longValue() == ValueTable.NIL_ID) {
			return null;
		}
		return vf.getRdbmsResource(id, rs.getString(index + 1));
	}

	private RdbmsValue createValue(ResultSet rs, int index)
		throws SQLException
	{
		Number id = ids.idOf(rs.getLong(index));
		if (ids.isLiteral(id)) {
			String label = rs.getString(index + 1);
			String language = rs.getString(index + 2);
			String datatype = rs.getString(index + 3);
			return vf.getRdbmsLiteral(id, label, language, datatype);
		}
		return createResource(rs, index);
	}

}
