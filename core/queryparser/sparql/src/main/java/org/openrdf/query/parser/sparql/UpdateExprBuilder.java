/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jws.soap.SOAPBinding.ParameterStyle;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.BNodeGenerator;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Modify;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.query.parser.sparql.ast.ASTDeleteClause;
import org.openrdf.query.parser.sparql.ast.ASTGraphPatternGroup;
import org.openrdf.query.parser.sparql.ast.ASTIRI;
import org.openrdf.query.parser.sparql.ast.ASTInsertClause;
import org.openrdf.query.parser.sparql.ast.ASTModify;
import org.openrdf.query.parser.sparql.ast.ASTUpdate;
import org.openrdf.query.parser.sparql.ast.VisitorException;

/**
 * Extension of TupleExprBuilder that builds Update Expressions.
 * 
 * @author Jeen Broekstra
 */
public class UpdateExprBuilder extends TupleExprBuilder {

	/**
	 * @param valueFactory
	 */
	public UpdateExprBuilder(ValueFactory valueFactory) {
		super(valueFactory);
	}

	@Override
	public UpdateExpr visit(ASTUpdate node, Object data)
		throws VisitorException
	{
		if (node instanceof ASTModify) {
			return this.visit((ASTModify)node, data);
		}

		return null;
	}

	public Modify visit(ASTModify node, Object data)
		throws VisitorException
	{
		
		ValueConstant with = null;
		ASTIRI withNode = node.getWithClause();
		if (withNode != null) {
			with = (ValueConstant)withNode.jjtAccept(this, data);
		}

		if (with != null) {
			graphPattern.setContextVar(valueExpr2Var(with));
			graphPattern.setStatementPatternScope(Scope.NAMED_CONTEXTS);
		}

		ASTGraphPatternGroup whereClause = node.getWhereClause();

		TupleExpr expr = null;
		if (whereClause != null) {
			expr = (TupleExpr)whereClause.jjtAccept(this, data);
		}

		TupleExpr delete = null;
		ASTDeleteClause deleteNode = node.getDeleteClause();
		if (deleteNode != null) {
			delete = (TupleExpr)deleteNode.jjtAccept(this, expr);
		}

		TupleExpr insert = null;
		ASTInsertClause insertNode = node.getInsertClause();
		if (insertNode != null) {
			insert = (TupleExpr)insertNode.jjtAccept(this, expr);
		}

		Modify modifyExpr = new Modify(delete, insert);

		return modifyExpr;
	}

	@Override
	public TupleExpr visit(ASTDeleteClause node, Object data)
		throws VisitorException
	{
		TupleExpr result = (TupleExpr)data;

		// Collect construct triples
		GraphPattern parentGP = graphPattern;

		graphPattern = new GraphPattern();
		
		// inherit scope & context
		graphPattern.setStatementPatternScope(parentGP.getStatementPatternScope());
		graphPattern.setContextVar(parentGP.getContextVar());
		
		if (node.jjtGetNumChildren() > 1) {
			ValueExpr contextNode = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, data);

			Var contextVar = valueExpr2Var(contextNode);
			graphPattern.setContextVar(contextVar);
			graphPattern.setStatementPatternScope(Scope.NAMED_CONTEXTS);

			node.jjtGetChild(1).jjtAccept(this, data);
		}
		else {
			node.jjtGetChild(0).jjtAccept(this, data);
		}
		TupleExpr deleteExpr = graphPattern.buildTupleExpr();

		graphPattern = parentGP;

		// Retrieve all StatementPatterns from the delete expression
		List<StatementPattern> statementPatterns = StatementPatternCollector.process(deleteExpr);

		Set<Var> projectionVars = getProjectionVars(statementPatterns);

		// Create extensions with valueconstants for all anonymous vars with values.
		Map<Var, ExtensionElem> extElemMap = new HashMap<Var, ExtensionElem>();

		for (Var var : projectionVars) {
			if (var.isAnonymous() && !extElemMap.containsKey(var)) {
				ValueExpr valueExpr;

				if (var.hasValue()) {
					valueExpr = new ValueConstant(var.getValue());
					extElemMap.put(var, new ExtensionElem(valueExpr, var.getName()));
				}
			}
		}

		if (!extElemMap.isEmpty()) {
			result = new Extension(result, extElemMap.values());
		}

		// Create a Projection for each StatementPattern in the clause
		List<ProjectionElemList> projList = new ArrayList<ProjectionElemList>();

		for (StatementPattern sp : statementPatterns) {
			ProjectionElemList projElemList = new ProjectionElemList();

			projElemList.addElement(new ProjectionElem(sp.getSubjectVar().getName(), "subject"));
			projElemList.addElement(new ProjectionElem(sp.getPredicateVar().getName(), "predicate"));
			projElemList.addElement(new ProjectionElem(sp.getObjectVar().getName(), "object"));
			if (sp.getContextVar() != null) {
				projElemList.addElement(new ProjectionElem(sp.getContextVar().getName(), "context"));
			}

			projList.add(projElemList);
		}

		if (projList.size() == 1) {
			result = new Projection(result, projList.get(0));
		}
		else if (projList.size() > 1) {
			result = new MultiProjection(result, projList);
		}
		else {
			// Empty constructor
			result = new EmptySet();
		}

		return new Reduced(result);
	}

	@Override
	public TupleExpr visit(ASTInsertClause node, Object data)
		throws VisitorException
	{
		TupleExpr result = (TupleExpr)data;

		// Collect construct triples
		GraphPattern parentGP = graphPattern;
		graphPattern = new GraphPattern();
		
		// inherit scope & context
		graphPattern.setStatementPatternScope(parentGP.getStatementPatternScope());
		graphPattern.setContextVar(parentGP.getContextVar());
		
		if (node.jjtGetNumChildren() > 1) {
			ValueExpr contextNode = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, data);

			Var contextVar = valueExpr2Var(contextNode);
			graphPattern.setContextVar(contextVar);
			graphPattern.setStatementPatternScope(Scope.NAMED_CONTEXTS);

			node.jjtGetChild(1).jjtAccept(this, data);
		}
		else {
			node.jjtGetChild(0).jjtAccept(this, data);
		}

		TupleExpr insertExpr = graphPattern.buildTupleExpr();

		graphPattern = parentGP;

		// Retrieve all StatementPatterns from the insert expression
		List<StatementPattern> statementPatterns = StatementPatternCollector.process(insertExpr);

		Set<Var> projectionVars = getProjectionVars(statementPatterns);

		// Create BNodeGenerators for all anonymous variables
		Map<Var, ExtensionElem> extElemMap = new HashMap<Var, ExtensionElem>();

		for (Var var : projectionVars) {
			if (var.isAnonymous() && !extElemMap.containsKey(var)) {
				ValueExpr valueExpr;

				if (var.hasValue()) {
					valueExpr = new ValueConstant(var.getValue());
				}
				else {
					valueExpr = new BNodeGenerator();
				}

				extElemMap.put(var, new ExtensionElem(valueExpr, var.getName()));
			}
		}

		if (!extElemMap.isEmpty()) {
			result = new Extension(result, extElemMap.values());
		}

		// Create a Projection for each StatementPattern in the clause
		List<ProjectionElemList> projList = new ArrayList<ProjectionElemList>();

		for (StatementPattern sp : statementPatterns) {
			ProjectionElemList projElemList = new ProjectionElemList();

			projElemList.addElement(new ProjectionElem(sp.getSubjectVar().getName(), "subject"));
			projElemList.addElement(new ProjectionElem(sp.getPredicateVar().getName(), "predicate"));
			projElemList.addElement(new ProjectionElem(sp.getObjectVar().getName(), "object"));

			if (sp.getContextVar() != null) {
				projElemList.addElement(new ProjectionElem(sp.getContextVar().getName(), "context"));
			}

			projList.add(projElemList);
		}

		if (projList.size() == 1) {
			result = new Projection(result, projList.get(0));
		}
		else if (projList.size() > 1) {
			result = new MultiProjection(result, projList);
		}
		else {
			// Empty constructor
			result = new EmptySet();
		}

		return new Reduced(result);
	}

	private Set<Var> getProjectionVars(Collection<StatementPattern> statementPatterns) {
		Set<Var> vars = new LinkedHashSet<Var>(statementPatterns.size() * 2);

		for (StatementPattern sp : statementPatterns) {
			vars.add(sp.getSubjectVar());
			vars.add(sp.getPredicateVar());
			vars.add(sp.getObjectVar());
			if (sp.getContextVar() != null) {
				vars.add(sp.getContextVar());
			}
		}

		return vars;
	}
}