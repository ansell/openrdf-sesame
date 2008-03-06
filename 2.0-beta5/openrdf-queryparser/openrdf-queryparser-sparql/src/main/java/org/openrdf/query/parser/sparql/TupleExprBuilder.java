/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.BNodeGenerator;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.DatatypeFunc;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.IsBNode;
import org.openrdf.query.algebra.IsLiteral;
import org.openrdf.query.algebra.IsURI;
import org.openrdf.query.algebra.LangFunc;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Null;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.RowSelection;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.StrFunc;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.parser.sparql.ast.ASTAnd;
import org.openrdf.query.parser.sparql.ast.ASTAskQuery;
import org.openrdf.query.parser.sparql.ast.ASTBlankNode;
import org.openrdf.query.parser.sparql.ast.ASTBlankNodePropertyList;
import org.openrdf.query.parser.sparql.ast.ASTBound;
import org.openrdf.query.parser.sparql.ast.ASTCollection;
import org.openrdf.query.parser.sparql.ast.ASTCompare;
import org.openrdf.query.parser.sparql.ast.ASTConstraint;
import org.openrdf.query.parser.sparql.ast.ASTConstruct;
import org.openrdf.query.parser.sparql.ast.ASTConstructQuery;
import org.openrdf.query.parser.sparql.ast.ASTDatasetClause;
import org.openrdf.query.parser.sparql.ast.ASTDatatype;
import org.openrdf.query.parser.sparql.ast.ASTDescribe;
import org.openrdf.query.parser.sparql.ast.ASTDescribeQuery;
import org.openrdf.query.parser.sparql.ast.ASTFalse;
import org.openrdf.query.parser.sparql.ast.ASTFunctionCall;
import org.openrdf.query.parser.sparql.ast.ASTGraphGraphPattern;
import org.openrdf.query.parser.sparql.ast.ASTIRI;
import org.openrdf.query.parser.sparql.ast.ASTIsBlank;
import org.openrdf.query.parser.sparql.ast.ASTIsIRI;
import org.openrdf.query.parser.sparql.ast.ASTIsLiteral;
import org.openrdf.query.parser.sparql.ast.ASTLang;
import org.openrdf.query.parser.sparql.ast.ASTLangMatches;
import org.openrdf.query.parser.sparql.ast.ASTLimit;
import org.openrdf.query.parser.sparql.ast.ASTMath;
import org.openrdf.query.parser.sparql.ast.ASTNot;
import org.openrdf.query.parser.sparql.ast.ASTNumericLiteral;
import org.openrdf.query.parser.sparql.ast.ASTObjectList;
import org.openrdf.query.parser.sparql.ast.ASTOffset;
import org.openrdf.query.parser.sparql.ast.ASTOptionalGraphPattern;
import org.openrdf.query.parser.sparql.ast.ASTOr;
import org.openrdf.query.parser.sparql.ast.ASTOrderClause;
import org.openrdf.query.parser.sparql.ast.ASTOrderCondition;
import org.openrdf.query.parser.sparql.ast.ASTPropertyList;
import org.openrdf.query.parser.sparql.ast.ASTQName;
import org.openrdf.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.query.parser.sparql.ast.ASTRDFLiteral;
import org.openrdf.query.parser.sparql.ast.ASTRegexExpression;
import org.openrdf.query.parser.sparql.ast.ASTSelect;
import org.openrdf.query.parser.sparql.ast.ASTSelectQuery;
import org.openrdf.query.parser.sparql.ast.ASTStr;
import org.openrdf.query.parser.sparql.ast.ASTString;
import org.openrdf.query.parser.sparql.ast.ASTTrue;
import org.openrdf.query.parser.sparql.ast.ASTUnionGraphPattern;
import org.openrdf.query.parser.sparql.ast.ASTVar;
import org.openrdf.query.parser.sparql.ast.VisitorException;

/**
 * @author arjohn
 */
class TupleExprBuilder extends ASTVisitorBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueFactory _valueFactory;

	private GraphPattern _graphPattern;

	private int _constantVarID = 1;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public TupleExprBuilder(ValueFactory valueFactory) {
		_valueFactory = valueFactory;
	}

	/*---------*
	 * Methods *
	 *---------*/

	private Var _valueExpr2Var(ValueExpr valueExpr) {
		if (valueExpr instanceof Var) {
			return (Var)valueExpr;
		}
		else if (valueExpr instanceof ValueConstant) {
			return _createConstVar(((ValueConstant)valueExpr).getValue());
		}
		else if (valueExpr == null) {
			throw new IllegalArgumentException("valueExpr is null");
		}
		else {
			throw new IllegalArgumentException("valueExpr is a: " + valueExpr.getClass());
		}
	}

	private Var _createConstVar(Value value) {
		Var var = _createAnonVar("-const-" + _constantVarID++);
		var.setValue(value);
		return var;
	}

	private Var _createAnonVar(String varName) {
		Var var = new Var(varName);
		var.setAnonymous(true);
		return var;
	}

	public TupleExpr visit(ASTQueryContainer node, Object data)
		throws VisitorException
	{
		// Skip the prolog, any information it contains should already have been
		// processed
		return (TupleExpr)node.getQuery().jjtAccept(this, null);
	}

	public TupleExpr visit(ASTSelectQuery node, Object data)
		throws VisitorException
	{
		TupleExpr tupleExpr = null;

		_graphPattern = new GraphPattern();

		// Skip the select clause for now
		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, null);
		}

		// Process select clause
		tupleExpr = (TupleExpr)node.jjtGetChild(0).jjtAccept(this, null);

		// process limit and offset clauses
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
			tupleExpr = new RowSelection(tupleExpr, offset, limit);
		}

		return tupleExpr;
	}

	public TupleExpr visit(ASTSelect node, Object data)
		throws VisitorException
	{
		TupleExpr bodyExpr = _graphPattern.buildTupleExpr();

		Projection proj = new Projection(bodyExpr);

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			Var projVar = (Var)node.jjtGetChild(i).jjtAccept(this, null);
			proj.add(new ProjectionElem(projVar.getName()));
		}

		if (node.isDistinct()) {
			return new Distinct(proj);
		}
		else {
			return proj;
		}
	}

	public TupleExpr visit(ASTConstructQuery node, Object data)
		throws VisitorException
	{
		TupleExpr tupleExpr = null;

		_graphPattern = new GraphPattern();

		// Skip the construct clause for now
		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, null);
		}

		// Process construct clause
		tupleExpr = (TupleExpr)node.jjtGetChild(0).jjtAccept(this, null);

		// process limit and offset clauses
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
			tupleExpr = new RowSelection(tupleExpr, offset, limit);
		}

		return tupleExpr;
	}

	public TupleExpr visit(ASTConstruct node, Object data)
		throws VisitorException
	{
		TupleExpr bodyExpr = _graphPattern.buildTupleExpr();

		// Collect construct triples
		_graphPattern = new GraphPattern();
		super.visit(node, null);
		TupleExpr constructExpr = _graphPattern.buildTupleExpr();

		// Retrieve all StatementPattern's from the construct expression
		StatementPatternCollector spCollector = new StatementPatternCollector();
		constructExpr.visit(spCollector);

		TupleExpr tupleExpr = bodyExpr;

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

	public TupleExpr visit(ASTDescribeQuery node, Object data)
		throws VisitorException
	{
		_graphPattern = new GraphPattern();

		// Skip the describe clause for now
		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, null);
		}

		// Process describe clause last
		return (TupleExpr)node.jjtGetChild(0).jjtAccept(this, null);
	}

	public TupleExpr visit(ASTDescribe node, Object data)
		throws VisitorException
	{
		// FIXME: implement
		return _graphPattern.buildTupleExpr();
	}

	public TupleExpr visit(ASTAskQuery node, Object data)
		throws VisitorException
	{
		_graphPattern = new GraphPattern();
		super.visit(node, null);
		// FIXME: implement
		return _graphPattern.buildTupleExpr();
	}

	public Object visit(ASTDatasetClause node, Object data)
		throws VisitorException
	{
		// ignore contexts clauses
		return data;
	}

	public Object visit(ASTOrderClause node, Object data)
		throws VisitorException
	{
		// FIXME: implement
		throw new VisitorException("Query result ordering is not yet supported.");
		// return super.visit(node, data);
	}

	public Object visit(ASTOrderCondition node, Object data)
		throws VisitorException
	{
		// FIXME: implement
		return super.visit(node, data);
	}

	public Integer visit(ASTLimit node, Object data)
		throws VisitorException
	{
		return node.getValue();
	}

	public Integer visit(ASTOffset node, Object data)
		throws VisitorException
	{
		return node.getValue();
	}

	public Object visit(ASTOptionalGraphPattern node, Object data)
		throws VisitorException
	{
		GraphPattern parentGP = _graphPattern;
		_graphPattern = new GraphPattern(parentGP);

		super.visit(node, null);

		parentGP.addOptionalTE(_graphPattern.buildTupleExpr());
		_graphPattern = parentGP;

		return null;
	}

	public Object visit(ASTGraphGraphPattern node, Object data)
		throws VisitorException
	{
		Var oldContext = _graphPattern.getContextVar();
		Scope oldScope = _graphPattern.getStatementPatternScope();

		ValueExpr newContext = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);

		_graphPattern.setContextVar(_valueExpr2Var(newContext));
		_graphPattern.setStatementPatternScope(Scope.NAMED_CONTEXTS);

		node.jjtGetChild(1).jjtAccept(this, null);

		_graphPattern.setContextVar(oldContext);
		_graphPattern.setStatementPatternScope(oldScope);

		return null;
	}

	public Object visit(ASTUnionGraphPattern node, Object data)
		throws VisitorException
	{
		GraphPattern parentGP = _graphPattern;

		_graphPattern = new GraphPattern(parentGP);
		node.jjtGetChild(0).jjtAccept(this, null);
		TupleExpr leftArg = _graphPattern.buildTupleExpr();

		_graphPattern = new GraphPattern(parentGP);
		node.jjtGetChild(1).jjtAccept(this, null);
		TupleExpr rightArg = _graphPattern.buildTupleExpr();

		parentGP.addRequiredTE(new Union(leftArg, rightArg));
		_graphPattern = parentGP;

		return null;
	}

	public Object visit(ASTPropertyList propListNode, Object data)
		throws VisitorException
	{
		ValueExpr subject = (ValueExpr)data;
		ValueExpr predicate = (ValueExpr)propListNode.getVerb().jjtAccept(this, null);
		@SuppressWarnings("unchecked")
		List<ValueExpr> objectList = (List<ValueExpr>)propListNode.getObjectList().jjtAccept(this, null);

		Var subjVar = _valueExpr2Var(subject);
		Var predVar = _valueExpr2Var(predicate);

		for (ValueExpr object : objectList) {
			Var objVar = _valueExpr2Var(object);
			_graphPattern.addRequiredSP(subjVar, predVar, objVar);
		}

		ASTPropertyList nextPropList = propListNode.getNextPropertyList();
		if (nextPropList != null) {
			nextPropList.jjtAccept(this, subject);
		}

		return null;
	}

	public List<ValueExpr> visit(ASTObjectList node, Object data)
		throws VisitorException
	{
		int childCount = node.jjtGetNumChildren();
		List<ValueExpr> result = new ArrayList<ValueExpr>(childCount);

		for (int i = 0; i < childCount; i++) {
			result.add((ValueExpr)node.jjtGetChild(i).jjtAccept(this, null));
		}

		return result;
	}

	public Var visit(ASTBlankNodePropertyList node, Object data)
		throws VisitorException
	{
		Var bnodeVar = _createAnonVar(node.getVarName());
		super.visit(node, bnodeVar);
		return bnodeVar;
	}

	public Var visit(ASTCollection node, Object data)
		throws VisitorException
	{
		String listVarName = node.getVarName();

		Var listVar = _createAnonVar(listVarName);
		Var rdfFirstVar = _createConstVar(RDF.FIRST);
		Var rdfRestVar = _createConstVar(RDF.REST);
		Var rdfNilVar = _createConstVar(RDF.NIL);

		int childCount = node.jjtGetNumChildren();
		for (int i = 0; i < childCount; i++) {
			ValueExpr childValue = (ValueExpr)node.jjtGetChild(i).jjtAccept(this, null);

			Var childVar = _valueExpr2Var(childValue);
			_graphPattern.addRequiredSP(listVar, rdfFirstVar, childVar);

			Var nextListVar;
			if (i == childCount - 1) {
				nextListVar = rdfNilVar;
			}
			else {
				nextListVar = _createAnonVar(listVarName + "-" + (i + 1));
			}
			_graphPattern.addRequiredSP(listVar, rdfRestVar, nextListVar);
			listVar = nextListVar;
		}

		return listVar;
	}

	public Object visit(ASTConstraint node, Object data)
		throws VisitorException
	{
		ValueExpr valueExpr = (ValueExpr)super.visit(node, null);
		_graphPattern.addConstraint(valueExpr);

		return null;
	}

	public Or visit(ASTOr node, Object data)
		throws VisitorException
	{
		ValueExpr leftArg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		ValueExpr rightArg = (ValueExpr)node.jjtGetChild(1).jjtAccept(this, null);
		return new Or(leftArg, rightArg);
	}

	public Object visit(ASTAnd node, Object data)
		throws VisitorException
	{
		ValueExpr leftArg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		ValueExpr rightArg = (ValueExpr)node.jjtGetChild(1).jjtAccept(this, null);
		return new And(leftArg, rightArg);
	}

	public Not visit(ASTNot node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)super.visit(node, null);
		return new Not(arg);
	}

	public Compare visit(ASTCompare node, Object data)
		throws VisitorException
	{
		ValueExpr leftArg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		ValueExpr rightArg = (ValueExpr)node.jjtGetChild(1).jjtAccept(this, null);
		return new Compare(leftArg, rightArg, node.getOperator());
	}

	public MathExpr visit(ASTMath node, Object data)
		throws VisitorException
	{
		ValueExpr leftArg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		ValueExpr rightArg = (ValueExpr)node.jjtGetChild(1).jjtAccept(this, null);
		return new MathExpr(leftArg, rightArg, node.getOperator());
	}

	public Object visit(ASTFunctionCall node, Object data)
		throws VisitorException
	{
		// FIXME: implement
		throw new VisitorException("Function calls are not yet supported");
	}

	public Object visit(ASTStr node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new StrFunc(arg);
	}

	public LangFunc visit(ASTLang node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new LangFunc(arg);
	}

	public DatatypeFunc visit(ASTDatatype node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new DatatypeFunc(arg);
	}

	public Object visit(ASTLangMatches node, Object data)
		throws VisitorException
	{
		// FIXME: implement
		throw new VisitorException("LangMatches functions are not yet supported");
	}

	public ValueExpr visit(ASTBound node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new Compare(arg, new Null(), CompareOp.NE);
	}

	public IsURI visit(ASTIsIRI node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new IsURI(arg);
	}

	public IsBNode visit(ASTIsBlank node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new IsBNode(arg);
	}

	public IsLiteral visit(ASTIsLiteral node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new IsLiteral(arg);
	}

	public Object visit(ASTRegexExpression node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		ValueConstant pattern = (ValueConstant)node.jjtGetChild(1).jjtAccept(this, null);
		String flags = "";
		if (node.jjtGetNumChildren() > 2) {
			ValueConstant vc = (ValueConstant)node.jjtGetChild(2).jjtAccept(this, null);
			flags = ((Literal)vc.getValue()).getLabel();
		}
		Literal p = (Literal)pattern.getValue();
		return new Regex(arg, p.getLabel(), flags);
	}

	public Var visit(ASTVar node, Object data)
		throws VisitorException
	{
		Var var = new Var(node.getName());
		var.setAnonymous(node.isAnonymous());
		return var;
	}

	public ValueConstant visit(ASTIRI node, Object data)
		throws VisitorException
	{
		return new ValueConstant(_valueFactory.createURI(node.getValue()));
	}

	public Object visit(ASTQName node, Object data)
		throws VisitorException
	{
		throw new VisitorException("QNames must be resolved before building the query model");
	}

	public Object visit(ASTBlankNode node, Object data)
		throws VisitorException
	{
		throw new VisitorException(
				"Blank nodes must be replaced with variables before building the query model");
	}

	public ValueConstant visit(ASTRDFLiteral node, Object data)
		throws VisitorException
	{
		String label = (String)node.getLabel().jjtAccept(this, null);
		String lang = node.getLang();
		ASTIRI datatypeNode = node.getDatatype();

		Literal literal;
		if (datatypeNode != null) {
			URI datatype = _valueFactory.createURI(datatypeNode.getValue());
			literal = _valueFactory.createLiteral(label, datatype);
		}
		else if (lang != null) {
			literal = _valueFactory.createLiteral(label, lang);
		}
		else {
			literal = _valueFactory.createLiteral(label);
		}

		return new ValueConstant(literal);
	}

	public ValueConstant visit(ASTNumericLiteral node, Object data)
		throws VisitorException
	{
		Literal literal = _valueFactory.createLiteral(node.getValue(), node.getDatatype());
		return new ValueConstant(literal);
	}

	public ValueConstant visit(ASTTrue node, Object data)
		throws VisitorException
	{
		return new ValueConstant(_valueFactory.createLiteral(true));
	}

	public ValueConstant visit(ASTFalse node, Object data)
		throws VisitorException
	{
		return new ValueConstant(_valueFactory.createLiteral(false));
	}

	public String visit(ASTString node, Object data)
		throws VisitorException
	{
		return node.getValue();
	}
}
