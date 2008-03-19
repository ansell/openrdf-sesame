/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra.factories;

import static org.openrdf.sail.rdbms.algebra.base.SqlExprSupport.unsupported;

import org.openrdf.model.Value;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.rdbms.RdbmsValueFactory;
import org.openrdf.sail.rdbms.algebra.NumberValue;
import org.openrdf.sail.rdbms.algebra.RefIdColumn;
import org.openrdf.sail.rdbms.algebra.SqlNull;
import org.openrdf.sail.rdbms.algebra.base.SqlExpr;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.exceptions.RdbmsRuntimeException;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;


/**
 *
 * @author James Leigh
 */
public class HashExprFactory extends
		QueryModelVisitorBase<UnsupportedRdbmsOperatorException> {
	protected SqlExpr result;
	private RdbmsValueFactory vf;

	public HashExprFactory(RdbmsValueFactory vf) {
		super();
		this.vf = vf;
	}

	public SqlExpr createHashExpr(ValueExpr expr)
			throws UnsupportedRdbmsOperatorException {
		result = null;
		if (expr == null)
			return new SqlNull();
		expr.visit(this);
		if (result == null)
			return new SqlNull();
		return result;
	}

	@Override
	public void meet(ValueConstant vc) {
		result = valueOf(vc.getValue());
	}

	@Override
	public void meet(Var var) {
		if (var.getValue() == null) {
			result = new RefIdColumn(var);
		} else {
			result = valueOf(var.getValue());
		}
	}

	@Override
	protected void meetNode(QueryModelNode arg)
			throws UnsupportedRdbmsOperatorException {
		throw unsupported(arg);
	}

	public SqlExpr valueOf(Value value) {
		try {
			return new NumberValue(vf.getInternalId(value));
		}
		catch (RdbmsException e) {
			throw new RdbmsRuntimeException(e);
		}
	}

}
