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
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;

/**
 * 
 * @author James Leigh
 */
public class HashExprFactory extends QueryModelVisitorBase<RdbmsException> {

	protected SqlExpr result;

	private RdbmsValueFactory vf;

	private QueryModelNode unsupported;

	public HashExprFactory(RdbmsValueFactory vf) {
		super();
		this.vf = vf;
	}

	public SqlExpr createHashExpr(ValueExpr expr)
		throws UnsupportedRdbmsOperatorException, RdbmsException
	{
		result = null;
		if (expr == null)
			return new SqlNull();
		expr.visit(this);
		if (unsupported != null)
			throw unsupported(unsupported);
		if (result == null)
			return new SqlNull();
		return result;
	}

	@Override
	public void meet(ValueConstant vc)
		throws RdbmsException
	{
		result = valueOf(vc.getValue());
	}

	@Override
	public void meet(Var var)
		throws RdbmsException
	{
		if (var.getValue() == null) {
			result = new RefIdColumn(var);
		}
		else {
			result = valueOf(var.getValue());
		}
	}

	@Override
	protected void meetNode(QueryModelNode arg) {
		result = null;
		if (unsupported != null) {
			unsupported = arg;
		}
	}

	public SqlExpr valueOf(Value value)
		throws RdbmsException
	{
		try {
			return new NumberValue(vf.getInternalId(value));
		}
		catch (RdbmsException e) {
			throw new RdbmsException(e);
		}
	}

}
