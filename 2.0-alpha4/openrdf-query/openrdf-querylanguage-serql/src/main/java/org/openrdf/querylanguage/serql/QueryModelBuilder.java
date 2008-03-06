/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openrdf.querylanguage.MalformedQueryException;
import org.openrdf.querylanguage.serql.ast.ASTAnd;
import org.openrdf.querylanguage.serql.ast.ASTBNode;
import org.openrdf.querylanguage.serql.ast.ASTBasicPathExpr;
import org.openrdf.querylanguage.serql.ast.ASTBasicPathExprTail;
import org.openrdf.querylanguage.serql.ast.ASTBooleanExpr;
import org.openrdf.querylanguage.serql.ast.ASTCompare;
import org.openrdf.querylanguage.serql.ast.ASTCompareAll;
import org.openrdf.querylanguage.serql.ast.ASTCompareAny;
import org.openrdf.querylanguage.serql.ast.ASTConstruct;
import org.openrdf.querylanguage.serql.ast.ASTConstructQuery;
import org.openrdf.querylanguage.serql.ast.ASTDatatype;
import org.openrdf.querylanguage.serql.ast.ASTEdge;
import org.openrdf.querylanguage.serql.ast.ASTExists;
import org.openrdf.querylanguage.serql.ast.ASTFalse;
import org.openrdf.querylanguage.serql.ast.ASTFrom;
import org.openrdf.querylanguage.serql.ast.ASTGraphIntersect;
import org.openrdf.querylanguage.serql.ast.ASTGraphMinus;
import org.openrdf.querylanguage.serql.ast.ASTGraphQuery;
import org.openrdf.querylanguage.serql.ast.ASTGraphQuerySet;
import org.openrdf.querylanguage.serql.ast.ASTGraphUnion;
import org.openrdf.querylanguage.serql.ast.ASTIn;
import org.openrdf.querylanguage.serql.ast.ASTIsBNode;
import org.openrdf.querylanguage.serql.ast.ASTIsLiteral;
import org.openrdf.querylanguage.serql.ast.ASTIsResource;
import org.openrdf.querylanguage.serql.ast.ASTIsURI;
import org.openrdf.querylanguage.serql.ast.ASTLabel;
import org.openrdf.querylanguage.serql.ast.ASTLang;
import org.openrdf.querylanguage.serql.ast.ASTLike;
import org.openrdf.querylanguage.serql.ast.ASTLimit;
import org.openrdf.querylanguage.serql.ast.ASTLiteral;
import org.openrdf.querylanguage.serql.ast.ASTLocalName;
import org.openrdf.querylanguage.serql.ast.ASTNamespace;
import org.openrdf.querylanguage.serql.ast.ASTNamespaceDecl;
import org.openrdf.querylanguage.serql.ast.ASTNode;
import org.openrdf.querylanguage.serql.ast.ASTNodeElem;
import org.openrdf.querylanguage.serql.ast.ASTNot;
import org.openrdf.querylanguage.serql.ast.ASTNull;
import org.openrdf.querylanguage.serql.ast.ASTOffset;
import org.openrdf.querylanguage.serql.ast.ASTOptPathExpr;
import org.openrdf.querylanguage.serql.ast.ASTOptPathExprTail;
import org.openrdf.querylanguage.serql.ast.ASTOr;
import org.openrdf.querylanguage.serql.ast.ASTQueryContainer;
import org.openrdf.querylanguage.serql.ast.ASTPathExpr;
import org.openrdf.querylanguage.serql.ast.ASTPathExprTail;
import org.openrdf.querylanguage.serql.ast.ASTProjectionElem;
import org.openrdf.querylanguage.serql.ast.ASTQName;
import org.openrdf.querylanguage.serql.ast.ASTQuery;
import org.openrdf.querylanguage.serql.ast.ASTQueryBody;
import org.openrdf.querylanguage.serql.ast.ASTReifiedStat;
import org.openrdf.querylanguage.serql.ast.ASTSelect;
import org.openrdf.querylanguage.serql.ast.ASTSelectQuery;
import org.openrdf.querylanguage.serql.ast.ASTString;
import org.openrdf.querylanguage.serql.ast.ASTTrue;
import org.openrdf.querylanguage.serql.ast.ASTTupleIntersect;
import org.openrdf.querylanguage.serql.ast.ASTTupleMinus;
import org.openrdf.querylanguage.serql.ast.ASTTupleQuery;
import org.openrdf.querylanguage.serql.ast.ASTTupleQuerySet;
import org.openrdf.querylanguage.serql.ast.ASTTupleUnion;
import org.openrdf.querylanguage.serql.ast.ASTURI;
import org.openrdf.querylanguage.serql.ast.ASTValue;
import org.openrdf.querylanguage.serql.ast.ASTValueExpr;
import org.openrdf.querylanguage.serql.ast.ASTVar;
import org.openrdf.querylanguage.serql.ast.ASTWhere;
import org.openrdf.querylanguage.serql.ast.Node;
import org.openrdf.querymodel.And;
import org.openrdf.querymodel.BooleanConstant;
import org.openrdf.querymodel.BooleanExpr;
import org.openrdf.querymodel.Compare;
import org.openrdf.querymodel.CompareAll;
import org.openrdf.querymodel.CompareAny;
import org.openrdf.querymodel.Datatype;
import org.openrdf.querymodel.Difference;
import org.openrdf.querymodel.Distinct;
import org.openrdf.querymodel.Exists;
import org.openrdf.querymodel.Extension;
import org.openrdf.querymodel.ExtensionElem;
import org.openrdf.querymodel.In;
import org.openrdf.querymodel.Intersection;
import org.openrdf.querymodel.IsBNode;
import org.openrdf.querymodel.IsLiteral;
import org.openrdf.querymodel.IsResource;
import org.openrdf.querymodel.IsURI;
import org.openrdf.querymodel.Label;
import org.openrdf.querymodel.Lang;
import org.openrdf.querymodel.Like;
import org.openrdf.querymodel.LocalName;
import org.openrdf.querymodel.Namespace;
import org.openrdf.querymodel.Not;
import org.openrdf.querymodel.Null;
import org.openrdf.querymodel.Or;
import org.openrdf.querymodel.Projection;
import org.openrdf.querymodel.ProjectionElem;
import org.openrdf.querymodel.SingletonSet;
import org.openrdf.querymodel.StatementPattern;
import org.openrdf.querymodel.TupleExpr;
import org.openrdf.querymodel.Union;
import org.openrdf.querymodel.ValueConstant;
import org.openrdf.querymodel.ValueExpr;
import org.openrdf.querymodel.Var;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.model.vocabulary.XMLSchema;

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

	private Map<String, String> _namespaces = new HashMap<String, String>();

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

	public String getNamespace(String prefix) {
		String namespace = _namespaces.get(prefix);

		if (namespace == null) {
			// Use default namespace prefixes when not defined explicitly
			if (prefix.equals("rdf")) {
				namespace = RDF.NAMESPACE;
			}
			else if (prefix.equals("rdfs")) {
				namespace = RDFS.NAMESPACE;
			}
			else if (prefix.equals("xsd")) {
				namespace = XMLSchema.NAMESPACE;
			}
			else if (prefix.equals("owl")) {
				namespace = OWL.NAMESPACE;
			}
			else if (prefix.equals("sesame")) {
				namespace = SESAME.NAMESPACE;
			}
			// For backwards compatibility:
			else if (prefix.equals("serql")) {
				namespace = SESAME.NAMESPACE;
			}
		}

		return namespace;
	}

	public Var _createConstantVar(Value value) {
		Var var = new Var("-const-" + _constantVarID++);
		var.setAnonymous(true);
		var.setValue(value);
		return var;
	}

	private TupleExpr _buildQueryContainer(ASTQueryContainer node)
		throws MalformedQueryException
	{
		if (node.hasNamespaceDeclList()) {
			// Process namespace declarations first
			for (ASTNamespaceDecl nsDecl : node.getNamespaceDeclList()) {
				_processNamespaceDecl(nsDecl);
			}
		}

		return _buildQuery(node.getQuery());
	}

	private void _processNamespaceDecl(ASTNamespaceDecl nsDecl)
		throws MalformedQueryException
	{
		String prefix = nsDecl.getPrefix();
		String namespace = nsDecl.getURI().getValue();

		if (_namespaces.containsKey(prefix)) {
			// Prefix already defined

			if (_namespaces.get(prefix).equals(namespace)) {
				// duplicate, ignore
			}
			else {
				throw new MalformedQueryException("Multiple namespace declarations for prefix '" + prefix + "'");
			}
		}
		else {
			_namespaces.put(prefix, namespace);
		}
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

		// FIXME: project the right argument's results on the left argument's
		// results?
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
					graphPattern.addConstraint(new Compare(var1, var2, Compare.Operator.NE));
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

	private BooleanExpr _buildBooleanExpr(ASTBooleanExpr boolNode)
		throws MalformedQueryException
	{
		if (boolNode instanceof ASTAnd) {
			return _buildAnd((ASTAnd)boolNode);
		}
		else if (boolNode instanceof ASTOr) {
			return _buildOr((ASTOr)boolNode);
		}
		else if (boolNode instanceof ASTTrue) {
			return _buildTrue((ASTTrue)boolNode);
		}
		else if (boolNode instanceof ASTFalse) {
			return _buildFalse((ASTFalse)boolNode);
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

	private BooleanExpr _buildOr(ASTOr node)
		throws MalformedQueryException
	{
		Iterator<ASTBooleanExpr> iter = node.getOperandList().iterator();

		BooleanExpr result = _buildBooleanExpr(iter.next());

		while (iter.hasNext()) {
			BooleanExpr operand = _buildBooleanExpr(iter.next());
			result = new Or(result, operand);
		}

		return result;
	}

	private BooleanExpr _buildAnd(ASTAnd node)
		throws MalformedQueryException
	{
		Iterator<ASTBooleanExpr> iter = node.getOperandList().iterator();

		BooleanExpr result = _buildBooleanExpr(iter.next());

		while (iter.hasNext()) {
			BooleanExpr operand = _buildBooleanExpr(iter.next());
			result = new And(result, operand);
		}

		return result;
	}

	private BooleanConstant _buildTrue(ASTTrue node)
		throws MalformedQueryException
	{
		return BooleanConstant.TRUE;
	}

	private BooleanConstant _buildFalse(ASTFalse node)
		throws MalformedQueryException
	{
		return BooleanConstant.FALSE;
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

	private BooleanExpr _buildExists(ASTExists node)
		throws MalformedQueryException
	{
		return new Exists(_buildTupleQuery(node.getOperand()));
	}

	private Compare _buildCompare(ASTCompare node)
		throws MalformedQueryException
	{
		ValueExpr leftArg = _buildValueExpr(node.getLeftOperand());
		ValueExpr rightArg = _buildValueExpr(node.getRightOperand());
		Compare.Operator operator = node.getOperator().getValue();

		return new Compare(leftArg, rightArg, operator);
	}

	private BooleanExpr _buildCompareAny(ASTCompareAny node)
		throws MalformedQueryException
	{
		ValueExpr valueExpr = _buildValueExpr(node.getLeftOperand());
		TupleExpr tupleExpr = _buildTupleQuery(node.getRightOperand());
		Compare.Operator op = node.getOperator().getValue();

		return new CompareAny(valueExpr, tupleExpr, op);
	}

	private BooleanExpr _buildCompareAll(ASTCompareAll node)
		throws MalformedQueryException
	{
		ValueExpr valueExpr = _buildValueExpr(node.getLeftOperand());
		TupleExpr tupleExpr = _buildTupleQuery(node.getRightOperand());
		Compare.Operator op = node.getOperator().getValue();

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

	private BooleanExpr _buildIn(ASTIn node)
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
		else if (valueNode instanceof ASTQName) {
			value = _buildQName((ASTQName)valueNode);
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

	private Datatype _buildDatatype(ASTDatatype node)
		throws MalformedQueryException
	{
		return new Datatype(_buildVar(node.getOperand()));
	}

	private Lang _buildLang(ASTLang node)
		throws MalformedQueryException
	{
		return new Lang(_buildVar(node.getOperand()));
	}

	private Label _buildLabel(ASTLabel node)
		throws MalformedQueryException
	{
		return new Label(_buildVar(node.getOperand()));
	}

	private Namespace _buildNamespace(ASTNamespace node)
		throws MalformedQueryException
	{
		return new Namespace(_buildVar(node.getOperand()));
	}

	private LocalName _buildLocalName(ASTLocalName node)
		throws MalformedQueryException
	{
		return new LocalName(_buildVar(node.getOperand()));
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

	private URI _buildQName(ASTQName node)
		throws MalformedQueryException
	{
		String qname = node.getValue();

		int colonIdx = qname.indexOf(':');
		assert colonIdx >= 0 : "Malformed QName: " + qname;

		String prefix = qname.substring(0, colonIdx);
		String localName = qname.substring(colonIdx + 1);

		String namespace = getNamespace(prefix);
		if (namespace == null) {
			throw new MalformedQueryException("QName '" + qname + "' uses an undefined namespace prefix");
		}

		return _valueFactory.createURI(namespace + localName);
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
		else if (dtNode instanceof ASTQName) {
			datatype = _buildQName((ASTQName)dtNode);
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
