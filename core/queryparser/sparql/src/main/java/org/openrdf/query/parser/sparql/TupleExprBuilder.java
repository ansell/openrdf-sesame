/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
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

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.algebra.AggregateOperator;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Avg;
import org.openrdf.query.algebra.BNodeGenerator;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Count;
import org.openrdf.query.algebra.Datatype;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.GroupConcat;
import org.openrdf.query.algebra.GroupElem;
import org.openrdf.query.algebra.IRIFunction;
import org.openrdf.query.algebra.IsBNode;
import org.openrdf.query.algebra.IsLiteral;
import org.openrdf.query.algebra.IsNumeric;
import org.openrdf.query.algebra.IsURI;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Lang;
import org.openrdf.query.algebra.LangMatches;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.Max;
import org.openrdf.query.algebra.Min;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.Sample;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.StrDt;
import org.openrdf.query.algebra.StrLang;
import org.openrdf.query.algebra.Sum;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.query.parser.sparql.ast.ASTAnd;
import org.openrdf.query.parser.sparql.ast.ASTAskQuery;
import org.openrdf.query.parser.sparql.ast.ASTAvg;
import org.openrdf.query.parser.sparql.ast.ASTBNodeFunc;
import org.openrdf.query.parser.sparql.ast.ASTBlankNode;
import org.openrdf.query.parser.sparql.ast.ASTBlankNodePropertyList;
import org.openrdf.query.parser.sparql.ast.ASTBound;
import org.openrdf.query.parser.sparql.ast.ASTCollection;
import org.openrdf.query.parser.sparql.ast.ASTCompare;
import org.openrdf.query.parser.sparql.ast.ASTConstraint;
import org.openrdf.query.parser.sparql.ast.ASTConstruct;
import org.openrdf.query.parser.sparql.ast.ASTConstructQuery;
import org.openrdf.query.parser.sparql.ast.ASTCount;
import org.openrdf.query.parser.sparql.ast.ASTDatatype;
import org.openrdf.query.parser.sparql.ast.ASTDescribe;
import org.openrdf.query.parser.sparql.ast.ASTDescribeQuery;
import org.openrdf.query.parser.sparql.ast.ASTExistsFunc;
import org.openrdf.query.parser.sparql.ast.ASTFalse;
import org.openrdf.query.parser.sparql.ast.ASTFunctionCall;
import org.openrdf.query.parser.sparql.ast.ASTGraphGraphPattern;
import org.openrdf.query.parser.sparql.ast.ASTGraphPatternGroup;
import org.openrdf.query.parser.sparql.ast.ASTGroupClause;
import org.openrdf.query.parser.sparql.ast.ASTGroupConcat;
import org.openrdf.query.parser.sparql.ast.ASTGroupCondition;
import org.openrdf.query.parser.sparql.ast.ASTHavingClause;
import org.openrdf.query.parser.sparql.ast.ASTIRI;
import org.openrdf.query.parser.sparql.ast.ASTIRIFunc;
import org.openrdf.query.parser.sparql.ast.ASTIsBlank;
import org.openrdf.query.parser.sparql.ast.ASTIsIRI;
import org.openrdf.query.parser.sparql.ast.ASTIsLiteral;
import org.openrdf.query.parser.sparql.ast.ASTIsNumeric;
import org.openrdf.query.parser.sparql.ast.ASTLang;
import org.openrdf.query.parser.sparql.ast.ASTLangMatches;
import org.openrdf.query.parser.sparql.ast.ASTLimit;
import org.openrdf.query.parser.sparql.ast.ASTMath;
import org.openrdf.query.parser.sparql.ast.ASTMax;
import org.openrdf.query.parser.sparql.ast.ASTMin;
import org.openrdf.query.parser.sparql.ast.ASTMinusGraphPattern;
import org.openrdf.query.parser.sparql.ast.ASTNot;
import org.openrdf.query.parser.sparql.ast.ASTNotExistsFunc;
import org.openrdf.query.parser.sparql.ast.ASTNumericLiteral;
import org.openrdf.query.parser.sparql.ast.ASTObjectList;
import org.openrdf.query.parser.sparql.ast.ASTOffset;
import org.openrdf.query.parser.sparql.ast.ASTOptionalGraphPattern;
import org.openrdf.query.parser.sparql.ast.ASTOr;
import org.openrdf.query.parser.sparql.ast.ASTOrderClause;
import org.openrdf.query.parser.sparql.ast.ASTOrderCondition;
import org.openrdf.query.parser.sparql.ast.ASTProjectionElem;
import org.openrdf.query.parser.sparql.ast.ASTPropertyList;
import org.openrdf.query.parser.sparql.ast.ASTQName;
import org.openrdf.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.query.parser.sparql.ast.ASTRDFLiteral;
import org.openrdf.query.parser.sparql.ast.ASTRegexExpression;
import org.openrdf.query.parser.sparql.ast.ASTSameTerm;
import org.openrdf.query.parser.sparql.ast.ASTSelect;
import org.openrdf.query.parser.sparql.ast.ASTSelectQuery;
import org.openrdf.query.parser.sparql.ast.ASTStr;
import org.openrdf.query.parser.sparql.ast.ASTStrDt;
import org.openrdf.query.parser.sparql.ast.ASTStrLang;
import org.openrdf.query.parser.sparql.ast.ASTString;
import org.openrdf.query.parser.sparql.ast.ASTSum;
import org.openrdf.query.parser.sparql.ast.ASTTrue;
import org.openrdf.query.parser.sparql.ast.ASTUnionGraphPattern;
import org.openrdf.query.parser.sparql.ast.ASTVar;
import org.openrdf.query.parser.sparql.ast.Node;
import org.openrdf.query.parser.sparql.ast.VisitorException;

/**
 * @author Arjohn Kampman
 */
class TupleExprBuilder extends ASTVisitorBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueFactory valueFactory;

	private GraphPattern graphPattern;

	private int constantVarID = 1;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public TupleExprBuilder(ValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

	/*---------*
	 * Methods *
	 *---------*/

	private Var valueExpr2Var(ValueExpr valueExpr) {
		if (valueExpr instanceof Var) {
			return (Var)valueExpr;
		}
		else if (valueExpr instanceof ValueConstant) {
			return createConstVar(((ValueConstant)valueExpr).getValue());
		}
		else if (valueExpr == null) {
			throw new IllegalArgumentException("valueExpr is null");
		}
		else {
			throw new IllegalArgumentException("valueExpr is a: " + valueExpr.getClass());
		}
	}

	private Var createConstVar(Value value) {
		Var var = createAnonVar("-const-" + constantVarID++);
		var.setValue(value);
		return var;
	}

	private Var createAnonVar(String varName) {
		Var var = new Var(varName);
		var.setAnonymous(true);
		return var;
	}

	@Override
	public TupleExpr visit(ASTQueryContainer node, Object data)
		throws VisitorException
	{
		// Skip the prolog, any information it contains should already have been
		// processed
		return (TupleExpr)node.getQuery().jjtAccept(this, null);
	}

	@Override
	public TupleExpr visit(ASTSelectQuery node, Object data)
		throws VisitorException
	{
		GraphPattern parentGP = graphPattern;

		// Start with building the graph pattern
		graphPattern = new GraphPattern();
		node.getWhereClause().jjtAccept(this, null);
		TupleExpr tupleExpr = graphPattern.buildTupleExpr();

		// Apply result ordering
		ASTOrderClause orderNode = node.getOrderClause();
		if (orderNode != null) {
			List<OrderElem> orderElemements = (List<OrderElem>)orderNode.jjtAccept(this, null);
			tupleExpr = new Order(tupleExpr, orderElemements);
		}

		// Apply grouping
		ASTGroupClause groupNode = node.getGroupClause();
		if (groupNode != null) {
			tupleExpr = new Group(tupleExpr, groupNode.getBindingNames());
		}

		// Apply HAVING group filter condition
		ASTHavingClause havingNode = node.getHavingClause();
		if (havingNode != null) {

			// add implicit group if necessary
			if (!(tupleExpr instanceof Group)) {
				tupleExpr = new Group(tupleExpr);
			}

			// FIXME is the child of a HAVING node always a compare?
			Compare condition = (Compare)havingNode.jjtGetChild(0).jjtAccept(this, tupleExpr);

			// retrieve any aggregate operators from the condition.
			AggregateCollector collector = new AggregateCollector();
			collector.meet(condition);

			// replace operator occurrences with an anonymous var, and alias it to
			// the group
			Extension extension = new Extension();
			for (AggregateOperator operator : collector.getOperators()) {
				Var var = createAnonVar("-const-" + constantVarID++);

				// replace occurrence of the operator in the filter condition with
				// the variable.
				AggregateOperatorReplacer replacer = new AggregateOperatorReplacer(operator, var);
				replacer.meet(condition);

				String alias = var.getName();

				// create an extension linking the operator to the variable name.
				ExtensionElem pe = new ExtensionElem(operator, alias);
				extension.addElement(pe);

				// add the aggregate operator to the group.
				GroupElem ge = new GroupElem(alias, operator);

				// FIXME quite often the aggregate in the HAVING clause will be a
				// duplicate of an aggregate in the projection. We could perhaps
				// optimize for that, to avoid
				// having to evaluate twice.
				((Group)tupleExpr).addGroupElement(ge);
			}

			extension.setArg(tupleExpr);
			tupleExpr = new Filter(extension, condition);
		}

		// Apply projection
		tupleExpr = (TupleExpr)node.getSelect().jjtAccept(this, tupleExpr);

		// Process limit and offset clauses
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

		if (parentGP != null) {
			parentGP.addRequiredTE(tupleExpr);
			graphPattern = parentGP;
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

			Node child = projElemNode.jjtGetChild(0);

			String alias = projElemNode.getAlias();
			if (alias != null) {
				// aliased projection element
				ValueExpr valueExpr = (ValueExpr)child.jjtAccept(this, null);

				projElemList.addElement(new ProjectionElem(alias));

				if (valueExpr instanceof AggregateOperator) {
					// Apply implicit grouping if necessary
					Group group = null;
					if (result instanceof Group) {
						group = (Group)result;
					}
					else {
						if (result instanceof Filter) {
							TupleExpr filterArg = ((Filter)result).getArg();
							if (filterArg instanceof Group) {
								group = (Group)filterArg;
							}
							else if (filterArg instanceof Extension) {
								group = (Group)((Extension)filterArg).getArg();
							}
							else {
								group = new Group(result);
							}
						}
						else {
							group = new Group(result);
						}
					}

					group.addGroupElement(new GroupElem(alias, (AggregateOperator)valueExpr));

					extension.setArg(group);

					if (!(result instanceof Filter)) {
						result = group;
					}
				}
				extension.addElement(new ExtensionElem(valueExpr, alias));
			}
			else if (child instanceof ASTVar) {
				Var projVar = (Var)child.jjtAccept(this, null);
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
		else if (node.isReduced()) {
			result = new Reduced(result);
		}

		return result;
	}

	@Override
	public TupleExpr visit(ASTConstructQuery node, Object data)
		throws VisitorException
	{
		// Start with building the graph pattern
		graphPattern = new GraphPattern();
		node.getWhereClause().jjtAccept(this, null);
		TupleExpr tupleExpr = graphPattern.buildTupleExpr();

		// Apply result ordering
		ASTOrderClause orderNode = node.getOrderClause();
		if (orderNode != null) {
			List<OrderElem> orderElemements = (List<OrderElem>)orderNode.jjtAccept(this, null);
			tupleExpr = new Order(tupleExpr, orderElemements);
		}

		// Process construct clause
		tupleExpr = (TupleExpr)node.getConstruct().jjtAccept(this, tupleExpr);

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
			tupleExpr = new Slice(tupleExpr, offset, limit);
		}

		return tupleExpr;
	}

	@Override
	public TupleExpr visit(ASTConstruct node, Object data)
		throws VisitorException
	{
		TupleExpr result = (TupleExpr)data;

		// Collect construct triples
		graphPattern = new GraphPattern();
		super.visit(node, null);
		TupleExpr constructExpr = graphPattern.buildTupleExpr();

		// Retrieve all StatementPattern's from the construct expression
		List<StatementPattern> statementPatterns = StatementPatternCollector.process(constructExpr);

		Set<Var> constructVars = getConstructVars(statementPatterns);

		// Create BNodeGenerator's for all anonymous variables
		Map<Var, ExtensionElem> extElemMap = new HashMap<Var, ExtensionElem>();

		for (Var var : constructVars) {
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

		// Create a Projection for each StatementPattern in the constructor
		List<ProjectionElemList> projList = new ArrayList<ProjectionElemList>();

		for (StatementPattern sp : statementPatterns) {
			ProjectionElemList projElemList = new ProjectionElemList();

			projElemList.addElement(new ProjectionElem(sp.getSubjectVar().getName(), "subject"));
			projElemList.addElement(new ProjectionElem(sp.getPredicateVar().getName(), "predicate"));
			projElemList.addElement(new ProjectionElem(sp.getObjectVar().getName(), "object"));

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

	/**
	 * Gets the set of variables that are relevant for the constructor. This
	 * method accumulates all subject, predicate and object variables from the
	 * supplied statement patterns, but ignores any context variables.
	 */
	private Set<Var> getConstructVars(Collection<StatementPattern> statementPatterns) {
		Set<Var> vars = new LinkedHashSet<Var>(statementPatterns.size() * 2);

		for (StatementPattern sp : statementPatterns) {
			vars.add(sp.getSubjectVar());
			vars.add(sp.getPredicateVar());
			vars.add(sp.getObjectVar());
		}

		return vars;
	}

	@Override
	public TupleExpr visit(ASTDescribeQuery node, Object data)
		throws VisitorException
	{
		TupleExpr tupleExpr = null;

		if (node.getWhereClause() != null) {
			// Start with building the graph pattern
			graphPattern = new GraphPattern();
			node.getWhereClause().jjtAccept(this, null);
			tupleExpr = graphPattern.buildTupleExpr();

			// Apply result ordering
			ASTOrderClause orderNode = node.getOrderClause();
			if (orderNode != null) {
				List<OrderElem> orderElemements = (List<OrderElem>)orderNode.jjtAccept(this, null);
				tupleExpr = new Order(tupleExpr, orderElemements);
			}

			// Process limit and offset clauses
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
		}

		// Process describe clause last
		return (TupleExpr)node.getDescribe().jjtAccept(this, tupleExpr);
	}

	@Override
	public TupleExpr visit(ASTDescribe node, Object data)
		throws VisitorException
	{
		TupleExpr result = (TupleExpr)data;

		// Create a graph query that produces the statements that have the
		// requests resources as subject or object
		Var subjVar = createAnonVar("-descr-subj");
		Var predVar = createAnonVar("-descr-pred");
		Var objVar = createAnonVar("-descr-obj");
		StatementPattern sp = new StatementPattern(subjVar, predVar, objVar);

		if (result == null) {
			result = sp;
		}
		else {
			result = new Join(result, sp);
		}

		List<SameTerm> sameTerms = new ArrayList<SameTerm>(2 * node.jjtGetNumChildren());

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			ValueExpr resource = (ValueExpr)node.jjtGetChild(i).jjtAccept(this, null);

			sameTerms.add(new SameTerm(subjVar.clone(), resource));
			sameTerms.add(new SameTerm(objVar.clone(), resource));
		}

		ValueExpr constraint = sameTerms.get(0);
		for (int i = 1; i < sameTerms.size(); i++) {
			constraint = new Or(constraint, sameTerms.get(i));
		}

		result = new Filter(result, constraint);

		ProjectionElemList projElemList = new ProjectionElemList();
		projElemList.addElement(new ProjectionElem(subjVar.getName(), "subject"));
		projElemList.addElement(new ProjectionElem(predVar.getName(), "predicate"));
		projElemList.addElement(new ProjectionElem(objVar.getName(), "object"));
		result = new Projection(result, projElemList);

		return new Reduced(result);
	}

	@Override
	public TupleExpr visit(ASTAskQuery node, Object data)
		throws VisitorException
	{
		graphPattern = new GraphPattern();

		super.visit(node, null);

		TupleExpr tupleExpr = graphPattern.buildTupleExpr();
		tupleExpr = new Slice(tupleExpr, 0, 1);

		return tupleExpr;
	}

	@Override
	public List<GroupElem> visit(ASTGroupClause node, Object data)
		throws VisitorException
	{
		int childCount = node.jjtGetNumChildren();
		List<GroupElem> elements = new ArrayList<GroupElem>(childCount);

		for (int i = 0; i < childCount; i++) {
			elements.add((GroupElem)node.jjtGetChild(i).jjtAccept(this, data));
		}

		return elements;
	}

	@Override
	public GroupElem visit(ASTGroupCondition node, Object data)
		throws VisitorException
	{
		return ((GroupElem)data);
	}

	@Override
	public List<OrderElem> visit(ASTOrderClause node, Object data)
		throws VisitorException
	{
		int childCount = node.jjtGetNumChildren();
		List<OrderElem> elements = new ArrayList<OrderElem>(childCount);

		for (int i = 0; i < childCount; i++) {
			elements.add((OrderElem)node.jjtGetChild(i).jjtAccept(this, null));
		}

		return elements;
	}

	@Override
	public OrderElem visit(ASTOrderCondition node, Object data)
		throws VisitorException
	{
		ValueExpr valueExpr = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new OrderElem(valueExpr, node.isAscending());
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
	public Object visit(ASTGraphPatternGroup node, Object data)
		throws VisitorException
	{
		GraphPattern parentGP = graphPattern;
		graphPattern = new GraphPattern(parentGP);

		super.visit(node, null);

		// Filters are scoped to the graph pattern group and do not affect
		// bindings external to the group
		TupleExpr te = graphPattern.buildTupleExpr();

		// TODO not sure this is the cleanest way of handling this.
		if (data != null && data instanceof Exists) {
			((Exists)data).setSubQuery(te);
		}
		else {
			parentGP.addRequiredTE(te);
		}

		graphPattern = parentGP;

		return null;
	}

	@Override
	public Object visit(ASTOptionalGraphPattern node, Object data)
		throws VisitorException
	{
		GraphPattern parentGP = graphPattern;
		graphPattern = new GraphPattern(parentGP);

		super.visit(node, null);

		// Optional constraints also apply to left hand side of operator
		List<ValueExpr> constraints = graphPattern.removeAllConstraints();

		TupleExpr leftArg = parentGP.buildTupleExpr();
		TupleExpr rightArg = graphPattern.buildTupleExpr();

		LeftJoin leftJoin;

		if (constraints.isEmpty()) {
			leftJoin = new LeftJoin(leftArg, rightArg);
		}
		else {
			ValueExpr constraint = constraints.get(0);
			for (int i = 1; i < constraints.size(); i++) {
				constraint = new And(constraint, constraints.get(i));
			}

			leftJoin = new LeftJoin(leftArg, rightArg, constraint);
		}

		graphPattern = parentGP;

		graphPattern.clear();
		graphPattern.addRequiredTE(leftJoin);

		return null;
	}

	@Override
	public Object visit(ASTGraphGraphPattern node, Object data)
		throws VisitorException
	{
		Var oldContext = graphPattern.getContextVar();
		Scope oldScope = graphPattern.getStatementPatternScope();

		ValueExpr newContext = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);

		graphPattern.setContextVar(valueExpr2Var(newContext));
		graphPattern.setStatementPatternScope(Scope.NAMED_CONTEXTS);

		node.jjtGetChild(1).jjtAccept(this, null);

		graphPattern.setContextVar(oldContext);
		graphPattern.setStatementPatternScope(oldScope);

		return null;
	}

	@Override
	public Object visit(ASTUnionGraphPattern node, Object data)
		throws VisitorException
	{
		GraphPattern parentGP = graphPattern;

		graphPattern = new GraphPattern(parentGP);
		node.jjtGetChild(0).jjtAccept(this, null);
		TupleExpr leftArg = graphPattern.buildTupleExpr();

		graphPattern = new GraphPattern(parentGP);
		node.jjtGetChild(1).jjtAccept(this, null);
		TupleExpr rightArg = graphPattern.buildTupleExpr();

		parentGP.addRequiredTE(new Union(leftArg, rightArg));
		graphPattern = parentGP;

		return null;
	}

	@Override
	public Object visit(ASTMinusGraphPattern node, Object data)
		throws VisitorException
	{
		GraphPattern parentGP = graphPattern;

		TupleExpr leftArg = graphPattern.buildTupleExpr();

		graphPattern = new GraphPattern(parentGP);
		node.jjtGetChild(0).jjtAccept(this, null);
		TupleExpr rightArg = graphPattern.buildTupleExpr();

		parentGP = new GraphPattern();
		parentGP.addRequiredTE(new Difference(leftArg, rightArg));
		graphPattern = parentGP;

		return null;
	}

	@Override
	public Object visit(ASTPropertyList propListNode, Object data)
		throws VisitorException
	{
		ValueExpr subject = (ValueExpr)data;
		ValueExpr predicate = (ValueExpr)propListNode.getVerb().jjtAccept(this, null);
		@SuppressWarnings("unchecked")
		List<ValueExpr> objectList = (List<ValueExpr>)propListNode.getObjectList().jjtAccept(this, null);

		Var subjVar = valueExpr2Var(subject);
		Var predVar = valueExpr2Var(predicate);

		for (ValueExpr object : objectList) {
			Var objVar = valueExpr2Var(object);
			graphPattern.addRequiredSP(subjVar, predVar, objVar);
		}

		ASTPropertyList nextPropList = propListNode.getNextPropertyList();
		if (nextPropList != null) {
			nextPropList.jjtAccept(this, subject);
		}

		return null;
	}

	@Override
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

	@Override
	public Var visit(ASTBlankNodePropertyList node, Object data)
		throws VisitorException
	{
		Var bnodeVar = createAnonVar(node.getVarName());
		super.visit(node, bnodeVar);
		return bnodeVar;
	}

	@Override
	public Var visit(ASTCollection node, Object data)
		throws VisitorException
	{
		String listVarName = node.getVarName();
		Var rootListVar = createAnonVar(listVarName);

		Var listVar = rootListVar;

		int childCount = node.jjtGetNumChildren();
		for (int i = 0; i < childCount; i++) {
			ValueExpr childValue = (ValueExpr)node.jjtGetChild(i).jjtAccept(this, null);

			Var childVar = valueExpr2Var(childValue);
			graphPattern.addRequiredSP(listVar, createConstVar(RDF.FIRST), childVar);

			Var nextListVar;
			if (i == childCount - 1) {
				nextListVar = createConstVar(RDF.NIL);
			}
			else {
				nextListVar = createAnonVar(listVarName + "-" + (i + 1));
			}

			graphPattern.addRequiredSP(listVar, createConstVar(RDF.REST), nextListVar);
			listVar = nextListVar;
		}

		return rootListVar;
	}

	@Override
	public Object visit(ASTConstraint node, Object data)
		throws VisitorException
	{
		ValueExpr valueExpr = (ValueExpr)super.visit(node, null);
		graphPattern.addConstraint(valueExpr);

		return valueExpr;
	}

	@Override
	public Or visit(ASTOr node, Object data)
		throws VisitorException
	{
		ValueExpr leftArg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		ValueExpr rightArg = (ValueExpr)node.jjtGetChild(1).jjtAccept(this, null);
		return new Or(leftArg, rightArg);
	}

	@Override
	public Object visit(ASTAnd node, Object data)
		throws VisitorException
	{
		ValueExpr leftArg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		ValueExpr rightArg = (ValueExpr)node.jjtGetChild(1).jjtAccept(this, null);
		return new And(leftArg, rightArg);
	}

	@Override
	public Not visit(ASTNot node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)super.visit(node, null);
		return new Not(arg);
	}

	@Override
	public Compare visit(ASTCompare node, Object data)
		throws VisitorException
	{
		ValueExpr leftArg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		ValueExpr rightArg = (ValueExpr)node.jjtGetChild(1).jjtAccept(this, null);
		return new Compare(leftArg, rightArg, node.getOperator());
	}

	@Override
	public SameTerm visit(ASTSameTerm node, Object data)
		throws VisitorException
	{
		ValueExpr leftArg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		ValueExpr rightArg = (ValueExpr)node.jjtGetChild(1).jjtAccept(this, null);
		return new SameTerm(leftArg, rightArg);
	}

	@Override
	public MathExpr visit(ASTMath node, Object data)
		throws VisitorException
	{
		ValueExpr leftArg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		ValueExpr rightArg = (ValueExpr)node.jjtGetChild(1).jjtAccept(this, null);
		return new MathExpr(leftArg, rightArg, node.getOperator());
	}

	@Override
	public Object visit(ASTFunctionCall node, Object data)
		throws VisitorException
	{
		ValueConstant uriNode = (ValueConstant)node.jjtGetChild(0).jjtAccept(this, null);
		URI functionURI = (URI)uriNode.getValue();

		FunctionCall functionCall = new FunctionCall(functionURI.toString());

		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			Node argNode = node.jjtGetChild(i);
			functionCall.addArg((ValueExpr)argNode.jjtAccept(this, null));
		}

		return functionCall;
	}

	@Override
	public Object visit(ASTStr node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new Str(arg);
	}

	@Override
	public StrDt visit(ASTStrDt node, Object data)
		throws VisitorException
	{
		// TODO stricter checking on argument value types?
		ValueExpr literalExpr = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		ValueExpr datatypeExpr = (ValueExpr)node.jjtGetChild(1).jjtAccept(this, null);
		return new StrDt(literalExpr, datatypeExpr);
	}

	@Override
	public StrLang visit(ASTStrLang node, Object data)
		throws VisitorException
	{
		// TODO stricter checking on argument value types?
		ValueExpr literalExpr = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		ValueExpr langTagExpr = (ValueExpr)node.jjtGetChild(1).jjtAccept(this, null);
		return new StrLang(literalExpr, langTagExpr);
	}

	@Override
	public IRIFunction visit(ASTIRIFunc node, Object data)
		throws VisitorException
	{
		ValueExpr expr = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new IRIFunction(expr);
	}

	@Override
	public Lang visit(ASTLang node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new Lang(arg);
	}

	@Override
	public Datatype visit(ASTDatatype node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new Datatype(arg);
	}

	@Override
	public Object visit(ASTLangMatches node, Object data)
		throws VisitorException
	{
		ValueExpr leftArg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		ValueExpr rightArg = (ValueExpr)node.jjtGetChild(1).jjtAccept(this, null);
		return new LangMatches(leftArg, rightArg);
	}

	@Override
	public ValueExpr visit(ASTBound node, Object data)
		throws VisitorException
	{
		Var var = (Var)node.getArg().jjtAccept(this, null);
		return new Bound(var);
	}

	@Override
	public IsURI visit(ASTIsIRI node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new IsURI(arg);
	}

	@Override
	public IsBNode visit(ASTIsBlank node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new IsBNode(arg);
	}

	@Override
	public IsLiteral visit(ASTIsLiteral node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new IsLiteral(arg);
	}

	@Override
	public IsNumeric visit(ASTIsNumeric node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		return new IsNumeric(arg);
	}

	public Object visit(ASTBNodeFunc node, Object data)
		throws VisitorException
	{

		BNodeGenerator generator = new BNodeGenerator();

		if (node.jjtGetNumChildren() > 0) {
			ValueExpr nodeIdExpr = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
			generator.setNodeIdExpr(nodeIdExpr);
		}

		return generator;
	}

	@Override
	public Object visit(ASTRegexExpression node, Object data)
		throws VisitorException
	{
		ValueExpr arg = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, null);
		ValueExpr pattern = (ValueExpr)node.jjtGetChild(1).jjtAccept(this, null);
		ValueExpr flags = null;
		if (node.jjtGetNumChildren() > 2) {
			flags = (ValueExpr)node.jjtGetChild(2).jjtAccept(this, null);
		}
		return new Regex(arg, pattern, flags);
	}

	@Override
	public Exists visit(ASTExistsFunc node, Object data)
		throws VisitorException
	{
		Exists e = new Exists();

		node.jjtGetChild(0).jjtAccept(this, e);
		return e;
	}

	@Override
	public Not visit(ASTNotExistsFunc node, Object data)
		throws VisitorException
	{
		Exists e = new Exists();
		node.jjtGetChild(0).jjtAccept(this, e);
		return new Not(e);
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
	public ValueConstant visit(ASTIRI node, Object data)
		throws VisitorException
	{
		URI uri;
		try {
			uri = valueFactory.createURI(node.getValue());
		}
		catch (IllegalArgumentException e) {
			// invalid URI
			throw new VisitorException(e.getMessage());
		}

		return new ValueConstant(uri);
	}

	@Override
	public Object visit(ASTQName node, Object data)
		throws VisitorException
	{
		throw new VisitorException("QNames must be resolved before building the query model");
	}

	@Override
	public Object visit(ASTBlankNode node, Object data)
		throws VisitorException
	{
		throw new VisitorException(
				"Blank nodes must be replaced with variables before building the query model");
	}

	@Override
	public ValueConstant visit(ASTRDFLiteral node, Object data)
		throws VisitorException
	{
		String label = (String)node.getLabel().jjtAccept(this, null);
		String lang = node.getLang();
		ASTIRI datatypeNode = node.getDatatype();

		Literal literal;
		if (datatypeNode != null) {
			URI datatype;
			try {
				datatype = valueFactory.createURI(datatypeNode.getValue());
			}
			catch (IllegalArgumentException e) {
				// invalid URI
				throw new VisitorException(e.getMessage());
			}
			literal = valueFactory.createLiteral(label, datatype);
		}
		else if (lang != null) {
			literal = valueFactory.createLiteral(label, lang);
		}
		else {
			literal = valueFactory.createLiteral(label);
		}

		return new ValueConstant(literal);
	}

	@Override
	public ValueConstant visit(ASTNumericLiteral node, Object data)
		throws VisitorException
	{
		Literal literal = valueFactory.createLiteral(node.getValue(), node.getDatatype());
		return new ValueConstant(literal);
	}

	@Override
	public ValueConstant visit(ASTTrue node, Object data)
		throws VisitorException
	{
		return new ValueConstant(valueFactory.createLiteral(true));
	}

	@Override
	public ValueConstant visit(ASTFalse node, Object data)
		throws VisitorException
	{
		return new ValueConstant(valueFactory.createLiteral(false));
	}

	@Override
	public String visit(ASTString node, Object data)
		throws VisitorException
	{
		return node.getValue();
	}

	@Override
	public Object visit(ASTCount node, Object data)
		throws VisitorException
	{
		ValueExpr ve = null;
		if (node.jjtGetNumChildren() > 0) {
			ve = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, data);
		}
		return new Count(ve, node.isDistinct());
	}

	@Override
	public Object visit(ASTGroupConcat node, Object data)
		throws VisitorException
	{
		ValueExpr ve = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, data);

		GroupConcat gc = new GroupConcat(ve);

		if (node.jjtGetNumChildren() > 1) {
			ValueExpr separator = (ValueExpr)node.jjtGetChild(1).jjtAccept(this, data);
			gc.setSeparator(separator);
		}

		return gc;
	}

	@Override
	public Object visit(ASTMax node, Object data)
		throws VisitorException
	{
		ValueExpr ve = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, data);

		return new Max(ve);
	}

	@Override
	public Object visit(ASTMin node, Object data)
		throws VisitorException
	{
		ValueExpr ve = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, data);

		return new Min(ve);
	}

	@Override
	public Object visit(ASTSum node, Object data)
		throws VisitorException
	{
		ValueExpr ve = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, data);

		return new Sum(ve);
	}

	@Override
	public Object visit(ASTAvg node, Object data)
		throws VisitorException
	{
		ValueExpr ve = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, data);

		return new Avg(ve);
	}

	static class AggregateCollector extends QueryModelVisitorBase<VisitorException> {

		private Collection<AggregateOperator> operators = new ArrayList<AggregateOperator>();

		public Collection<AggregateOperator> getOperators() {
			return operators;
		}

		@Override
		public void meet(Avg node)
			throws VisitorException
		{
			super.meet(node);
			meetAggregate(node);
		}

		@Override
		public void meet(Count node)
			throws VisitorException
		{
			super.meet(node);
			meetAggregate(node);
		}

		@Override
		public void meet(GroupConcat node)
			throws VisitorException
		{
			super.meet(node);
			meetAggregate(node);
		}

		@Override
		public void meet(Max node)
			throws VisitorException
		{
			super.meet(node);
			meetAggregate(node);
		}

		@Override
		public void meet(Min node)
			throws VisitorException
		{
			super.meet(node);
			meetAggregate(node);
		}

		@Override
		public void meet(Sample node)
			throws VisitorException
		{
			super.meet(node);
			meetAggregate(node);
		}

		@Override
		public void meet(Sum node)
			throws VisitorException
		{
			super.meet(node);
			meetAggregate(node);
		}

		private void meetAggregate(AggregateOperator node) {
			operators.add(node);
		}
	}

	static class AggregateOperatorReplacer extends QueryModelVisitorBase<VisitorException> {

		private Var replacement;

		private AggregateOperator operator;

		public AggregateOperatorReplacer(AggregateOperator operator, Var replacement) {
			this.operator = operator;
			this.replacement = replacement;
		}

		@Override
		public void meet(Avg node)
			throws VisitorException
		{
			super.meet(node);
			meetAggregate(node);
		}

		@Override
		public void meet(Count node)
			throws VisitorException
		{
			super.meet(node);
			meetAggregate(node);
		}

		@Override
		public void meet(GroupConcat node)
			throws VisitorException
		{
			super.meet(node);
			meetAggregate(node);
		}

		@Override
		public void meet(Max node)
			throws VisitorException
		{
			super.meet(node);
			meetAggregate(node);
		}

		@Override
		public void meet(Min node)
			throws VisitorException
		{
			super.meet(node);
			meetAggregate(node);
		}

		@Override
		public void meet(Sample node)
			throws VisitorException
		{
			super.meet(node);
			meetAggregate(node);
		}

		@Override
		public void meet(Sum node)
			throws VisitorException
		{
			super.meet(node);
			meetAggregate(node);
		}

		private void meetAggregate(AggregateOperator node) {
			if (node.equals(operator)) {
				node.getParentNode().replaceChildNode(node, replacement);
			}
		}
	}
}
