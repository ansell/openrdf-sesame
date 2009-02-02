/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import org.openrdf.cursor.ConvertingCursor;
import org.openrdf.cursor.Cursor;
import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

/**
 * converts the statements to var bindings
 * 
 * @author James Leigh
 * 
 */
public class StatementBindingSetCursor extends
		ConvertingCursor<Statement, BindingSet> {
	private BindingSet bindings;
	private Var subjVar;
	private Var predVar;
	private Var objVar;
	private Var conVar;

	public StatementBindingSetCursor(Cursor<? extends Statement> delegate,
			StatementPattern sp, BindingSet bindings) {
		super(delegate);
		this.bindings = bindings;
		subjVar = sp.getSubjectVar();
		predVar = sp.getPredicateVar();
		objVar = sp.getObjectVar();
		conVar = sp.getContextVar();
	}

	@Override
	protected BindingSet convert(Statement st) {
		QueryBindingSet result = new QueryBindingSet(bindings);

		if (subjVar != null && !result.hasBinding(subjVar.getName())) {
			result.addBinding(subjVar.getName(), st.getSubject());
		}
		if (predVar != null && !result.hasBinding(predVar.getName())) {
			result.addBinding(predVar.getName(), st.getPredicate());
		}
		if (objVar != null && !result.hasBinding(objVar.getName())) {
			result.addBinding(objVar.getName(), st.getObject());
		}
		if (conVar != null && !result.hasBinding(conVar.getName())
				&& st.getContext() != null) {
			result.addBinding(conVar.getName(), st.getContext());
		}

		return result;
	}
}
