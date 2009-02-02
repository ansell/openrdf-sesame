/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.FilteringCursor;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;

/**
 * The same variable might have been used multiple times in this
 * StatementPattern, verify value equality in those cases.
 * 
 * @author James Leigh
 * 
 */
public class StatementPatternCursor extends FilteringCursor<Statement> {
	private Var subjVar;
	private Var predVar;
	private Var objVar;
	private Var conVar;

	public StatementPatternCursor(Cursor<? extends Statement> delegate,
			StatementPattern sp) {
		super(delegate);
		subjVar = sp.getSubjectVar();
		predVar = sp.getPredicateVar();
		objVar = sp.getObjectVar();
		conVar = sp.getContextVar();
	}

	@Override
	protected boolean accept(Statement st) {
		Resource subj = st.getSubject();
		URI pred = st.getPredicate();
		Value obj = st.getObject();
		Resource context = st.getContext();

		if (subjVar != null) {
			if (subjVar.equals(predVar) && !subj.equals(pred)) {
				return false;
			}
			if (subjVar.equals(objVar) && !subj.equals(obj)) {
				return false;
			}
			if (subjVar.equals(conVar) && !subj.equals(context)) {
				return false;
			}
		}

		if (predVar != null) {
			if (predVar.equals(objVar) && !pred.equals(obj)) {
				return false;
			}
			if (predVar.equals(conVar) && !pred.equals(context)) {
				return false;
			}
		}

		if (objVar != null) {
			if (objVar.equals(conVar) && !obj.equals(context)) {
				return false;
			}
		}

		return true;
	}
}

