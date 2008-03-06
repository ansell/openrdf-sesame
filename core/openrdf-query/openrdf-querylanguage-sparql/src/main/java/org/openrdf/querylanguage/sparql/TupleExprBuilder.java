/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.sparql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.querylanguage.sparql.ast.ASTAnd;
import org.openrdf.querylanguage.sparql.ast.ASTAskQuery;
import org.openrdf.querylanguage.sparql.ast.ASTBlankNode;
import org.openrdf.querylanguage.sparql.ast.ASTBlankNodePropertyList;
import org.openrdf.querylanguage.sparql.ast.ASTBound;
import org.openrdf.querylanguage.sparql.ast.ASTCollection;
import org.openrdf.querylanguage.sparql.ast.ASTCompare;
import org.openrdf.querylanguage.sparql.ast.ASTConstraint;
import org.openrdf.querylanguage.sparql.ast.ASTConstruct;
import org.openrdf.querylanguage.sparql.ast.ASTConstructQuery;
import org.openrdf.querylanguage.sparql.ast.ASTDatasetClause;
import org.openrdf.querylanguage.sparql.ast.ASTDatatype;
import org.openrdf.querylanguage.sparql.ast.ASTDescribe;
import org.openrdf.querylanguage.sparql.ast.ASTDescribeQuery;
import org.openrdf.querylanguage.sparql.ast.ASTFalse;
import org.openrdf.querylanguage.sparql.ast.ASTFunctionCall;
import org.openrdf.querylanguage.sparql.ast.ASTGraphGraphPattern;
import org.openrdf.querylanguage.sparql.ast.ASTIRI;
import org.openrdf.querylanguage.sparql.ast.ASTIsBlank;
import org.openrdf.querylanguage.sparql.ast.ASTIsIRI;
import org.openrdf.querylanguage.sparql.ast.ASTIsLiteral;
import org.openrdf.querylanguage.sparql.ast.ASTLang;
import org.openrdf.querylanguage.sparql.ast.ASTLangMatches;
import org.openrdf.querylanguage.sparql.ast.ASTLimit;
import org.openrdf.querylanguage.sparql.ast.ASTMath;
import org.openrdf.querylanguage.sparql.ast.ASTNot;
import org.openrdf.querylanguage.sparql.ast.ASTNumericLiteral;
import org.openrdf.querylanguage.sparql.ast.ASTObjectList;
import org.openrdf.querylanguage.sparql.ast.ASTOffset;
import org.openrdf.querylanguage.sparql.ast.ASTOptionalGraphPattern;
import org.openrdf.querylanguage.sparql.ast.ASTOr;
import org.openrdf.querylanguage.sparql.ast.ASTOrderClause;
import org.openrdf.querylanguage.sparql.ast.ASTOrderCondition;
import org.openrdf.querylanguage.sparql.ast.ASTPropertyList;
import org.openrdf.querylanguage.sparql.ast.ASTQName;
import org.openrdf.querylanguage.sparql.ast.ASTQueryContainer;
import org.openrdf.querylanguage.sparql.ast.ASTRDFLiteral;
import org.openrdf.querylanguage.sparql.ast.ASTRegexExpression;
import org.openrdf.querylanguage.sparql.ast.ASTSelect;
import org.openrdf.querylanguage.sparql.ast.ASTSelectQuery;
import org.openrdf.querylanguage.sparql.ast.ASTStr;
import org.openrdf.querylanguage.sparql.ast.ASTString;
import org.openrdf.querylanguage.sparql.ast.ASTTrue;
import org.openrdf.querylanguage.sparql.ast.ASTUnionGraphPattern;
import org.openrdf.querylanguage.sparql.ast.ASTVar;
import org.openrdf.querylanguage.sparql.ast.VisitorException;
import org.openrdf.querymodel.And;
import org.openrdf.querymodel.BNodeGenerator;
import org.openrdf.querymodel.BooleanConstant;
import org.openrdf.querymodel.BooleanExpr;
import org.openrdf.querymodel.Compare;
import org.openrdf.querymodel.Datatype;
import org.openrdf.querymodel.Distinct;
import org.openrdf.querymodel.EffectiveBooleanValue;
import org.openrdf.querymodel.EmptySet;
import org.openrdf.querymodel.Extension;
import org.openrdf.querymodel.ExtensionElem;
import org.openrdf.querymodel.IsBNode;
import org.openrdf.querymodel.IsLiteral;
import org.openrdf.querymodel.IsURI;
import org.openrdf.querymodel.Lang;
import org.openrdf.querymodel.MathExpr;
import org.openrdf.querymodel.MultiProjection;
import org.openrdf.querymodel.Not;
import org.openrdf.querymodel.Null;
import org.openrdf.querymodel.Or;
import org.openrdf.querymodel.Projection;
import org.openrdf.querymodel.ProjectionElem;
import org.openrdf.querymodel.StatementPattern;
import org.openrdf.querymodel.TupleExpr;
import org.openrdf.querymodel.Union;
import org.openrdf.querymodel.ValueConstant;
import org.openrdf.querymodel.ValueExpr;
import org.openrdf.querymodel.Var;
import org.openrdf.querymodel.StatementPattern.Scope;

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

	private BooleanExpr _valueExpr2BooleanExpr(ValueExpr valueExpr) {
		if (valueExpr instanceof BooleanExpr) {
			return (BooleanExpr)valueExpr;
		}
		else {
			return new EffectiveBooleanValue(valueExpr);
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
		_graphPattern = new GraphPattern();

		// Skip the select clause for now
		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, null);
		}

		// Process select clause last
		return (TupleExpr)node.jjtGetChild(0).jjtAccept(this, null);
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
		_graphPattern = new GraphPattern();

		// Skip the construct clause for now
		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, null);
		}

		// Process construct clause last
		return (TupleExpr)node.jjtGetChild(0).jjtAccept(this, null);
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
		// ignore dataset clauses
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

	public Object visit(ASTLimit node, Object data)
		throws VisitorException
	{
		_graphPattern.setLimit(node.getValue());
		return null;
	}

	public Object visit(ASTOffset node, Object data)
		throws VisitorException
	{
		_graphPattern.setOffset(node.getValue());
		return null;
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
		BooleanExpr booleanExpr = _valueExpr2BooleanExpr(valueExpr);
		_graphPattern.addConstraint(booleanExpr);

		return null;
	}

	public Or visit(ASTOr node, Object data)
		throws VisitorException
	{
		ValueExpr leftArg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		ValueExpr rightArg = (ValueExpr)node.jjtGetChild(1).jjtAccept(this, null);
		return new Or(_valueExpr2BooleanExpr(leftArg), _valueExpr2BooleanExpr(rightArg));
	}

	public Object visit(ASTAnd node, Object data)
		throws VisitorException
	{
		ValueExpr leftArg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		ValueExpr rightArg = (ValueExpr)node.jjtGetChild(1).jjtAccept(this, null);
		return new And(_valueExpr2BooleanExpr(leftArg), _valueExpr2BooleanExpr(rightArg));
	}

	public Not visit(ASTNot node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)super.visit(node, null);
		return new Not(_valueExpr2BooleanExpr(arg));
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
		// FIXME: implement
		throw new VisitorException("Str functions are not yet supported");
	}

	public Lang visit(ASTLang node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new Lang(arg);
	}

	public Datatype visit(ASTDatatype node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new Datatype(arg);
	}

	public Object visit(ASTLangMatches node, Object data)
		throws VisitorException
	{
		// FIXME: implement
		throw new VisitorException("LangMatches functions are not yet supported");
	}

	public BooleanExpr visit(ASTBound node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new Compare(arg, new Null(), Compare.Operator.NE);
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
		// FIXME: implement
		throw new VisitorException("RegEx functions are not yet supported");
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

	public BooleanConstant visit(ASTTrue node, Object data)
		throws VisitorException
	{
		return BooleanConstant.TRUE;
	}

	public BooleanConstant visit(ASTFalse node, Object data)
		throws VisitorException
	{
		return BooleanConstant.FALSE;
	}

	public String visit(ASTString node, Object data)
		throws VisitorException
	{
		return node.getValue();
	}
}
