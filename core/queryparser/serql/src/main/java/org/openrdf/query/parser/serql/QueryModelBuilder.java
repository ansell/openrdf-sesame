/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.CompareAll;
import org.openrdf.query.algebra.CompareAny;
import org.openrdf.query.algebra.Datatype;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.In;
import org.openrdf.query.algebra.Intersection;
import org.openrdf.query.algebra.IsBNode;
import org.openrdf.query.algebra.IsLiteral;
import org.openrdf.query.algebra.IsResource;
import org.openrdf.query.algebra.IsURI;
import org.openrdf.query.algebra.Label;
import org.openrdf.query.algebra.Lang;
import org.openrdf.query.algebra.LocalName;
import org.openrdf.query.algebra.Namespace;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.Slice;
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
import org.openrdf.query.parser.serql.ast.ASTBound;
import org.openrdf.query.parser.serql.ast.ASTCompare;
import org.openrdf.query.parser.serql.ast.ASTCompareAll;
import org.openrdf.query.parser.serql.ast.ASTCompareAny;
import org.openrdf.query.parser.serql.ast.ASTConstruct;
import org.openrdf.query.parser.serql.ast.ASTConstructQuery;
import org.openrdf.query.parser.serql.ast.ASTDatatype;
import org.openrdf.query.parser.serql.ast.ASTEdge;
import org.openrdf.query.parser.serql.ast.ASTExists;
import org.openrdf.query.parser.serql.ast.ASTFrom;
import org.openrdf.query.parser.serql.ast.ASTFunctionCall;
import org.openrdf.query.parser.serql.ast.ASTGraphIntersect;
import org.openrdf.query.parser.serql.ast.ASTGraphMinus;
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
import org.openrdf.query.parser.serql.ast.ASTQueryBody;
import org.openrdf.query.parser.serql.ast.ASTQueryContainer;
import org.openrdf.query.parser.serql.ast.ASTReifiedStat;
import org.openrdf.query.parser.serql.ast.ASTSelect;
import org.openrdf.query.parser.serql.ast.ASTSelectQuery;
import org.openrdf.query.parser.serql.ast.ASTString;
import org.openrdf.query.parser.serql.ast.ASTTupleIntersect;
import org.openrdf.query.parser.serql.ast.ASTTupleMinus;
import org.openrdf.query.parser.serql.ast.ASTTupleUnion;
import org.openrdf.query.parser.serql.ast.ASTURI;
import org.openrdf.query.parser.serql.ast.ASTValueExpr;
import org.openrdf.query.parser.serql.ast.ASTVar;
import org.openrdf.query.parser.serql.ast.ASTWhere;
import org.openrdf.query.parser.serql.ast.VisitorException;

class QueryModelBuilder extends ASTVisitorBase {

	public static TupleExpr buildQueryModel(ASTQueryContainer node, ValueFactory valueFactory)
		throws MalformedQueryException
	{
		try {
			QueryModelBuilder qmBuilder = new QueryModelBuilder(valueFactory);
			return (TupleExpr)node.jjtAccept(qmBuilder, null);
		}
		catch (VisitorException e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueFactory valueFactory;

	private int constantVarID = 1;

	private GraphPattern graphPattern;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public QueryModelBuilder(ValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

	/*---------*
	 * Methods *
	 *---------*/

	private Var createConstantVar(Value value) {
		Var var = new Var("-const-" + constantVarID++);
		var.setAnonymous(true);
		var.setValue(value);
		return var;
	}

	@Override
	public TupleExpr visit(ASTQueryContainer node, Object data)
		throws VisitorException
	{
		// Skip the namespace declarations, any information it contains should
		// already have been processed
		return (TupleExpr)node.getQuery().jjtAccept(this, null);
	}

	@Override
	public TupleExpr visit(ASTTupleUnion node, Object data)
		throws VisitorException
	{
		TupleExpr leftArg = (TupleExpr)node.getLeftArg().jjtAccept(this, null);
		TupleExpr rightArg = (TupleExpr)node.getRightArg().jjtAccept(this, null);

		TupleExpr result = new Union(leftArg, rightArg);

		if (node.isDistinct()) {
			result = new Distinct(result);
		}

		return result;
	}

	@Override
	public TupleExpr visit(ASTTupleMinus node, Object data)
		throws VisitorException
	{
		TupleExpr leftArg = (TupleExpr)node.getLeftArg().jjtAccept(this, null);
		TupleExpr rightArg = (TupleExpr)node.getRightArg().jjtAccept(this, null);

		return new Difference(leftArg, rightArg);
	}

	@Override
	public TupleExpr visit(ASTTupleIntersect node, Object data)
		throws VisitorException
	{
		TupleExpr leftArg = (TupleExpr)node.getLeftArg().jjtAccept(this, null);
		TupleExpr rightArg = (TupleExpr)node.getRightArg().jjtAccept(this, null);

		return new Intersection(leftArg, rightArg);
	}

	@Override
	public TupleExpr visit(ASTGraphUnion node, Object data)
		throws VisitorException
	{
		TupleExpr leftArg = (TupleExpr)node.getLeftArg().jjtAccept(this, null);
		TupleExpr rightArg = (TupleExpr)node.getRightArg().jjtAccept(this, null);

		TupleExpr result = new Union(leftArg, rightArg);

		if (node.isDistinct()) {
			result = new Distinct(result);
		}

		return result;
	}

	@Override
	public TupleExpr visit(ASTGraphMinus node, Object data)
		throws VisitorException
	{
		TupleExpr leftArg = (TupleExpr)node.getLeftArg().jjtAccept(this, null);
		TupleExpr rightArg = (TupleExpr)node.getRightArg().jjtAccept(this, null);

		return new Difference(leftArg, rightArg);
	}

	@Override
	public TupleExpr visit(ASTGraphIntersect node, Object data)
		throws VisitorException
	{
		TupleExpr leftArg = (TupleExpr)node.getLeftArg().jjtAccept(this, null);
		TupleExpr rightArg = (TupleExpr)node.getRightArg().jjtAccept(this, null);

		return new Intersection(leftArg, rightArg);
	}

	@Override
	public TupleExpr visit(ASTSelectQuery node, Object data)
		throws VisitorException
	{
		TupleExpr tupleExpr;

		ASTQueryBody queryBodyNode = node.getQueryBody();

		if (queryBodyNode != null) {
			// Build tuple expression for query body
			tupleExpr = (TupleExpr)queryBodyNode.jjtAccept(this, null);
		}
		else {
			tupleExpr = new SingletonSet();
		}

		// Apply projection
		tupleExpr = (TupleExpr)node.getSelectClause().jjtAccept(this, tupleExpr);

		// process limit and offset clauses, if present.
		ASTLimit limitNode = node.getLimit();
		int limit = -1;
		if (limitNode != null) {
			limit = (Integer)limitNode.jjtAccept(this, null);
		}

		ASTOffset offsetNode = node.getOffset();
		int offset = -1;
		if (offsetNode != null) {
			offset = (Integer)offsetNode.jjtAccept(this, null);
		}

		if (offset >= 1 || limit >= 0) {
			tupleExpr = new Slice(tupleExpr, offset, limit);
		}
		return tupleExpr;
	}

	@Override
	public TupleExpr visit(ASTSelect node, Object data)
		throws VisitorException
	{
		TupleExpr result = (TupleExpr)data;

		Extension extension = new Extension();
		ProjectionElemList projElemList = new ProjectionElemList();

		for (ASTProjectionElem projElemNode : node.getProjectionElemList()) {
			ValueExpr valueExpr = (ValueExpr)projElemNode.getValueExpr().jjtAccept(this, null);

			String alias = projElemNode.getAlias();
			if (alias != null) {
				// aliased projection element
				extension.addElement(new ExtensionElem(valueExpr, alias));
				projElemList.addElement(new ProjectionElem(alias));
			}
			else if (valueExpr instanceof Var) {
				// unaliased variable
				Var projVar = (Var)valueExpr;
				projElemList.addElement(new ProjectionElem(projVar.getName()));
			}
			else {
				throw new IllegalStateException("required alias for non-Var projection elements not found");
			}
		}

		if (!extension.getElements().isEmpty()) {
			extension.setArg(result);
			result = extension;
		}

		result = new Projection(result, projElemList);

		if (node.isDistinct()) {
			result = new Distinct(result);
		}

		return result;
	}

	@Override
	public TupleExpr visit(ASTConstructQuery node, Object data)
		throws VisitorException
	{
		TupleExpr tupleExpr;

		if (node.hasQueryBody()) {
			// Build tuple expression for query body
			tupleExpr = (TupleExpr)node.getQueryBody().jjtAccept(this, null);
		}
		else {
			tupleExpr = new SingletonSet();
		}

		// Create constructor
		ConstructorBuilder cb = new ConstructorBuilder();
		ASTConstruct constructNode = node.getConstructClause();

		if (!constructNode.isWildcard()) {
			TupleExpr constructExpr = (TupleExpr)constructNode.jjtAccept(this, null);
			tupleExpr = cb.buildConstructor(tupleExpr, constructExpr, constructNode.isDistinct());
		}
		else if (node.hasQueryBody()) {
			tupleExpr = cb.buildConstructor(tupleExpr, constructNode.isDistinct());
		}
		// else: "construct *" without query body, just return the SingletonSet

		// process limit and offset clauses, if present.
		ASTLimit limitNode = node.getLimit();
		int limit = -1;
		if (limitNode != null) {
			limit = (Integer)limitNode.jjtAccept(this, null);
		}

		ASTOffset offsetNode = node.getOffset();
		int offset = -1;

		if (offsetNode != null) {
			offset = (Integer)offsetNode.jjtAccept(this, null);
		}

		if (offset >= 1 || limit >= 0) {
			tupleExpr = new Slice(tupleExpr, offset, limit);
		}

		return tupleExpr;
	}

	@Override
	public TupleExpr visit(ASTConstruct node, Object data)
		throws VisitorException
	{
		assert !node.isWildcard() : "Cannot build constructor for wildcards";

		graphPattern = new GraphPattern(graphPattern);
		try {
			super.visit(node, data);
			return graphPattern.buildTupleExpr();
		}
		finally {
			graphPattern = graphPattern.getParent();
		}
	}

	@Override
	public TupleExpr visit(ASTQueryBody node, Object data)
		throws VisitorException
	{
		graphPattern = new GraphPattern(graphPattern);
		try {
			super.visit(node, data);
			return graphPattern.buildTupleExpr();
		}
		finally {
			graphPattern = graphPattern.getParent();
		}
	}

	@Override
	public Object visit(ASTFrom node, Object data)
		throws VisitorException
	{
		StatementPattern.Scope scope = StatementPattern.Scope.DEFAULT_CONTEXTS;
		Var contextVar = null;

		if (node.hasContextID()) {
			scope = StatementPattern.Scope.NAMED_CONTEXTS;
			ValueExpr contextID = (ValueExpr)node.getContextID().jjtAccept(this, null);

			if (contextID instanceof Var) {
				contextVar = (Var)contextID;
			}
			else if (contextID instanceof ValueConstant) {
				ValueConstant vc = (ValueConstant)contextID;
				contextVar = createConstantVar(vc.getValue());
			}
			else {
				throw new IllegalArgumentException("Unexpected contextID result type: " + contextID.getClass());
			}
		}

		graphPattern.setStatementPatternScope(scope);
		graphPattern.setContextVar(contextVar);

		for (ASTPathExpr pathExprNode : node.getPathExprList()) {
			pathExprNode.jjtAccept(this, null);
		}

		return null;
	}

	@Override
	public Integer visit(ASTWhere node, Object data)
		throws VisitorException
	{
		ValueExpr valueExpr = (ValueExpr)node.getCondition().jjtAccept(this, null);
		graphPattern.addConstraint(valueExpr);
		return null;
	}

	@Override
	public Integer visit(ASTLimit node, Object data)
		throws VisitorException
	{
		return node.getValue();
	}

	@Override
	public Integer visit(ASTOffset node, Object data)
		throws VisitorException
	{
		return node.getValue();
	}

	@Override
	public Object visit(ASTBasicPathExpr node, Object data)
		throws VisitorException
	{
		// process subject node
		List<Var> subjVars = (List<Var>)node.getHead().jjtAccept(this, null);

		// supply subject vars to tail segment
		node.getTail().jjtAccept(this, subjVars);

		return null;
	}

	@Override
	public Object visit(ASTOptPathExpr node, Object data)
		throws VisitorException
	{
		// Create new sub-graph pattern for optional path expressions
		graphPattern = new GraphPattern(graphPattern);

		super.visit(node, data);

		graphPattern.getParent().addOptionalTE(graphPattern);
		graphPattern = graphPattern.getParent();

		return null;
	}

	@Override
	public Object visit(ASTBasicPathExprTail tailNode, Object data)
		throws VisitorException
	{
		List<Var> subjVars = (List<Var>)data;
		Var predVar = (Var)tailNode.getEdge().jjtAccept(this, null);
		List<Var> objVars = (List<Var>)tailNode.getNode().jjtAccept(this, null);

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
			nextTailNode.jjtAccept(this, joinVars);
		}

		return null;
	}

	@Override
	public Object visit(ASTOptPathExprTail tailNode, Object data)
		throws VisitorException
	{
		List<Var> subjVars = (List<Var>)data;

		// Create new sub-graph pattern for optional path expressions
		graphPattern = new GraphPattern(graphPattern);

		// optional path expression tail
		tailNode.getOptionalTail().jjtAccept(this, subjVars);

		ASTWhere whereNode = tailNode.getWhereClause();
		if (whereNode != null) {
			// boolean contraint on optional path expression tail
			whereNode.jjtAccept(this, null);
		}

		graphPattern.getParent().addOptionalTE(graphPattern);
		graphPattern = graphPattern.getParent();

		ASTPathExprTail nextTailNode = tailNode.getNextTail();
		if (nextTailNode != null) {
			// branch after optional path expression tail
			nextTailNode.jjtAccept(this, subjVars);
		}

		return null;
	}

	@Override
	public Var visit(ASTEdge node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.getValueExpr().jjtAccept(this, null);

		if (arg instanceof Var) {
			return (Var)arg;
		}
		else if (arg instanceof ValueConstant) {
			ValueConstant vc = (ValueConstant)arg;
			return createConstantVar(vc.getValue());
		}
		else {
			throw new IllegalArgumentException("Unexpected edge argument type: " + arg.getClass());
		}
	}

	@Override
	public List<Var> visit(ASTNode node, Object data)
		throws VisitorException
	{
		List<Var> nodeVars = new ArrayList<Var>();

		for (ASTNodeElem nodeElem : node.getNodeElemList()) {
			Var nodeVar = (Var)nodeElem.jjtAccept(this, null);
			nodeVars.add(nodeVar);
		}

		// Create any implicit unequalities
		for (int i = 0; i < nodeVars.size() - 1; i++) {
			Var var1 = nodeVars.get(i);

			for (int j = i + 1; j < nodeVars.size(); j++) {
				Var var2 = nodeVars.get(j);

				// At least one of the variables should be non-constant
				// for the unequality to make any sense:
				if (!var1.hasValue() || !var2.hasValue()) {
					graphPattern.addConstraint(new Not(new SameTerm(var1, var2)));
				}
			}
		}

		return nodeVars;
	}

	@Override
	public Var visit(ASTNodeElem node, Object data)
		throws VisitorException
	{
		ValueExpr valueExpr = (ValueExpr)node.getChild().jjtAccept(this, null);

		if (valueExpr instanceof Var) {
			return (Var)valueExpr;
		}
		else if (valueExpr instanceof ValueConstant) {
			ValueConstant vc = (ValueConstant)valueExpr;
			return createConstantVar(vc.getValue());
		}
		else {
			throw new IllegalArgumentException("Unexpected node element result type: " + valueExpr.getClass());
		}
	}

	@Override
	public Var visit(ASTReifiedStat node, Object data)
		throws VisitorException
	{
		assert node.getID() != null : "ID variable not set";

		Var subjVar = (Var)node.getSubject().jjtAccept(this, null);
		Var predVar = (Var)node.getPredicate().jjtAccept(this, null);
		Var objVar = (Var)node.getObject().jjtAccept(this, null);
		Var idVar = (Var)node.getID().jjtAccept(this, null);

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

	@Override
	public ValueExpr visit(ASTOr node, Object data)
		throws VisitorException
	{
		Or or = new Or();

		for (ASTBooleanExpr expr : node.getOperandList()) {
			ValueExpr arg = (ValueExpr)expr.jjtAccept(this, null);
			or.addArg(arg);
		}

		return or;
	}

	@Override
	public ValueExpr visit(ASTAnd node, Object data)
		throws VisitorException
	{
		And and = new And();

		for (ASTBooleanExpr expr : node.getOperandList()) {
			ValueExpr arg = (ValueExpr)expr.jjtAccept(this, null);
			and.addArg(arg);
		}

		return and;
	}

	@Override
	public ValueConstant visit(ASTBooleanConstant node, Object data)
		throws VisitorException
	{
		return new ValueConstant(valueFactory.createLiteral(node.getValue()));
	}

	@Override
	public Not visit(ASTNot node, Object data)
		throws VisitorException
	{
		return new Not((ValueExpr)super.visit(node, data));
	}

	@Override
	public Bound visit(ASTBound node, Object data)
		throws VisitorException
	{
		return new Bound((Var)super.visit(node, data));
	}

	@Override
	public IsResource visit(ASTIsResource node, Object data)
		throws VisitorException
	{
		return new IsResource((ValueExpr)super.visit(node, data));
	}

	@Override
	public IsLiteral visit(ASTIsLiteral node, Object data)
		throws VisitorException
	{
		return new IsLiteral((ValueExpr)super.visit(node, data));
	}

	@Override
	public IsURI visit(ASTIsURI node, Object data)
		throws VisitorException
	{
		return new IsURI((ValueExpr)super.visit(node, data));
	}

	@Override
	public IsBNode visit(ASTIsBNode node, Object data)
		throws VisitorException
	{
		return new IsBNode((ValueExpr)super.visit(node, data));
	}

	@Override
	public Exists visit(ASTExists node, Object data)
		throws VisitorException
	{
		return new Exists((TupleExpr)super.visit(node, data));
	}

	@Override
	public Compare visit(ASTCompare node, Object data)
		throws VisitorException
	{
		ValueExpr leftArg = (ValueExpr)node.getLeftOperand().jjtAccept(this, null);
		ValueExpr rightArg = (ValueExpr)node.getRightOperand().jjtAccept(this, null);
		CompareOp operator = node.getOperator().getValue();

		return new Compare(leftArg, rightArg, operator);
	}

	@Override
	public CompareAny visit(ASTCompareAny node, Object data)
		throws VisitorException
	{
		ValueExpr valueExpr = (ValueExpr)node.getLeftOperand().jjtAccept(this, null);
		TupleExpr tupleExpr = (TupleExpr)node.getRightOperand().jjtAccept(this, null);
		CompareOp op = node.getOperator().getValue();

		return new CompareAny(valueExpr, tupleExpr, op);
	}

	@Override
	public CompareAll visit(ASTCompareAll node, Object data)
		throws VisitorException
	{
		ValueExpr valueExpr = (ValueExpr)node.getLeftOperand().jjtAccept(this, null);
		TupleExpr tupleExpr = (TupleExpr)node.getRightOperand().jjtAccept(this, null);
		CompareOp op = node.getOperator().getValue();

		return new CompareAll(valueExpr, tupleExpr, op);
	}

	@Override
	public Regex visit(ASTLike node, Object data)
		throws VisitorException
	{
		ValueExpr expr = (ValueExpr)node.getValueExpr().jjtAccept(this, null);
		String pattern = (String)node.getPattern().jjtAccept(this, null);
		boolean caseSensitive = !node.ignoreCase();

		return new Regex(expr, pattern, caseSensitive);
	}

	@Override
	public In visit(ASTIn node, Object data)
		throws VisitorException
	{
		ValueExpr valueExpr = (ValueExpr)node.getLeftOperand().jjtAccept(this, null);
		TupleExpr tupleExpr = (TupleExpr)node.getRightOperand().jjtAccept(this, null);
		return new In(valueExpr, tupleExpr);
	}

	@Override
	public Var visit(ASTVar node, Object data)
		throws VisitorException
	{
		Var var = new Var(node.getName());
		var.setAnonymous(node.isAnonymous());
		return var;
	}

	@Override
	public Datatype visit(ASTDatatype node, Object data)
		throws VisitorException
	{
		return new Datatype((ValueExpr)super.visit(node, data));
	}

	@Override
	public Lang visit(ASTLang node, Object data)
		throws VisitorException
	{
		return new Lang((ValueExpr)super.visit(node, data));
	}

	@Override
	public Label visit(ASTLabel node, Object data)
		throws VisitorException
	{
		return new Label((ValueExpr)super.visit(node, data));
	}

	@Override
	public Namespace visit(ASTNamespace node, Object data)
		throws VisitorException
	{
		return new Namespace((ValueExpr)super.visit(node, data));
	}

	@Override
	public LocalName visit(ASTLocalName node, Object data)
		throws VisitorException
	{
		return new LocalName((ValueExpr)super.visit(node, data));
	}

	@Override
	public FunctionCall visit(ASTFunctionCall node, Object data)
		throws VisitorException
	{
		ValueConstant vc = (ValueConstant)node.getURI().jjtAccept(this, null);
		assert vc.getValue() instanceof URI;

		FunctionCall functionCall = new FunctionCall(vc.getValue().toString());

		for (ASTValueExpr argExpr : node.getArgList()) {
			functionCall.addArg((ValueExpr)argExpr.jjtAccept(this, null));
		}

		return functionCall;
	}

	@Override
	public Object visit(ASTNull node, Object data)
		throws VisitorException
	{
		throw new VisitorException(
				"Use of NULL values in SeRQL queries has been deprecated, use BOUND(...) instead");
	}

	@Override
	public ValueConstant visit(ASTURI node, Object data)
		throws VisitorException
	{
		return new ValueConstant(valueFactory.createURI(node.getValue()));
	}

	@Override
	public ValueConstant visit(ASTBNode node, Object data)
		throws VisitorException
	{
		return new ValueConstant(valueFactory.createBNode(node.getID()));
	}

	@Override
	public ValueConstant visit(ASTLiteral litNode, Object data)
		throws VisitorException
	{
		URI datatype = null;

		// Get datatype URI from child URI node, if present
		ASTValueExpr dtNode = litNode.getDatatypeNode();
		if (dtNode instanceof ASTURI) {
			datatype = valueFactory.createURI(((ASTURI)dtNode).getValue());
		}
		else if (dtNode != null) {
			throw new IllegalArgumentException("Unexpected datatype type: " + dtNode.getClass());
		}

		Literal literal;
		if (datatype != null) {
			literal = valueFactory.createLiteral(litNode.getLabel(), datatype);
		}
		else if (litNode.hasLang()) {
			literal = valueFactory.createLiteral(litNode.getLabel(), litNode.getLang());
		}
		else {
			literal = valueFactory.createLiteral(litNode.getLabel());
		}

		return new ValueConstant(literal);
	}

	@Override
	public String visit(ASTString node, Object data)
		throws VisitorException
	{
		return node.getValue();
	}
}
