/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.spin;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.BindingSetAssignment;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.DescribeOperator;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.MathExpr.MathOp;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedDescribeQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedOperation;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.spin.util.Statements;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

public class SPINParser {

	private static final Set<URI> QUERY_TYPES = Sets.newHashSet(SP.SELECT_CLASS, SP.CONSTRUCT_CLASS,
			SP.ASK_CLASS, SP.DESCRIBE_CLASS);

	private static final Set<URI> UPDATE_TYPES = Sets.newHashSet(SP.MODIFY_CLASS, SP.INSERT_DATA_CLASS,
			SP.DELETE_DATA_CLASS, SP.LOAD_CLASS, SP.CLEAR_CLASS, SP.CREATE_CLASS, SP.DROP_CLASS);

	private static final Set<URI> COMMAND_TYPES = Sets.union(QUERY_TYPES, UPDATE_TYPES);

	private static final Set<URI> NON_TEMPLATES = Sets.newHashSet(RDFS.RESOURCE, SP.SYSTEM_CLASS,
			SP.COMMAND_CLASS, SP.QUERY_CLASS, SP.UPDATE_CLASS, SPIN.MODULES_CLASS, SPIN.TEMPLATES_CLASS,
			SPIN.ASK_TEMPLATES_CLASS, SPIN.SELECT_TEMPLATES_CLASS, SPIN.CONSTRUCT_TEMPLATES_CLASS,
			SPIN.UPDATE_TEMPLATES_CLASS, SPIN.RULE_CLASS);

	private static final Set<URI> TEMPLATE_TYPES = Sets.newHashSet(SPIN.ASK_TEMPLATE_CLASS,
			SPIN.SELECT_TEMPLATE_CLASS, SPIN.CONSTRUCT_TEMPLATE_CLASS, SPIN.UPDATE_TEMPLATE_CLASS);

	private static final Set<URI> NON_ARG_PROPERTIES = Sets.newHashSet(RDF.TYPE, SP.ARG_PROPERTY, SP.ELEMENTS_PROPERTY);

	public enum Input {
		TEXT_FIRST(true, true),
		TEXT_ONLY(true, false),
		RDF_FIRST(false, true),
		RDF_ONLY(false, false);

		final boolean textFirst;

		final boolean canFallback;

		Input(boolean textFirst, boolean canFallback) {
			this.textFirst = textFirst;
			this.canFallback = canFallback;
		}
	}

	private final Input input;
	private final Function<URI,String> wellKnownVars;
	private final Function<URI,String> wellKnownFunctions;

	public SPINParser() {
		this(Input.TEXT_FIRST);
	}

	public SPINParser(Input input) {
		this(input,
			new Function<URI,String>() {
				@Override
				public String apply(URI uri) {
					return SPINWellKnownVars.INSTANCE.getName(uri);
				}
			},
			new Function<URI,String>() {
				@Override
				public String apply(URI uri) {
					return SPINWellKnownFunctions.INSTANCE.getName(uri);
				}
			});
	}

	public SPINParser(Input input, Function<URI,String> wellKnownVarsMapper, Function<URI,String> wellKnownFuncMapper) {
		this.input = input;
		this.wellKnownVars = wellKnownVarsMapper;
		this.wellKnownFunctions = wellKnownFuncMapper;
	}

	public Map<URI, RuleProperty> parseRuleProperties(TripleSource store)
		throws OpenRDFException
	{
		Map<URI, RuleProperty> rules = new HashMap<URI, RuleProperty>();
		CloseableIteration<? extends URI, ? extends OpenRDFException> rulePropIter = Statements.getSubjectURIs(
				RDFS.SUBPROPERTYOF, SPIN.RULE_PROPERTY, store);
		try {
			while (rulePropIter.hasNext()) {
				URI ruleProp = rulePropIter.next();
				RuleProperty ruleProperty = new RuleProperty(ruleProp);

				List<URI> nextRules = getNextRules(ruleProp, store);
				ruleProperty.setNextRules(nextRules);

				int maxIterCount = getMaxIterationCount(ruleProp, store);
				ruleProperty.setMaxIterationCount(maxIterCount);

				rules.put(ruleProp, ruleProperty);
			}
		}
		finally {
			rulePropIter.close();
		}
		return rules;
	}

	private List<URI> getNextRules(Resource ruleProp, TripleSource store)
		throws OpenRDFException
	{
		List<URI> nextRules = new ArrayList<URI>();
		CloseableIteration<? extends URI, ? extends OpenRDFException> iter = Statements.getObjectURIs(ruleProp,
				SPIN.NEXT_RULE_PROPERTY_PROPERTY, store);
		try {
			while (iter.hasNext()) {
				nextRules.add(iter.next());
			}
		}
		finally {
			iter.close();
		}
		return nextRules;
	}

	private int getMaxIterationCount(Resource ruleProp, TripleSource store)
		throws OpenRDFException
	{
		Value v = Statements.singleValue(ruleProp, SPIN.RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY, store);
		if (v == null) {
			return -1;
		}
		else if (v instanceof Literal) {
			try {
				return ((Literal)v).intValue();
			}
			catch (NumberFormatException e) {
				throw new MalformedSPINException("Value for " + SPIN.RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY
						+ " must be of datatype " + XMLSchema.INTEGER + ": " + ruleProp);
			}
		}
		else {
			throw new MalformedSPINException("Non-literal value for "
					+ SPIN.RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY + ": " + ruleProp);
		}
	}

	public boolean isThisUnbound(Resource subj, TripleSource store)
		throws OpenRDFException
	{
		return Statements.booleanValue(subj, SPIN.THIS_UNBOUND_PROPERTY, store);
	}

	public ConstraintViolation parseConstraintViolation(Resource subj, TripleSource store)
		throws OpenRDFException
	{
		Value labelValue = Statements.singleValue(subj, RDFS.LABEL, store);
		Value rootValue = Statements.singleValue(subj, SPIN.VIOLATION_ROOT_PROPERTY, store);
		Value pathValue = Statements.singleValue(subj, SPIN.VIOLATION_PATH_PROPERTY, store);
		Value valueValue = Statements.singleValue(subj, SPIN.VIOLATION_VALUE_PROPERTY, store);
		Value levelValue = Statements.singleValue(subj, SPIN.VIOLATION_LEVEL_PROPERTY, store);
		String label = (labelValue instanceof Literal) ? labelValue.stringValue() : null;
		String root = (rootValue instanceof Resource) ? rootValue.stringValue() : null;
		String path = (pathValue != null) ? pathValue.stringValue() : null;
		String value = (valueValue != null) ? valueValue.stringValue() : null;
		ConstraintViolationLevel level;
		if (levelValue == null) {
			level = ConstraintViolationLevel.ERROR;
		}
		else if (SPIN.INFO_VIOLATION_LEVEL.equals(levelValue)) {
			level = ConstraintViolationLevel.INFO;
		}
		else if (SPIN.WARNING_VIOLATION_LEVEL.equals(levelValue)) {
			level = ConstraintViolationLevel.WARNING;
		}
		else if (SPIN.ERROR_VIOLATION_LEVEL.equals(levelValue)) {
			level = ConstraintViolationLevel.ERROR;
		}
		else if (SPIN.FATAL_VIOLATION_LEVEL.equals(levelValue)) {
			level = ConstraintViolationLevel.FATAL;
		}
		else {
			throw new MalformedSPINException("Invalid value " + levelValue + " for "
					+ SPIN.VIOLATION_LEVEL_PROPERTY + ": " + subj);
		}
		return new ConstraintViolation(label, root, path, value, level);
	}

	public ParsedOperation parse(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return parse(queryResource, SP.COMMAND_CLASS, store);
	}

	public ParsedQuery parseQuery(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return (ParsedQuery)parse(queryResource, SP.QUERY_CLASS, store);
	}

	public ParsedGraphQuery parseConstructQuery(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return (ParsedGraphQuery)parse(queryResource, SP.CONSTRUCT_CLASS, store);
	}

	public ParsedTupleQuery parseSelectQuery(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return (ParsedTupleQuery)parse(queryResource, SP.SELECT_CLASS, store);
	}

	public ParsedBooleanQuery parseAskQuery(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return (ParsedBooleanQuery)parse(queryResource, SP.ASK_CLASS, store);
	}

	public ParsedDescribeQuery parseDescribeQuery(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return (ParsedDescribeQuery)parse(queryResource, SP.DESCRIBE_CLASS, store);
	}

	protected ParsedOperation parse(Resource queryResource, URI queryClass, TripleSource store)
		throws OpenRDFException
	{
		Boolean isQueryElseTemplate = null;
		Set<URI> possibleQueryTypes = new HashSet<URI>();
		Set<URI> possibleTemplates = new HashSet<URI>();
		CloseableIteration<? extends URI, ? extends OpenRDFException> typeIter = Statements.getObjectURIs(
				queryResource, RDF.TYPE, store);
		try {
			while (typeIter.hasNext()) {
				URI type = typeIter.next();
				if (isQueryElseTemplate == null && SPIN.TEMPLATES_CLASS.equals(type)) {
					isQueryElseTemplate = Boolean.FALSE;
				}
				else if ((isQueryElseTemplate == null || isQueryElseTemplate == Boolean.TRUE)
						&& COMMAND_TYPES.contains(type))
				{
					isQueryElseTemplate = Boolean.TRUE;
					possibleQueryTypes.add(type);
				}
				else if ((isQueryElseTemplate == null || isQueryElseTemplate == Boolean.FALSE)
						&& !NON_TEMPLATES.contains(type))
				{
					possibleTemplates.add(type);
				}
			}
		}
		finally {
			typeIter.close();
		}

		ParsedOperation parsedOp;
		if (isQueryElseTemplate == null) {
			throw new MalformedSPINException("Missing RDF type: " + queryResource);
		}
		else if (isQueryElseTemplate == Boolean.TRUE) {
			// command (query or update)
			if (possibleQueryTypes.size() > 1) {
				throw new MalformedSPINException("Incompatible RDF types for command: " + queryResource
						+ " has types " + possibleQueryTypes);
			}

			URI queryType = possibleQueryTypes.iterator().next();

			if (input.textFirst) {
				parsedOp = parseText(queryResource, queryType, store);
				if (parsedOp == null && input.canFallback) {
					parsedOp = parseRDF(queryResource, queryType, store);
				}
			}
			else {
				parsedOp = parseRDF(queryResource, queryType, store);
				if (parsedOp == null && input.canFallback) {
					parsedOp = parseText(queryResource, queryType, store);
				}
			}

			if (parsedOp == null) {
				throw new MalformedSPINException("Command is not parsable: " + queryResource);
			}
		}
		else {
			// template
			URI templateResource;
			if (possibleTemplates.size() > 1) {
				templateResource = null;
				for (URI t : possibleTemplates) {
					Value abstractValue = Statements.singleValue(t, SPIN.ABSTRACT_PROPERTY, store);
					if(abstractValue == null || BooleanLiteralImpl.FALSE.equals(abstractValue)) {
						templateResource = t;
						break;
					}
				}
			}
			else if (possibleTemplates.size() == 1) {
				templateResource = possibleTemplates.iterator().next();
			}
			else {
				templateResource = null;
			}

			if (templateResource == null) {
				throw new MalformedSPINException("Template missing RDF type: " + queryResource);
			}

			Template tmpl = parseTemplate(templateResource, queryClass, store);
			// TODO
			BindingSet args = null;
			parsedOp = new ParsedTemplateQuery(templateResource, tmpl.getParsedOperation(), args);
		}

		return parsedOp;
	}

	private Template parseTemplate(URI tmplUri, URI queryType, TripleSource store)
		throws OpenRDFException
	{
		Set<URI> possibleTmplTypes = new HashSet<URI>();
		CloseableIteration<? extends URI, ? extends OpenRDFException> typeIter = Statements.getObjectURIs(
				tmplUri, RDF.TYPE, store);
		try {
			while (typeIter.hasNext()) {
				URI type = typeIter.next();
				if (TEMPLATE_TYPES.contains(type)) {
					possibleTmplTypes.add(type);
				}
			}
		}
		finally {
			typeIter.close();
		}

		if (possibleTmplTypes.isEmpty()) {
			throw new MalformedSPINException("Template missing RDF type: " + tmplUri);
		}
		else if (possibleTmplTypes.size() > 1) {
			throw new MalformedSPINException("Incompatible RDF types for template: " + tmplUri + " has types "
					+ possibleTmplTypes);
		}

		URI tmplType = possibleTmplTypes.iterator().next();

		Set<URI> compatibleTmplTypes;
		if (SP.QUERY_CLASS.equals(queryType)) {
			compatibleTmplTypes = Sets.newHashSet(SPIN.ASK_TEMPLATE_CLASS, SPIN.SELECT_TEMPLATE_CLASS,
					SPIN.CONSTRUCT_TEMPLATE_CLASS);
		}
		else if (SP.UPDATE_CLASS.equals(queryType) || UPDATE_TYPES.contains(queryType)) {
			compatibleTmplTypes = Collections.singleton(SPIN.UPDATE_TEMPLATE_CLASS);
		}
		else if (SP.ASK_CLASS.equals(queryType)) {
			compatibleTmplTypes = Collections.singleton(SPIN.ASK_TEMPLATE_CLASS);
		}
		else if (SP.SELECT_CLASS.equals(queryType)) {
			compatibleTmplTypes = Collections.singleton(SPIN.SELECT_TEMPLATE_CLASS);
		}
		else if (SP.CONSTRUCT_CLASS.equals(queryType)) {
			compatibleTmplTypes = Collections.singleton(SPIN.CONSTRUCT_TEMPLATE_CLASS);
		}
		else {
			compatibleTmplTypes = TEMPLATE_TYPES;
		}
		if (!compatibleTmplTypes.contains(tmplType)) {
			throw new MalformedSPINException("Template type " + tmplType + " is incompatible with command type "
					+ queryType);
		}

		Template tmpl = new Template(tmplUri);

		Value body = Statements.singleValue(tmplUri, SPIN.BODY_PROPERTY, store);
		if (!(body instanceof Resource)) {
			throw new MalformedSPINException("Template body is not a resource: " + body);
		}
		ParsedOperation op = parse((Resource)body, queryType, store);
		tmpl.setParsedOperation(op);

		// TODO args

		return tmpl;
	}

	private ParsedOperation parseText(Resource queryResource, URI queryType, TripleSource store)
		throws OpenRDFException
	{
		Value text = Statements.singleValue(queryResource, SP.TEXT_PROPERTY, store);
		if (text != null) {
			if (QUERY_TYPES.contains(queryType)) {
				return QueryParserUtil.parseQuery(QueryLanguage.SPARQL, text.stringValue(), null);
			}
			else if (UPDATE_TYPES.contains(queryType)) {
				return QueryParserUtil.parseUpdate(QueryLanguage.SPARQL, text.stringValue(), null);
			}
			else {
				throw new MalformedSPINException("Unrecognised command type: " + queryType);
			}
		}
		else {
			return null;
		}
	}

	private ParsedOperation parseRDF(Resource queryResource, URI queryType, TripleSource store)
		throws OpenRDFException
	{
		if(SP.CONSTRUCT_CLASS.equals(queryType)) {
			SPINVisitor visitor = new SPINVisitor(store);
			visitor.visitConstruct(queryResource);
			return new ParsedGraphQuery(visitor.getTupleExpr());
		}
		else if(SP.SELECT_CLASS.equals(queryType)) {
			SPINVisitor visitor = new SPINVisitor(store);
			visitor.visitSelect(queryResource);
			return new ParsedTupleQuery(visitor.getTupleExpr());
		}
		else if(SP.ASK_CLASS.equals(queryType)) {
			SPINVisitor visitor = new SPINVisitor(store);
			visitor.visitAsk(queryResource);
			return new ParsedBooleanQuery(visitor.getTupleExpr());
		}
		else if(SP.DESCRIBE_CLASS.equals(queryType)) {
			SPINVisitor visitor = new SPINVisitor(store);
			visitor.visitDescribe(queryResource);
			return new ParsedDescribeQuery(visitor.getTupleExpr());
		}
		else if (UPDATE_TYPES.contains(queryType)) {
			return new ParsedUpdate();
		}
		else {
			throw new MalformedSPINException("Unrecognised command type: " + queryType);
		}
	}


	class SPINVisitor {
		final TripleSource store;
		TupleExpr root;
		TupleExpr node;
		Var namedGraph;
		Map<String,ExtensionElem> extElems;
		Map<Resource,String> vars = new HashMap<Resource,String>();

		SPINVisitor(TripleSource store) {
			this.store = store;
		}

		public TupleExpr getTupleExpr() {
			return root;
		}

		public void visitConstruct(Resource construct)
				throws OpenRDFException
		{
			Value templates = Statements.singleValue(construct, SP.TEMPLATES_PROPERTY, store);
			if(!(templates instanceof Resource)) {
				throw new MalformedSPINException("Value of "+SP.TEMPLATES_PROPERTY+" is not a resource");
			}

			extElems = new LinkedHashMap<String,ExtensionElem>();

			UnaryTupleOperator projection = visitTemplates((Resource) templates);
			visitWhere(construct);

			if(!extElems.isEmpty()) {
				Extension ext = new Extension();
				ext.setElements(extElems.values());
				ext.setArg(projection.getArg());
				projection.setArg(ext);
			}
		}

		public void visitDescribe(Resource describe)
				throws OpenRDFException
		{
			Value resultNodes = Statements.singleValue(describe, SP.RESULT_NODES_PROPERTY, store);
			if(!(resultNodes instanceof Resource)) {
				throw new MalformedSPINException("Value of "+SP.RESULT_NODES_PROPERTY+" is not a resource");
			}

			extElems = new LinkedHashMap<String,ExtensionElem>();

			Projection projection = visitResultNodes((Resource) resultNodes);
			visitWhere(describe);

			if(!extElems.isEmpty()) {
				Extension ext = new Extension();
				ext.setElements(extElems.values());
				ext.setArg(projection.getArg());
				projection.setArg(ext);
			}
		}

		public void visitSelect(Resource select)
				throws OpenRDFException
		{
			Value resultVars = Statements.singleValue(select, SP.RESULT_VARIABLES_PROPERTY, store);
			if(!(resultVars instanceof Resource)) {
				throw new MalformedSPINException("Value of "+SP.RESULT_VARIABLES_PROPERTY+" is not a resource");
			}

			extElems = new LinkedHashMap<String,ExtensionElem>();

			Projection projection = visitResultVariables((Resource) resultVars);
			visitWhere(select);

			if(!extElems.isEmpty()) {
				Extension ext = new Extension();
				ext.setElements(extElems.values());
				ext.setArg(projection.getArg());
				projection.setArg(ext);
			}

			Value orderby = Statements.singleValue(select, SP.ORDER_BY_PROPERTY, store);
			if(orderby instanceof Resource) {
				Order order = visitOrderBy((Resource) orderby);
				order.setArg(projection.getArg());
				projection.setArg(order);
			}

			boolean distinct = Statements.booleanValue(select, SP.DISTINCT_PROPERTY, store);
			if(distinct) {
				root = new Distinct(root);
			}

			long offset = -1L;
			Value offsetValue = Statements.singleValue(select, SP.OFFSET_PROPERTY, store);
			if(offsetValue instanceof Literal) {
				offset = ((Literal)offsetValue).longValue();
			}
			long limit = -1L;
			Value limitValue = Statements.singleValue(select, SP.LIMIT_PROPERTY, store);
			if(limitValue instanceof Literal) {
				limit = ((Literal)limitValue).longValue();
			}
			if(offset > 0L || limit >= 0L) {
				Slice slice = new Slice(root);
				if(offset > 0L) {
					slice.setOffset(offset);
				}
				if(limit >= 0L) {
					slice.setLimit(limit);
				}
				root = slice;
			}
		}

		public void visitAsk(Resource ask)
				throws OpenRDFException
		{
			node = new SingletonSet();
			root = new Slice(node, 0, 1);
			visitWhere(ask);
		}

		private UnaryTupleOperator visitTemplates(Resource templates)
				throws OpenRDFException
		{
			List<ProjectionElemList> projElemLists = new ArrayList<ProjectionElemList>();
			Iteration<? extends Resource,QueryEvaluationException> iter = Statements.listResources(templates, store);
			while(iter.hasNext()) {
				Resource r = iter.next();
				ProjectionElemList projElems = visitTemplate(r);
				projElemLists.add(projElems);
			}

			UnaryTupleOperator expr;
			if(projElemLists.size() > 1) {
				MultiProjection proj = new MultiProjection();
				proj.setProjections(projElemLists);
				expr = proj;
			}
			else {
				Projection proj = new Projection();
				proj.setProjectionElemList(projElemLists.get(0));
				expr = proj;
			}

			Reduced reduced = new Reduced();
			reduced.setArg(expr);
			root = reduced;
			SingletonSet stub = new SingletonSet();
			expr.setArg(stub);
			node = stub;
			return expr;
		}

		private ProjectionElemList visitTemplate(Resource r)
				throws OpenRDFException
		{
			ProjectionElemList projElems = new ProjectionElemList();
			Value subj = Statements.singleValue(r, SP.SUBJECT_PROPERTY, store);
			projElems.addElement(createProjectionElem(subj, "subject"));
			Value pred = Statements.singleValue(r, SP.PREDICATE_PROPERTY, store);
			projElems.addElement(createProjectionElem(pred, "predicate"));
			Value obj = Statements.singleValue(r, SP.OBJECT_PROPERTY, store);
			projElems.addElement(createProjectionElem(obj, "object"));
			return projElems;
		}

		private Projection visitResultNodes(Resource resultNodes)
				throws OpenRDFException
		{
			ProjectionElemList projElemList = new ProjectionElemList();
			Iteration<? extends Resource,QueryEvaluationException> iter = Statements.listResources(resultNodes, store);
			while(iter.hasNext()) {
				Resource r = iter.next();
				ProjectionElem projElem = visitResultNode(r);
				projElemList.addElement(projElem);
			}

			Projection proj = new Projection();
			proj.setProjectionElemList(projElemList);

			root = new DescribeOperator(proj);
			SingletonSet stub = new SingletonSet();
			proj.setArg(stub);
			node = stub;
			return proj;
		}

		private ProjectionElem visitResultNode(Resource r)
				throws OpenRDFException
		{
			return createProjectionElem(r, getVarName(r));
		}

		private Projection visitResultVariables(Resource resultVars)
				throws OpenRDFException
		{
			ProjectionElemList projElemList = new ProjectionElemList();
			Iteration<? extends Resource,QueryEvaluationException> iter = Statements.listResources(resultVars, store);
			while(iter.hasNext()) {
				Resource r = iter.next();
				ProjectionElem projElem = visitResultVariable(r);
				projElemList.addElement(projElem);
			}

			Projection proj = new Projection();
			proj.setProjectionElemList(projElemList);

			root = proj;
			SingletonSet stub = new SingletonSet();
			proj.setArg(stub);
			node = stub;
			return proj;
		}

		private ProjectionElem visitResultVariable(Resource r)
			throws OpenRDFException
		{
			return createProjectionElem(r, getVarName(r));
		}

		private Order visitOrderBy(Resource orderby)
			throws OpenRDFException
		{
			Order order = new Order();
			Iteration<? extends Resource,QueryEvaluationException> iter = Statements.listResources(orderby, store);
			while(iter.hasNext()) {
				Resource r = iter.next();
				OrderElem orderElem = visitOrderByCondition(r);
				order.addElement(orderElem);
			}
			return order;
		}

		private OrderElem visitOrderByCondition(Resource r)
				throws OpenRDFException
		{
			Value expr = Statements.singleValue(r, SP.EXPRESSION_PROPERTY, store);
			ValueExpr valueExpr = visitExpression(expr);
			Set<Resource> types = Iterations.asSet(Statements.getObjectResources(r, RDF.TYPE, store));
			boolean asc = types.contains(SP.DESC_CLASS) ? false : true;
			return new OrderElem(valueExpr, asc);
		}

		private ProjectionElem createProjectionElem(Value v, String projName)
			throws OpenRDFException
		{
			String varName = null;
			if(v instanceof Resource) {
				varName = getVarName((Resource) v);
			}

			if(varName != null) {
				extElems.put(varName, new ExtensionElem(new Var(varName), varName));
			}
			else {
				varName = getConstVarName(v);
				extElems.put(varName, new ExtensionElem(new ValueConstant(v), varName));
			}
			return new ProjectionElem(varName, projName);
		}

		public void visitWhere(Resource query)
				throws OpenRDFException
		{
			Value where = Statements.singleValue(query, SP.WHERE_PROPERTY, store);
			if(!(where instanceof Resource)) {
				throw new MalformedSPINException("Value of "+SP.WHERE_PROPERTY+" is not a resource");
			}
			visitGroupGraphPattern((Resource) where);
		}

		public void visitGroupGraphPattern(Resource group)
			throws OpenRDFException
		{
			Map<Resource,Set<URI>> patternTypes = new LinkedHashMap<Resource,Set<URI>>();
			Iteration<? extends Resource, QueryEvaluationException> groupIter = Statements.listResources(group, store);
			while(groupIter.hasNext()) {
				Resource r = groupIter.next();
				patternTypes.put(r, Iterations.asSet(Statements.getObjectURIs(r, RDF.TYPE, store)));
			}

			// first process filters
			for(Iterator<Map.Entry<Resource,Set<URI>>> iter = patternTypes.entrySet().iterator(); iter.hasNext(); ) {
				Map.Entry<Resource,Set<URI>> entry = iter.next();
				if(entry.getValue().contains(SP.FILTER_CLASS)) {
					visitFilter(entry.getKey());
					iter.remove();
				}
			}

			// then binds
			for(Iterator<Map.Entry<Resource,Set<URI>>> iter = patternTypes.entrySet().iterator(); iter.hasNext(); ) {
				Map.Entry<Resource,Set<URI>> entry = iter.next();
				if(entry.getValue().contains(SP.BIND_CLASS)) {
					visitBind(entry.getKey());
					iter.remove();
				}
			}

			// then anything else
			for(Iterator<Map.Entry<Resource,Set<URI>>> iter = patternTypes.entrySet().iterator(); iter.hasNext(); ) {
				Map.Entry<Resource,Set<URI>> entry = iter.next();
				visitPattern(entry.getKey(), entry.getValue());
			}
		}

		private void visitPattern(Resource r, Set<URI> types)
			throws OpenRDFException
		{
			TupleExpr currentNode = node;
			Value subj = Statements.singleValue(r, SP.SUBJECT_PROPERTY, store);
			if(subj != null) {
				Value pred = Statements.singleValue(r, SP.PREDICATE_PROPERTY, store);
				Value obj = Statements.singleValue(r, SP.OBJECT_PROPERTY, store);
				Scope stmtScope = (namedGraph != null) ? Scope.NAMED_CONTEXTS : Scope.DEFAULT_CONTEXTS;
				node = new StatementPattern(stmtScope, getVar(subj), getVar(pred), getVar(obj), namedGraph);
			}
			else {
				if(types.contains(SP.NAMED_GRAPH_CLASS)) {
					Var oldGraph = namedGraph;
					Value graphValue = Statements.singleValue(r, SP.GRAPH_NAME_NODE_PROPERTY, store);
					namedGraph = getVar(graphValue);
					Value elements = Statements.singleValue(r, SP.ELEMENTS_PROPERTY, store);
					if(!(elements instanceof Resource)) {
						throw new MalformedSPINException("Value of "+SP.ELEMENTS_PROPERTY+" is not a resource");
					}
					node = new SingletonSet();
					QueryRoot group = new QueryRoot(node);
					visitGroupGraphPattern((Resource) elements);
					node = group.getArg();
					namedGraph = oldGraph;
				}
				else if(types.contains(SP.UNION_CLASS)) {
					Value elements = Statements.singleValue(r, SP.ELEMENTS_PROPERTY, store);
					if(!(elements instanceof Resource)) {
						throw new MalformedSPINException("Value of "+SP.ELEMENTS_PROPERTY+" is not a resource");
					}

					Iteration<? extends Resource, QueryEvaluationException> iter = Statements.listResources((Resource) elements, store);
					TupleExpr prev = null;
					while(iter.hasNext()) {
						Resource entry = iter.next();
						node = new SingletonSet();
						QueryRoot groupRoot = new QueryRoot(node);
						visitGroupGraphPattern(entry);
						TupleExpr groupExpr = groupRoot.getArg();
						if(prev != null) {
							node = new Union(prev, groupExpr);
						}
						prev = groupExpr;
					}
				}
				else if(types.contains(SP.OPTIONAL_CLASS)) {
					Value elements = Statements.singleValue(r, SP.ELEMENTS_PROPERTY, store);
					if(!(elements instanceof Resource)) {
						throw new MalformedSPINException("Value of "+SP.ELEMENTS_PROPERTY+" is not a resource");
					}
					node = new SingletonSet();
					QueryRoot groupRoot = new QueryRoot(node);
					visitGroupGraphPattern((Resource) elements);
					LeftJoin leftJoin = new LeftJoin();
					currentNode.replaceWith(leftJoin);
					leftJoin.setLeftArg(currentNode);
					leftJoin.setRightArg(groupRoot.getArg());
					node = leftJoin;
					currentNode = null;
				}
				else if(types.contains(SP.MINUS_CLASS)) {
					Value elements = Statements.singleValue(r, SP.ELEMENTS_PROPERTY, store);
					if(!(elements instanceof Resource)) {
						throw new MalformedSPINException("Value of "+SP.ELEMENTS_PROPERTY+" is not a resource");
					}
					node = new SingletonSet();
					QueryRoot groupRoot = new QueryRoot(node);
					visitGroupGraphPattern((Resource) elements);
					Difference difference = new Difference();
					currentNode.replaceWith(difference);
					difference.setLeftArg(currentNode);
					difference.setRightArg(groupRoot.getArg());
					node = difference;
					currentNode = null;
				}
				else if(types.contains(SP.SUB_QUERY_CLASS)) {
					// TODO
				}
				else if(types.contains(SP.VALUES_CLASS)) {
					// TODO
					BindingSetAssignment bsa = new BindingSetAssignment();
					node = bsa;
				}
				else if(types.contains(RDF.LIST) || (Statements.singleValue(r, RDF.FIRST, store) != null)) {
					node = new SingletonSet();
					QueryRoot group = new QueryRoot(node);
					visitGroupGraphPattern(r);
					node = group.getArg();
				}
				else if(types.contains(SP.SERVICE_CLASS)) {
					Value serviceUri = Statements.singleValue(r, SP.SERVICE_URI_PROPERTY, store);
					boolean isSilent = false;
					node = new SingletonSet();
					String exprString = ""; // TODO
					Map<String,String> prefixDecls = Collections.emptyMap(); // TODO
					Service service = new Service(getVar(serviceUri), node, exprString, prefixDecls, null, isSilent);
					Value elements = Statements.singleValue(r, SP.ELEMENTS_PROPERTY, store);
					if(!(elements instanceof Resource)) {
						throw new MalformedSPINException("Value of "+SP.ELEMENTS_PROPERTY+" is not a resource");
					}
					visitGroupGraphPattern((Resource) elements);
					node = service;
				}
				else {
					throw new UnsupportedOperationException(types.toString());
				}
			}

			if(currentNode instanceof SingletonSet) {
				currentNode.replaceWith(node);
			}
			else if(currentNode != null) {
				Join join = new Join();
				currentNode.replaceWith(join);
				join.setLeftArg(currentNode);
				join.setRightArg(node);
				node = join;
			}
		}

		private void visitFilter(Resource r)
				throws OpenRDFException
		{
			Value expr = Statements.singleValue(r, SP.EXPRESSION_PROPERTY, store);
			ValueExpr valueExpr = visitExpression(expr);
			TupleExpr currentNode = node;
			node = new SingletonSet();
			Filter filter = new Filter(node, valueExpr);
			currentNode.replaceWith(filter);
		}

		private void visitBind(Resource r)
				throws OpenRDFException
		{
			Value expr = Statements.singleValue(r, SP.EXPRESSION_PROPERTY, store);
			ValueExpr valueExpr = visitExpression(expr);
			Value varValue = Statements.singleValue(r, SP.VARIABLE_PROPERTY, store);
			if(!(varValue instanceof Resource)) {
				throw new MalformedSPINException("Value of "+SP.VARIABLE_PROPERTY+" is not a resource");
			}
			String varName = getVarName((Resource)varValue);
			TupleExpr currentNode = node;
			node = new SingletonSet();
			Extension extension = new Extension(node, new ExtensionElem(valueExpr, varName));
			currentNode.replaceWith(extension);
		}

		private ValueExpr visitExpression(Value v)
				throws OpenRDFException
		{
			ValueExpr expr;
			if(v instanceof Literal) {
				expr = new ValueConstant(v);
			}
			else {
				Resource r = (Resource) v;
				Set<URI> exprTypes = Iterations.asSet(Statements.getObjectURIs(r, RDF.TYPE, store));

				URI func;
				if(exprTypes.size() > 1) {
					func = null;
					if(exprTypes.remove(SPIN.FUNCTIONS_CLASS)) {
						exprTypes.remove(SPIN.MODULES_CLASS);
						exprTypes.remove(RDFS.RESOURCE);
						for(URI f : exprTypes) {
							Value abstractValue = Statements.singleValue(f, SPIN.ABSTRACT_PROPERTY, store);
							if(abstractValue == null || BooleanLiteralImpl.FALSE.equals(abstractValue)) {
								func = f;
								break;
							}
						}
						if(func == null) {
							throw new MalformedSPINException("Function missing RDF type: "+r);
						}
					}
					else if(exprTypes.contains(SP.VARIABLE_CLASS)) {
						func = null;
					}
					else {
						throw new MalformedSPINException("Expression missing RDF type: "+r);
					}
				}
				else if(exprTypes.size() == 1) {
					func = exprTypes.iterator().next();
				}
				else {
					func = null;
				}

				if(func != null) {
					SortedMap<URI, ValueExpr> argValues = new TreeMap<URI, ValueExpr>(new Comparator<URI>()
					{
						@Override
						public int compare(URI uri1, URI uri2) {
							return uri1.getLocalName().compareTo(uri2.getLocalName());
						}
					});
					CloseableIteration<? extends Statement, QueryEvaluationException> iter = store.getStatements(r, null, null);
					try {
						while(iter.hasNext()) {
							Statement stmt = iter.next();
							URI argName = stmt.getPredicate();
							if(!NON_ARG_PROPERTIES.contains(argName)) {
								ValueExpr argValue = visitExpression(stmt.getObject());
								argValues.put(argName, argValue);
							}
						}
					}
					finally {
						iter.close();
					}

					int numArgs = argValues.size();
					List<ValueExpr> args = new ArrayList<ValueExpr>(numArgs);
					for(int i=0; i<numArgs; i++) {
						ValueExpr argExpr = argValues.remove(toArgProperty(i));
						if(argExpr == null) {
							argExpr = argValues.remove(argValues.firstKey());
						}
						args.add(argExpr);
					}

					expr = toValueExpr(r, func, args);
				}
				else {
					String varName = getVarName(r);
					if(varName != null) {
						expr = createVar(varName);
					}
					else {
						expr = new ValueConstant(v);
					}
				}
			}
			return expr;
		}

		private URI toArgProperty(int i) {
			switch(i) {
			case 1: return SP.ARG1_PROPERTY;
			case 2: return SP.ARG2_PROPERTY;
			case 3: return SP.ARG3_PROPERTY;
			case 4: return SP.ARG4_PROPERTY;
			case 5: return SP.ARG5_PROPERTY;
			default:
				return ValueFactoryImpl.getInstance().createURI(SP.NAMESPACE, "arg"+i);
			}
		}

		private ValueExpr toValueExpr(Resource r, URI func, List<ValueExpr> args)
			throws OpenRDFException
		{
			ValueExpr expr;
			CompareOp compareOp;
			MathOp mathOp;
			if((compareOp = toCompareOp(func)) != null) {
				if(args.size() != 2) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new Compare(args.get(0), args.get(1), compareOp);
			}
			else if((mathOp = toMathOp(func)) != null) {
				if(args.size() != 2) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new MathExpr(args.get(0), args.get(1), mathOp);
			}
			else if(SP.NOT.equals(func)) {
				if(args.size() != 1) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new Not(args.get(0));
			}
			else if(SP.EXISTS.equals(func)) {
				Value elements = Statements.singleValue(r, SP.ELEMENTS_PROPERTY, store);
				if(!(elements instanceof Resource)) {
					throw new MalformedSPINException("Value of "+SP.ELEMENTS_PROPERTY+" is not a resource");
				}
				TupleExpr currentNode = node;
				node = new SingletonSet();
				expr = new Exists(node);
				visitGroupGraphPattern((Resource) elements);
				node = currentNode;
			}
			else if(SP.NOT_EXISTS.equals(func)) {
				Value elements = Statements.singleValue(r, SP.ELEMENTS_PROPERTY, store);
				if(!(elements instanceof Resource)) {
					throw new MalformedSPINException("Value of "+SP.ELEMENTS_PROPERTY+" is not a resource");
				}
				TupleExpr currentNode = node;
				node = new SingletonSet();
				expr = new Not(new Exists(node));
				visitGroupGraphPattern((Resource) elements);
				node = currentNode;
			}
			else {
				String funcName = wellKnownFunctions.apply(func);
				if(funcName == null) {
					funcName = func.stringValue();
				}
				expr = new FunctionCall(funcName, args);
			}
			return expr;
		}

		private CompareOp toCompareOp(URI func) {
			if(SP.EQ.equals(func)) {
				return CompareOp.EQ;
			}
			else if(SP.NE.equals(func)) {
				return CompareOp.NE;
			}
			else if(SP.LT.equals(func)) {
				return CompareOp.LT;
			}
			else if(SP.LE.equals(func)) {
				return CompareOp.LE;
			}
			else if(SP.GE.equals(func)) {
				return CompareOp.GE;
			}
			else if(SP.GT.equals(func)) {
				return CompareOp.GT;
			}
			else {
				return null;
			}
		}

		private MathOp toMathOp(URI func) {
			if(SP.ADD.equals(func)) {
				return MathOp.PLUS;
			}
			else if(SP.SUB.equals(func)) {
				return MathOp.MINUS;
			}
			else if(SP.MUL.equals(func)) {
				return MathOp.MULTIPLY;
			}
			else if(SP.DIVIDE.equals(func)) {
				return MathOp.DIVIDE;
			}
			else {
				return null;
			}
		}

		private String getVarName(Resource r)
				throws OpenRDFException
		{
			// have we already seen it
			String varName = vars.get(r);
			// is it well-known
			if(varName == null && r instanceof URI) {
				varName = wellKnownVars.apply((URI) r);
				if(varName != null) {
					vars.put(r, varName);
				}
			}
			if(varName == null) {
				// check for a varName statement
				Value nameValue = Statements.singleValue(r, SP.VAR_NAME_PROPERTY, store);
				if(nameValue instanceof Literal) {
					varName = ((Literal)nameValue).getLabel();
					if(varName != null) {
						vars.put(r, varName);
					}
				}
				else if(nameValue != null) {
					throw new MalformedSPINException("Value of "+SP.VAR_NAME_PROPERTY+" is not a literal");
				}
			}
			return varName;
		}

		private Var getVar(Value v)
			throws OpenRDFException
		{
			Var var = null;
			if(v instanceof Resource) {
				String varName = getVarName((Resource) v);
				if(varName != null) {
					var = createVar(varName);
				}
			}

			if(var == null) {
				// it must be a constant then
				var = createConstVar(v);
			}

			return var;
		}

		private Var createVar(String varName) {
			if(extElems != null) {
				extElems.remove(varName);
			}
			return new Var(varName);
		}

		private Var createConstVar(Value value) {
			if (value == null) {
				throw new IllegalArgumentException("value can not be null");
			}

			String varName = getConstVarName(value);
			Var var = new Var(varName);
			var.setConstant(true);
			var.setAnonymous(true);
			var.setValue(value);
			return var;
		}

		private String getConstVarName(Value value) {
			// We use toHexString to get a more compact stringrep.
			String uniqueStringForValue = Integer.toHexString(value.stringValue().hashCode());

			if (value instanceof Literal) {
				uniqueStringForValue += "-lit";

				// we need to append datatype and/or language tag to ensure a unique
				// var name (see SES-1927)
				Literal lit = (Literal)value;
				if (lit.getDatatype() != null) {
					uniqueStringForValue += "-" + lit.getDatatype().stringValue();
				}
				if (lit.getLanguage() != null) {
					uniqueStringForValue += "-" + lit.getLanguage();
				}
			}
			else if (value instanceof BNode) {
				uniqueStringForValue += "-node";
			}
			else {
				uniqueStringForValue += "-uri";
			}
			return "_const-" + uniqueStringForValue;
		}
	}
}
