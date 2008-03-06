/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.querymodel.BNodeGenerator;
import org.openrdf.querymodel.EmptySet;
import org.openrdf.querymodel.Extension;
import org.openrdf.querymodel.ExtensionElem;
import org.openrdf.querymodel.MultiProjection;
import org.openrdf.querymodel.Projection;
import org.openrdf.querymodel.ProjectionElem;
import org.openrdf.querymodel.StatementPattern;
import org.openrdf.querymodel.TupleExpr;
import org.openrdf.querymodel.ValueConstant;
import org.openrdf.querymodel.ValueExpr;
import org.openrdf.querymodel.Var;

class ConstructorBuilder {

	public TupleExpr buildConstructor(TupleExpr bodyExpr, TupleExpr constructExpr) {
		return _buildConstructor(bodyExpr, constructExpr, true);
	}

	public TupleExpr buildConstructor(TupleExpr bodyExpr) {
		return _buildConstructor(bodyExpr, bodyExpr, false);
	}

	private TupleExpr _buildConstructor(TupleExpr bodyExpr, TupleExpr constructExpr, boolean explicit) {
		// Retrieve all StatementPattern's from the construct expression
		StatementPatternCollector spCollector = new StatementPatternCollector();
		constructExpr.visit(spCollector);

		TupleExpr tupleExpr = bodyExpr;

		if (explicit) {
			// Create BNodeGenerator's for all anonymous variables
			Map<Var, ExtensionElem> extElemMap = new HashMap<Var, ExtensionElem>();

			for (StatementPattern sp : spCollector.getStatementPatterns()) {
				_createExtensionElem(sp.getSubjectVar(), extElemMap);
				_createExtensionElem(sp.getPredicateVar(), extElemMap);
				_createExtensionElem(sp.getObjectVar(), extElemMap);
			}

			if (!extElemMap.isEmpty()) {
				tupleExpr = new Extension(tupleExpr, extElemMap.values());
			}
		}

		// Create a Projection for each StatementPattern
		List<Projection> projList = new ArrayList<Projection>();

		for (StatementPattern sp : spCollector.getStatementPatterns()) {
			projList.add(_createProjection(tupleExpr, sp));
		}

		if (projList.size() == 1) {
			tupleExpr = projList.get(0);
		}
		else if (projList.size() > 1) {
			tupleExpr = new MultiProjection(tupleExpr, projList);
		}
		else {
			// Empty constructor
			tupleExpr = new EmptySet();
		}

		return tupleExpr;
	}

	/**
	 * Creates extension elements for variables that are anonymous or bound and
	 * adds these to the supplied map. Variables are only mapped to generators
	 * once, consecutive calls with the same variable are ignored.
	 * 
	 * @param var
	 * @param extElemMap
	 */
	private void _createExtensionElem(Var var, Map<Var, ExtensionElem> extElemMap) {
		if (var.isAnonymous() && !extElemMap.containsKey(var)) {
			ValueExpr valueExpr = null;
			
			if (var.hasValue()) {
				valueExpr = new ValueConstant(var.getValue());
			}
			else {
				valueExpr = new BNodeGenerator();
			}

			extElemMap.put(var, new ExtensionElem(valueExpr, var.getName()));
		}
	}

	private Projection _createProjection(TupleExpr bodyExpr, StatementPattern sp) {
		Projection proj = new Projection(bodyExpr);

		proj.add(new ProjectionElem(sp.getSubjectVar().getName(), "subject"));
		proj.add(new ProjectionElem(sp.getPredicateVar().getName(), "predicate"));
		proj.add(new ProjectionElem(sp.getObjectVar().getName(), "object"));

		return proj;
	}
}
