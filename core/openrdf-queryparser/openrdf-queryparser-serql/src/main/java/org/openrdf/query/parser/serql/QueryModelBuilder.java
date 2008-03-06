/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.CompareAll;
import org.openrdf.query.algebra.CompareAny;
import org.openrdf.query.algebra.DatatypeFunc;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.In;
import org.openrdf.query.algebra.Intersection;
import org.openrdf.query.algebra.IsBNode;
import org.openrdf.query.algebra.IsLiteral;
import org.openrdf.query.algebra.IsResource;
import org.openrdf.query.algebra.IsURI;
import org.openrdf.query.algebra.LabelFunc;
import org.openrdf.query.algebra.LangFunc;
import org.openrdf.query.algebra.Like;
import org.openrdf.query.algebra.LocalNameFunc;
import org.openrdf.query.algebra.NamespaceFunc;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Null;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.parser.serql.ast.ASTAnd;
import org.openrdf.query.parser.serql.ast.ASTBNode;
import org.openrdf.query.parser.serql.ast.ASTBasicPathExpr;
import org.openrdf.query.parser.serql.ast.ASTBasicPathExprTail;
import org.openrdf.query.parser.serql.ast.ASTBooleanConstant;
import org.openrdf.query.parser.serql.ast.ASTBooleanExpr;
import org.openrdf.query.parser.serql.ast.ASTCompare;
import org.openrdf.query.parser.serql.ast.ASTCompareAll;
import org.openrdf.query.parser.serql.ast.ASTCompareAny;
import org.openrdf.query.parser.serql.ast.ASTConstruct;
import org.openrdf.query.parser.serql.ast.ASTConstructQuery;
import org.openrdf.query.parser.serql.ast.ASTDatatype;
import org.openrdf.query.parser.serql.ast.ASTEdge;
import org.openrdf.query.parser.serql.ast.ASTExists;
import org.openrdf.query.parser.serql.ast.ASTFrom;
import org.openrdf.query.parser.serql.ast.ASTGraphIntersect;
import org.openrdf.query.parser.serql.ast.ASTGraphMinus;
import org.openrdf.query.parser.serql.ast.ASTGraphQuery;
import org.openrdf.query.parser.serql.ast.ASTGraphQuerySet;
import org.openrdf.query.parser.serql.ast.ASTGraphUnion;
import org.openrdf.query.parser.serql.ast.ASTIn;
import org.openrdf.query.parser.serql.ast.ASTIsBNode;
import org.openrdf.query.parser.serql.ast.ASTIsLiteral;
import org.openrdf.query.parser.serql.ast.ASTIsResource;
import org.openrdf.query.parser.serql.ast.ASTIsURI;
import org.openrdf.query.parser.serql.ast.ASTLabel;
import org.openrdf.query.parser.serql.ast.ASTLang;
import org.openrdf.query.parser.serql.ast.ASTLike;
import org.openrdf.query.parser.serql.ast.ASTLimit;
import org.openrdf.query.parser.serql.ast.ASTLiteral;
import org.openrdf.query.parser.serql.ast.ASTLocalName;
import org.openrdf.query.parser.serql.ast.ASTNamespace;
import org.openrdf.query.parser.serql.ast.ASTNode;
import org.openrdf.query.parser.serql.ast.ASTNodeElem;
import org.openrdf.query.parser.serql.ast.ASTNot;
import org.openrdf.query.parser.serql.ast.ASTNull;
import org.openrdf.query.parser.serql.ast.ASTOffset;
import org.openrdf.query.parser.serql.ast.ASTOptPathExpr;
import org.openrdf.query.parser.serql.ast.ASTOptPathExprTail;
import org.openrdf.query.parser.serql.ast.ASTOr;
import org.openrdf.query.parser.serql.ast.ASTPathExpr;
import org.openrdf.query.parser.serql.ast.ASTPathExprTail;
import org.openrdf.query.parser.serql.ast.ASTProjectionElem;
import org.openrdf.query.parser.serql.ast.ASTQuery;
import org.openrdf.query.parser.serql.ast.ASTQueryBody;
import org.openrdf.query.parser.serql.ast.ASTQueryContainer;
import org.openrdf.query.parser.serql.ast.ASTReifiedStat;
import org.openrdf.query.parser.serql.ast.ASTSelect;
import org.openrdf.query.parser.serql.ast.ASTSelectQuery;
import org.openrdf.query.parser.serql.ast.ASTString;
import org.openrdf.query.parser.serql.ast.ASTTupleIntersect;
import org.openrdf.query.parser.serql.ast.ASTTupleMinus;
import org.openrdf.query.parser.serql.ast.ASTTupleQuery;
import org.openrdf.query.parser.serql.ast.ASTTupleQuerySet;
import org.openrdf.query.parser.serql.ast.ASTTupleUnion;
import org.openrdf.query.parser.serql.ast.ASTURI;
import org.openrdf.query.parser.serql.ast.ASTValue;
import org.openrdf.query.parser.serql.ast.ASTValueExpr;
import org.openrdf.query.parser.serql.ast.ASTVar;
import org.openrdf.query.parser.serql.ast.ASTWhere;
import org.openrdf.query.parser.serql.ast.Node;

class QueryModelBuilder {

	public static TupleExpr buildQueryModel(ASTQueryContainer node, ValueFactory valueFactory)
		throws MalformedQueryException
	{
		QueryModelBuilder qmBuilder = new QueryModelBuilder(valueFactory);
		return qmBuilder._buildQueryContainer(node);
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueFactory _valueFactory;

	private int _constantVarID = 1;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public QueryModelBuilder(ValueFactory valueFactory) {
		_valueFactory = valueFactory;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Var _createConstantVar(Value value) {
		Var var = new Var("-const-" + _constantVarID++);
		var.setAnonymous(true);
		var.setValue(value);
		return var;
	}

	private TupleExpr _buildQueryContainer(ASTQueryContainer node)
		throws MalformedQueryException
	{
		return _buildQuery(node.getQuery());
	}

	private TupleExpr _buildQuery(ASTQuery queryNode)
		throws MalformedQueryException
	{
		if (queryNode instanceof ASTTupleQuery) {
			return _buildTupleQuery((ASTTupleQuery)queryNode);
		}
		else if (queryNode instanceof ASTGraphQuery) {
			return _buildGraphQuery((ASTGraphQuery)queryNode);
		}
		else {
			throw new IllegalArgumentException("Unexpected argument type: " + queryNode.getClass());
		}
	}

	private TupleExpr _buildTupleQuery(ASTTupleQuery tqNode)
		throws MalformedQueryException
	{
		if (tqNode instanceof ASTTupleQuerySet) {
			return _buildTupleQuerySet((ASTTupleQuerySet)tqNode);
		}
		else if (tqNode instanceof ASTSelectQuery) {
			return _buildSelectQuery((ASTSelectQuery)tqNode);
		}
		else {
			throw new IllegalArgumentException("Unexpected argument type: " + tqNode.getClass());
		}
	}

	private TupleExpr _buildTupleQuerySet(ASTTupleQuerySet tqsNode)
		throws MalformedQueryException
	{
		TupleExpr leftArg = _buildTupleQuery(tqsNode.getLeftArg());
		TupleExpr rightArg = _buildTupleQuery(tqsNode.getRightArg());

		TupleExpr result;

		if (tqsNode instanceof ASTTupleUnion) {
			result = new Union(leftArg, rightArg);

			if (((ASTTupleUnion)tqsNode).isDistinct()) {
				result = new Distinct(result);
			}
		}
		else if (tqsNode instanceof ASTTupleMinus) {
			result = new Difference(leftArg, rightArg);
		}
		else if (tqsNode instanceof ASTTupleIntersect) {
			result = new Intersection(leftArg, rightArg);
		}
		else {
			throw new IllegalArgumentException("Unexpected argument type: " + tqsNode.getClass());
		}

		return result;
	}

	private TupleExpr _buildGraphQuery(ASTGraphQuery gqNode)
		throws MalformedQueryException
	{
		if (gqNode instanceof ASTGraphQuerySet) {
			return _buildGraphQuerySet((ASTGraphQuerySet)gqNode);
		}
		else if (gqNode instanceof ASTConstructQuery) {
			return _buildConstructQuery((ASTConstructQuery)gqNode);
		}
		else {
			throw new IllegalArgumentException("Unexpected argument type: " + gqNode.getClass());
		}
	}

	private TupleExpr _buildGraphQuerySet(ASTGraphQuerySet gqsNode)
		throws MalformedQueryException
	{
		TupleExpr leftArg = _buildGraphQuery(gqsNode.getLeftArg());
		TupleExpr rightArg = _buildGraphQuery(gqsNode.getRightArg());

		TupleExpr result;

		if (gqsNode instanceof ASTGraphUnion) {
			result = new Union(leftArg, rightArg);

			if (((ASTGraphUnion)gqsNode).isDistinct()) {
				result = new Distinct(result);
			}
		}
		else if (gqsNode instanceof ASTGraphMinus) {
			result = new Difference(leftArg, rightArg);
		}
		else if (gqsNode instanceof ASTGraphIntersect) {
			result = new Intersection(leftArg, rightArg);
		}
		else {
			throw new IllegalArgumentException("Unexpected argument type: " + gqsNode.getClass());
		}

		return result;
	}

	private TupleExpr _buildSelectQuery(ASTSelectQuery sqNode)
		throws MalformedQueryException
	{
		TupleExpr tupleExpr;

		ASTQueryBody queryBodyNode = sqNode.getQueryBody();

		if (queryBodyNode != null) {
			// Build tuple expression for query body
			tupleExpr = _buildQueryBody(queryBodyNode);
		}
		else {
			tupleExpr = new SingletonSet();
		}

		// Apply projection
		tupleExpr = _addProjection(sqNode.getSelectClause(), tupleExpr);

		return tupleExpr;
	}

	private TupleExpr _addProjection(ASTSelect node, TupleExpr tupleExpr)
		throws MalformedQueryException
	{
		Extension extension = new Extension(tupleExpr);
		Projection projection = new Projection(extension);

		for (ASTProjectionElem projElemNode : node.getProjectionElemList()) {
			ValueExpr valueExpr = _buildValueExpr(projElemNode.getValueExpr());

			String alias = projElemNode.getAlias();
			if (alias != null) {
				// aliased projection element
				extension.add(new ExtensionElem(valueExpr, alias));
				projection.add(new ProjectionElem(alias));
			}
			else if (valueExpr instanceof Var) {
				// unaliased variable
				Var projVar = (Var)valueExpr;
				projection.add(new ProjectionElem(projVar.getName()));
			}
			else {
				throw new IllegalStateException("required alias for non-Var projection elements not found");
			}
		}

		if (node.isDistinct()) {
			return new Distinct(projection);
		}
		else {
			return projection;
		}
	}

	private TupleExpr _buildConstructQuery(ASTConstructQuery node)
		throws MalformedQueryException
	{
		TupleExpr tupleExpr;

		if (node.hasQueryBody()) {
			// Build tuple expression for query body
			tupleExpr = _buildQueryBody(node.getQueryBody());
		}
		else {
			tupleExpr = new SingletonSet();
		}

		// Create constructor
		ConstructorBuilder cb = new ConstructorBuilder();
		ASTConstruct constructNode = node.getConstructClause();

		if (!constructNode.isWildcard()) {
			TupleExpr constructExpr = _buildConstructor(constructNode);
			tupleExpr = cb.buildConstructor(tupleExpr, constructExpr);
		}
		else if (node.hasQueryBody()) {
			tupleExpr = cb.buildConstructor(tupleExpr);
		}
		// else: "construct *" without query body, just return the SingletonSet

		if (constructNode.isDistinct() && node.hasQueryBody()) {
			tupleExpr = new Distinct(tupleExpr);
		}

		return tupleExpr;
	}

	private TupleExpr _buildConstructor(ASTConstruct constructNode)
		throws MalformedQueryException
	{
		assert !constructNode.isWildcard() : "Cannot build constructor for wildcards";

		GraphPattern graphPattern = new GraphPattern();

		for (ASTPathExpr pathExprNode : constructNode.getPathExprList()) {
			_buildGraphPattern(pathExprNode, graphPattern);
		}

		return graphPattern.buildTupleExpr();
	}

	private TupleExpr _buildQueryBody(ASTQueryBody node)
		throws MalformedQueryException
	{
		GraphPattern graphPattern = new GraphPattern();

		for (ASTFrom fromClause : node.getFromClauseList()) {
			_buildGraphPattern(fromClause, graphPattern);
		}

		ASTWhere whereClause = node.getWhereClause();
		if (whereClause != null) {
			_buildGraphPattern(whereClause, graphPattern);
		}

		ASTLimit limitClause = node.getLimitClause();
		if (limitClause != null) {
			_buildGraphPattern(limitClause, graphPattern);
		}

		ASTOffset offsetClause = node.getOffsetClause();
		if (offsetClause != null) {
			_buildGraphPattern(offsetClause, graphPattern);
		}

		return graphPattern.buildTupleExpr();
	}

	private void _buildGraphPattern(ASTFrom node, GraphPattern graphPattern)
		throws MalformedQueryException
	{
		ASTValueExpr contextNode = node.getContextID();

		if (contextNode != null) {
			ValueExpr contextID = _buildValueExpr(contextNode);

			Var contextVar;
			if (contextID instanceof Var) {
				contextVar = (Var)contextID;
			}
			else if (contextID instanceof ValueConstant) {
				ValueConstant vc = (ValueConstant)contextID;
				contextVar = _createConstantVar(vc.getValue());
			}
			else {
				throw new IllegalArgumentException("Unexpected contextID result type: " + contextID.getClass());
			}

			graphPattern.setContextVar(contextVar);
			graphPattern.setStatementPatternScope(StatementPattern.Scope.NAMED_CONTEXTS);
		}

		for (ASTPathExpr pathExprNode : node.getPathExprList()) {
			_buildGraphPattern(pathExprNode, graphPattern);
		}
	}

	private void _buildGraphPattern(ASTWhere node, GraphPattern graphPattern)
		throws MalformedQueryException
	{
		graphPattern.addConstraint(_buildBooleanExpr(node.getCondition()));
	}

	private void _buildGraphPattern(ASTLimit node, GraphPattern graphPattern)
		throws MalformedQueryException
	{
		graphPattern.setLimit(node.getValue());
	}

	private void _buildGraphPattern(ASTOffset node, GraphPattern graphPattern)
		throws MalformedQueryException
	{
		graphPattern.setOffset(node.getValue());
	}

	private void _buildGraphPattern(ASTPathExpr node, GraphPattern graphPattern)
		throws MalformedQueryException
	{
		if (node instanceof ASTBasicPathExpr) {
			_buildGraphPattern((ASTBasicPathExpr)node, graphPattern);
		}
		else if (node instanceof ASTOptPathExpr) {
			_buildGraphPattern((ASTOptPathExpr)node, graphPattern);
		}
		else {
			throw new IllegalArgumentException("Unknown path expression type: " + node.getClass());
		}
	}

	private void _buildGraphPattern(ASTBasicPathExpr node, GraphPattern graphPattern)
		throws MalformedQueryException
	{
		// process subject node
		List<Var> subjVars = _buildGraphPattern(node.getHead(), graphPattern);

		// supply subject vars to tail segment
		_buildGraphPattern(subjVars, node.getTail(), graphPattern);
	}

	private void _buildGraphPattern(ASTOptPathExpr node, GraphPattern graphPattern)
		throws MalformedQueryException
	{
		// Create new sub-graph pattern for optional path expressions
		GraphPattern optGraphPattern = new GraphPattern();

		for (ASTPathExpr pathExprNode : node.getPathExprList()) {
			_buildGraphPattern(pathExprNode, optGraphPattern);
		}

		ASTWhere whereClause = node.getWhereClause();
		if (whereClause != null) {
			_buildGraphPattern(whereClause, optGraphPattern);
		}

		graphPattern.addOptionalTE(optGraphPattern.buildTupleExpr());
	}

	private void _buildGraphPattern(List<Var> subjVars, ASTPathExprTail tailNode, GraphPattern graphPattern)
		throws MalformedQueryException
	{
		if (tailNode instanceof ASTBasicPathExprTail) {
			_buildGraphPattern(subjVars, (ASTBasicPathExprTail)tailNode, graphPattern);
		}
		else if (tailNode instanceof ASTOptPathExprTail) {
			_buildGraphPattern(subjVars, (ASTOptPathExprTail)tailNode, graphPattern);
		}
		else {
			throw new IllegalArgumentException("Unknown path expression tail type: " + tailNode.getClass());
		}
	}

	private void _buildGraphPattern(List<Var> subjVars, ASTBasicPathExprTail tailNode,
			GraphPattern graphPattern)
		throws MalformedQueryException
	{
		Var predVar = _buildGraphPattern(tailNode.getEdge(), graphPattern);
		List<Var> objVars = _buildGraphPattern(tailNode.getNode(), graphPattern);

		Var contextVar = graphPattern.getContextVar();
		StatementPattern.Scope spScope = graphPattern.getStatementPatternScope();

		for (Var subjVar : subjVars) {
			for (Var objVar : objVars) {
				StatementPattern sp = new StatementPattern(spScope, subjVar, predVar, objVar, contextVar);
				graphPattern.addRequiredTE(sp);
			}
		}

		// Process next tail segment
		ASTPathExprTail nextTailNode = tailNode.getNextTail();
		if (nextTailNode != null) {
			List<Var> joinVars = nextTailNode.isBranch() ? subjVars : objVars;
			_buildGraphPattern(joinVars, nextTailNode, graphPattern);
		}
	}

	private void _buildGraphPattern(List<Var> subjVars, ASTOptPathExprTail tailNode, GraphPattern graphPattern)
		throws MalformedQueryException
	{
		// Create new sub-graph pattern for optional path expressions
		GraphPattern optGraphPattern = new GraphPattern();

		// optional path expression tail
		_buildGraphPattern(subjVars, tailNode.getOptionalTail(), optGraphPattern);

		ASTWhere whereNode = tailNode.getWhereClause();
		if (whereNode != null) {
			// boolean contraint on optional path expression tail
			_buildGraphPattern(whereNode, optGraphPattern);
		}

		graphPattern.addOptionalTE(optGraphPattern.buildTupleExpr());

		ASTPathExprTail nextTailNode = tailNode.getNextTail();
		if (nextTailNode != null) {
			// branch after optional path expression tail
			_buildGraphPattern(subjVars, nextTailNode, graphPattern);
		}
	}

	private Var _buildGraphPattern(ASTEdge node, GraphPattern graphPattern)
		throws MalformedQueryException
	{
		ValueExpr arg = _buildValueExpr(node.getValueExpr());

		if (arg instanceof Var) {
			return (Var)arg;
		}
		else if (arg instanceof ValueConstant) {
			ValueConstant vc = (ValueConstant)arg;
			return _createConstantVar(vc.getValue());
		}
		else {
			throw new IllegalArgumentException("Unexpected edge argument type: " + arg.getClass());
		}
	}

	private List<Var> _buildGraphPattern(ASTNode node, GraphPattern graphPattern)
		throws MalformedQueryException
	{
		List<Var> nodeVars = new ArrayList<Var>();

		for (ASTNodeElem nodeElem : node.getNodeElemList()) {
			nodeVars.add(_buildGraphPattern(nodeElem, graphPattern));
		}

		// Create any implicit unequalities
		for (int i = 0; i < nodeVars.size() - 1; i++) {
			Var var1 = nodeVars.get(i);

			for (int j = i + 1; j < nodeVars.size(); j++) {
				Var var2 = nodeVars.get(j);

				// At least one of the variables should be non-constant
				// for the unequality to make any sense:
				if (!var1.hasValue() || !var2.hasValue()) {
					graphPattern.addConstraint(new Compare(var1, var2, CompareOp.NE));
				}
			}
		}

		return nodeVars;
	}

	private Var _buildGraphPattern(ASTNodeElem node, GraphPattern graphPattern)
		throws MalformedQueryException
	{
		Node childNode = node.getChild();
		ValueExpr valueExpr;

		if (childNode instanceof ASTValueExpr) {
			valueExpr = _buildValueExpr((ASTValueExpr)childNode);
		}
		else if (childNode instanceof ASTReifiedStat) {
			valueExpr = _buildGraphPattern((ASTReifiedStat)childNode, graphPattern);
		}
		else {
			throw new IllegalArgumentException("Unexpected node element argument type: " + childNode.getClass());
		}

		if (valueExpr instanceof Var) {
			return (Var)valueExpr;
		}
		else if (valueExpr instanceof ValueConstant) {
			ValueConstant vc = (ValueConstant)valueExpr;
			return _createConstantVar(vc.getValue());
		}
		else {
			throw new IllegalArgumentException("Unexpected node element result type: " + valueExpr.getClass());
		}
	}

	private Var _buildGraphPattern(ASTReifiedStat node, GraphPattern graphPattern)
		throws MalformedQueryException
	{
		assert node.getID() != null : "ID variable not set";

		Var subjVar = _buildGraphPattern(node.getSubject(), graphPattern);
		Var predVar = _buildGraphPattern(node.getPredicate(), graphPattern);
		Var objVar = _buildGraphPattern(node.getObject(), graphPattern);
		Var idVar = _buildVar(node.getID());

		Var contextVar = graphPattern.getContextVar();
		StatementPattern.Scope spScope = graphPattern.getStatementPatternScope();

		Var rdfType = new Var("_rdfType", RDF.TYPE);
		Var rdfStatement = new Var("_rdfStatement", RDF.STATEMENT);
		Var rdfSubject = new Var("_rdfSubject", RDF.SUBJECT);
		Var rdfPredicate = new Var("_rdfPredicate", RDF.PREDICATE);
		Var rdfObject = new Var("_rdfObject", RDF.OBJECT);

		graphPattern.addRequiredTE(new StatementPattern(spScope, idVar, rdfType, rdfStatement, contextVar));
		graphPattern.addRequiredTE(new StatementPattern(spScope, idVar, rdfSubject, subjVar, contextVar));
		graphPattern.addRequiredTE(new StatementPattern(spScope, idVar, rdfPredicate, predVar, contextVar));
		graphPattern.addRequiredTE(new StatementPattern(spScope, idVar, rdfObject, objVar, contextVar));

		return idVar;
	}

	private ValueExpr _buildBooleanExpr(ASTBooleanExpr boolNode)
		throws MalformedQueryException
	{
		if (boolNode instanceof ASTAnd) {
			return _buildAnd((ASTAnd)boolNode);
		}
		else if (boolNode instanceof ASTOr) {
			return _buildOr((ASTOr)boolNode);
		}
		else if (boolNode instanceof ASTBooleanConstant) {
			return _buildTrue((ASTBooleanConstant)boolNode);
		}
		else if (boolNode instanceof ASTNot) {
			return _buildNot((ASTNot)boolNode);
		}
		else if (boolNode instanceof ASTIsResource) {
			return _buildIsResource((ASTIsResource)boolNode);
		}
		else if (boolNode instanceof ASTIsLiteral) {
			return _buildIsLiteral((ASTIsLiteral)boolNode);
		}
		else if (boolNode instanceof ASTIsURI) {
			return _buildIsURI((ASTIsURI)boolNode);
		}
		else if (boolNode instanceof ASTIsBNode) {
			return _buildIsBNode((ASTIsBNode)boolNode);
		}
		else if (boolNode instanceof ASTExists) {
			return _buildExists((ASTExists)boolNode);
		}
		else if (boolNode instanceof ASTCompare) {
			return _buildCompare((ASTCompare)boolNode);
		}
		else if (boolNode instanceof ASTCompareAny) {
			return _buildCompareAny((ASTCompareAny)boolNode);
		}
		else if (boolNode instanceof ASTCompareAll) {
			return _buildCompareAll((ASTCompareAll)boolNode);
		}
		else if (boolNode instanceof ASTLike) {
			return _buildLike((ASTLike)boolNode);
		}
		else if (boolNode instanceof ASTIn) {
			return _buildIn((ASTIn)boolNode);
		}
		else {
			throw new IllegalArgumentException("Unexpected argument type: " + boolNode.getClass());
		}
	}

	private ValueExpr _buildOr(ASTOr node)
		throws MalformedQueryException
	{
		Iterator<ASTBooleanExpr> iter = node.getOperandList().iterator();

		ValueExpr result = _buildBooleanExpr(iter.next());

		while (iter.hasNext()) {
			ValueExpr operand = _buildBooleanExpr(iter.next());
			result = new Or(result, operand);
		}

		return result;
	}

	private ValueExpr _buildAnd(ASTAnd node)
		throws MalformedQueryException
	{
		Iterator<ASTBooleanExpr> iter = node.getOperandList().iterator();

		ValueExpr result = _buildBooleanExpr(iter.next());

		while (iter.hasNext()) {
			ValueExpr operand = _buildBooleanExpr(iter.next());
			result = new And(result, operand);
		}

		return result;
	}

	private ValueConstant _buildTrue(ASTBooleanConstant node)
		throws MalformedQueryException
	{
		return new ValueConstant(_valueFactory.createLiteral(node.getValue()));
	}

	private Not _buildNot(ASTNot node)
		throws MalformedQueryException
	{
		return new Not(_buildBooleanExpr(node.getOperand()));
	}

	private IsResource _buildIsResource(ASTIsResource node)
		throws MalformedQueryException
	{
		return new IsResource(_buildVar(node.getOperand()));
	}

	private IsLiteral _buildIsLiteral(ASTIsLiteral node)
		throws MalformedQueryException
	{
		return new IsLiteral(_buildVar(node.getOperand()));
	}

	private IsURI _buildIsURI(ASTIsURI node)
		throws MalformedQueryException
	{
		return new IsURI(_buildVar(node.getOperand()));
	}

	private IsBNode _buildIsBNode(ASTIsBNode node)
		throws MalformedQueryException
	{
		return new IsBNode(_buildVar(node.getOperand()));
	}

	private Exists _buildExists(ASTExists node)
		throws MalformedQueryException
	{
		return new Exists(_buildTupleQuery(node.getOperand()));
	}

	private Compare _buildCompare(ASTCompare node)
		throws MalformedQueryException
	{
		ValueExpr leftArg = _buildValueExpr(node.getLeftOperand());
		ValueExpr rightArg = _buildValueExpr(node.getRightOperand());
		CompareOp operator = node.getOperator().getValue();

		return new Compare(leftArg, rightArg, operator);
	}

	private CompareAny _buildCompareAny(ASTCompareAny node)
		throws MalformedQueryException
	{
		ValueExpr valueExpr = _buildValueExpr(node.getLeftOperand());
		TupleExpr tupleExpr = _buildTupleQuery(node.getRightOperand());
		CompareOp op = node.getOperator().getValue();

		return new CompareAny(valueExpr, tupleExpr, op);
	}

	private CompareAll _buildCompareAll(ASTCompareAll node)
		throws MalformedQueryException
	{
		ValueExpr valueExpr = _buildValueExpr(node.getLeftOperand());
		TupleExpr tupleExpr = _buildTupleQuery(node.getRightOperand());
		CompareOp op = node.getOperator().getValue();

		return new CompareAll(valueExpr, tupleExpr, op);
	}

	private Like _buildLike(ASTLike node)
		throws MalformedQueryException
	{
		ValueExpr expr = _buildValueExpr(node.getValueExpr());
		String pattern = _buildString(node.getPattern());
		boolean caseSensitive = !node.ignoreCase();

		return new Like(expr, pattern, caseSensitive);
	}

	private In _buildIn(ASTIn node)
		throws MalformedQueryException
	{
		ValueExpr valueExpr = _buildValueExpr(node.getLeftOperand());
		TupleExpr tupleExpr = _buildTupleQuery(node.getRightOperand());
		return new In(valueExpr, tupleExpr);
	}

	private ValueExpr _buildValueExpr(ASTValueExpr valueNode)
		throws MalformedQueryException
	{
		if (valueNode instanceof ASTVar) {
			return _buildVar((ASTVar)valueNode);
		}
		else if (valueNode instanceof ASTValue) {
			return _buildValueConstant((ASTValue)valueNode);
		}
		else if (valueNode instanceof ASTDatatype) {
			return _buildDatatype((ASTDatatype)valueNode);
		}
		else if (valueNode instanceof ASTLang) {
			return _buildLang((ASTLang)valueNode);
		}
		else if (valueNode instanceof ASTLabel) {
			return _buildLabel((ASTLabel)valueNode);
		}
		else if (valueNode instanceof ASTNamespace) {
			return _buildNamespace((ASTNamespace)valueNode);
		}
		else if (valueNode instanceof ASTLocalName) {
			return _buildLocalName((ASTLocalName)valueNode);
		}
		else if (valueNode instanceof ASTNull) {
			return _buildNull((ASTNull)valueNode);
		}
		else {
			throw new IllegalArgumentException("Unexpected argument type: " + valueNode.getClass());
		}
	}

	private Var _buildVar(ASTVar node)
		throws MalformedQueryException
	{
		Var var = new Var(node.getName());
		var.setAnonymous(node.isAnonymous());
		return var;
	}

	private ValueConstant _buildValueConstant(ASTValue valueNode)
		throws MalformedQueryException
	{
		Value value;

		if (valueNode instanceof ASTURI) {
			value = _buildURI((ASTURI)valueNode);
		}
		else if (valueNode instanceof ASTBNode) {
			value = _buildBNode((ASTBNode)valueNode);
		}
		else if (valueNode instanceof ASTLiteral) {
			value = _buildLiteral((ASTLiteral)valueNode);
		}
		else {
			throw new IllegalArgumentException("Unexpected argument type: " + valueNode.getClass());
		}

		return new ValueConstant(value);
	}

	private DatatypeFunc _buildDatatype(ASTDatatype node)
		throws MalformedQueryException
	{
		return new DatatypeFunc(_buildVar(node.getOperand()));
	}

	private LangFunc _buildLang(ASTLang node)
		throws MalformedQueryException
	{
		return new LangFunc(_buildVar(node.getOperand()));
	}

	private LabelFunc _buildLabel(ASTLabel node)
		throws MalformedQueryException
	{
		return new LabelFunc(_buildVar(node.getOperand()));
	}

	private NamespaceFunc _buildNamespace(ASTNamespace node)
		throws MalformedQueryException
	{
		return new NamespaceFunc(_buildVar(node.getOperand()));
	}

	private LocalNameFunc _buildLocalName(ASTLocalName node)
		throws MalformedQueryException
	{
		return new LocalNameFunc(_buildVar(node.getOperand()));
	}

	private Null _buildNull(ASTNull node)
		throws MalformedQueryException
	{
		return new Null();
	}

	private URI _buildURI(ASTURI node)
		throws MalformedQueryException
	{
		return _valueFactory.createURI(node.getValue());
	}

	private BNode _buildBNode(ASTBNode node)
		throws MalformedQueryException
	{
		return _valueFactory.createBNode(node.getID());
	}

	private Literal _buildLiteral(ASTLiteral litNode)
		throws MalformedQueryException
	{
		// Get datatype URI from child URI node, if present
		URI datatype = null;
		ASTValueExpr dtNode = litNode.getDatatypeNode();
		if (dtNode instanceof ASTURI) {
			datatype = _buildURI((ASTURI)dtNode);
		}
		else if (dtNode != null) {
			throw new IllegalArgumentException("Unexpected datatype type: " + dtNode.getClass());
		}

		if (datatype != null) {
			return _valueFactory.createLiteral(litNode.getLabel(), datatype);
		}
		else if (litNode.hasLang()) {
			return _valueFactory.createLiteral(litNode.getLabel(), litNode.getLang());
		}
		else {
			return _valueFactory.createLiteral(litNode.getLabel());
		}
	}

	private String _buildString(ASTString node)
		throws MalformedQueryException
	{
		return node.getValue();
	}
}
