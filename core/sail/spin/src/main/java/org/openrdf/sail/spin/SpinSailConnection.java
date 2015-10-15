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
package org.openrdf.sail.spin;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iterations;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Dataset;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.Operation;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.Update;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolverBase;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.query.algebra.evaluation.function.TupleFunction;
import org.openrdf.query.algebra.evaluation.function.TupleFunctionRegistry;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.openrdf.query.algebra.evaluation.impl.ConstantOptimizer;
import org.openrdf.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStatistics;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.impl.FilterOptimizer;
import org.openrdf.query.algebra.evaluation.impl.IterativeEvaluationOptimizer;
import org.openrdf.query.algebra.evaluation.impl.OrderLimitOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryJoinOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryModelNormalizer;
import org.openrdf.query.algebra.evaluation.impl.SameTermFilterOptimizer;
import org.openrdf.query.algebra.evaluation.impl.TupleFunctionEvaluationStrategy;
import org.openrdf.query.algebra.evaluation.util.Statements;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedOperation;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailConnectionQueryPreparer;
import org.openrdf.sail.SailException;
import org.openrdf.sail.evaluation.SailTripleSource;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.fc.AbstractForwardChainingInferencerConnection;
import org.openrdf.sail.inferencer.util.RDFInferencerInserter;
import org.openrdf.sail.spin.SpinSail.EvaluationMode;
import org.openrdf.spin.ConstraintViolation;
import org.openrdf.spin.ConstraintViolationRDFHandler;
import org.openrdf.spin.MalformedSpinException;
import org.openrdf.spin.ParsedTemplate;
import org.openrdf.spin.QueryContext;
import org.openrdf.spin.RuleProperty;
import org.openrdf.spin.SpinParser;
import org.openrdf.spin.function.TransientFunction;
import org.openrdf.spin.function.TransientTupleFunction;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

class SpinSailConnection extends AbstractForwardChainingInferencerConnection {

	private static final String THIS_VAR = "this";

	private static final URI EXECUTED = ValueFactoryImpl.getInstance().createURI("http://www.openrdf.org/schema/spin#executed");

	private static final Marker constraintViolationMarker = MarkerFactory.getMarker("ConstraintViolation");

	private static final String CONSTRAINT_VIOLATION_MESSAGE = "Constraint violation: {}: {} {} {}";

	private final EvaluationMode evaluationMode;

	private final FunctionRegistry functionRegistry;

	private final TupleFunctionRegistry tupleFunctionRegistry;

	private final FederatedServiceResolverBase serviceResolver;

	private final ValueFactory vf;

	private final TripleSource tripleSource;

	private final SpinParser parser;

	private List<URI> orderedRuleProperties;

	private Map<URI, RuleProperty> rulePropertyMap;

	private Map<Resource,Executions> ruleExecutions;

	private Map<Resource,Set<Resource>> classToSubclassMap;
	
	private SailConnectionQueryPreparer queryPreparer;

	public SpinSailConnection(SpinSail sail, InferencerConnection con) {
		super(sail, con);
		this.evaluationMode = sail.getEvaluationMode();
		this.functionRegistry = sail.getFunctionRegistry();
		this.tupleFunctionRegistry = sail.getTupleFunctionRegistry();
		this.vf = sail.getValueFactory();
		this.parser = sail.getSpinParser();
		this.tripleSource = new SailTripleSource(getWrappedConnection(), true, vf);
		this.queryPreparer = new SailConnectionQueryPreparer(this, true, tripleSource);

		if(evaluationMode == EvaluationMode.SERVICE) {
			FederatedServiceResolver resolver = sail.getFederatedServiceResolver();
			if(!(resolver instanceof FederatedServiceResolverBase)) {
				throw new IllegalArgumentException("SERVICE EvaluationMode requires a FederatedServiceResolver that is an instance of "+FederatedServiceResolverBase.class.getName());
			}
			this.serviceResolver = (FederatedServiceResolverBase) resolver;
		}
		else {
			this.serviceResolver = null;
		}

		con.addConnectionListener(new SubclassListener());
		con.addConnectionListener(new RulePropertyListener());
		con.addConnectionListener(new InvalidationListener());

		QueryContext.begin(queryPreparer);
	}

	public void setParserConfig(ParserConfig parserConfig) {
		queryPreparer.setParserConfig(parserConfig);
	}

	public ParserConfig getParserConfig() {
		return queryPreparer.getParserConfig();
	}

	@Override
	public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(TupleExpr tupleExpr,
			Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		logger.trace("Incoming query model:\n{}", tupleExpr);

		// Clone the tuple expression to allow for more aggresive optimizations
		tupleExpr = tupleExpr.clone();

		if (!(tupleExpr instanceof QueryRoot)) {
			// Add a dummy root node to the tuple expressions to allow the
			// optimizers to modify the actual root node
			tupleExpr = new QueryRoot(tupleExpr);
		}

		new SpinFunctionInterpreter(parser, tripleSource, functionRegistry).optimize(tupleExpr, dataset, bindings);
		new SpinMagicPropertyInterpreter(parser, tripleSource, tupleFunctionRegistry, serviceResolver).optimize(tupleExpr, dataset, bindings);

		logger.trace("SPIN query model:\n{}", tupleExpr);

		if(evaluationMode == EvaluationMode.TRIPLE_SOURCE) {
			EvaluationStrategy strategy = new TupleFunctionEvaluationStrategy(
					new EvaluationStrategyImpl(new SailTripleSource(this, includeInferred, vf), dataset, serviceResolver),
					vf,
					tupleFunctionRegistry);

			// do standard optimizations
			new BindingAssigner().optimize(tupleExpr, dataset, bindings);
			new ConstantOptimizer(strategy).optimize(tupleExpr, dataset, bindings);
			new CompareOptimizer().optimize(tupleExpr, dataset, bindings);
			new ConjunctiveConstraintSplitter().optimize(tupleExpr, dataset, bindings);
			new DisjunctiveConstraintOptimizer().optimize(tupleExpr, dataset, bindings);
			new SameTermFilterOptimizer().optimize(tupleExpr, dataset, bindings);
			new QueryModelNormalizer().optimize(tupleExpr, dataset, bindings);
			new QueryJoinOptimizer(new EvaluationStatistics()).optimize(tupleExpr, dataset, bindings);
			// new SubSelectJoinOptimizer().optimize(tupleExpr, dataset, bindings);
			new IterativeEvaluationOptimizer().optimize(tupleExpr, dataset, bindings);
			new FilterOptimizer().optimize(tupleExpr, dataset, bindings);
			new OrderLimitOptimizer().optimize(tupleExpr, dataset, bindings);

			logger.trace("Optimized query model:\n{}", tupleExpr);

			try {
				return strategy.evaluate(tupleExpr, bindings);
			}
			catch(QueryEvaluationException e) {
				throw new SailException(e);
			}
		}
		else {
			return super.evaluate(tupleExpr, dataset, bindings, includeInferred);
		}
	}

	@Override
	public void close()
		throws SailException
	{
		super.close();
		QueryContext.end();
	}

	private void initRuleProperties()
		throws OpenRDFException
	{
		if(rulePropertyMap != null) {
			return;
		}

		rulePropertyMap = parser.parseRuleProperties(tripleSource);
		// order rules
		Set<URI> remainingRules = new HashSet<URI>(rulePropertyMap.keySet());
		List<URI> reverseOrder = new ArrayList<URI>(remainingRules.size());
		while(!remainingRules.isEmpty()) {
			for(Iterator<URI> ruleIter = remainingRules.iterator(); ruleIter.hasNext(); ) {
				URI rule = ruleIter.next();
				boolean isTerminal = true;
				RuleProperty ruleProperty = rulePropertyMap.get(rule);
				if(ruleProperty != null) {
					List<URI> nextRules = ruleProperty.getNextRules();
					for(URI nextRule : nextRules) {
						if(!nextRule.equals(rule) && remainingRules.contains(nextRule)) {
							isTerminal = false;
							break;
						}
					}
				}
				if(isTerminal) {
					reverseOrder.add(rule);
					ruleIter.remove();
				}
			}
		}
		orderedRuleProperties = Lists.reverse(reverseOrder);
	}

	private void resetRuleProperties() {
		orderedRuleProperties = null;
		rulePropertyMap = null;
	}

	private List<URI> getRuleProperties()
		throws OpenRDFException
	{
		initRuleProperties();
		return orderedRuleProperties;
	}

	private RuleProperty getRuleProperty(URI ruleProp)
		throws OpenRDFException
	{
		initRuleProperties();
		return rulePropertyMap.get(ruleProp);
	}

	private void initSubclasses()
		throws OpenRDFException
	{
		if(classToSubclassMap != null) {
			return;
		}

		classToSubclassMap = new HashMap<Resource,Set<Resource>>();
		CloseableIteration<? extends Statement, QueryEvaluationException> stmtIter = tripleSource.getStatements(null, RDFS.SUBCLASSOF, null);
		try {
			while(stmtIter.hasNext()) {
				Statement stmt = stmtIter.next();
				if(stmt.getObject() instanceof Resource) {
					Resource child = stmt.getSubject();
					Resource parent = (Resource) stmt.getObject();
					Set<Resource> children = getSubclasses(parent);
					if(children == null) {
						children = new HashSet<Resource>(64);
						classToSubclassMap.put(parent, children);
					}
					children.add(child);
				}
			}
		}
		finally {
			stmtIter.close();
		}
	}

	private void resetSubclasses() {
		classToSubclassMap = null;
	}

	private Set<Resource> getSubclasses(Resource cls)
		throws OpenRDFException
	{
		initSubclasses();
		return classToSubclassMap.get(cls);
	}

	@Override
	protected Model createModel() {
		return new TreeModel();
	}

	@Override
	protected void addAxiomStatements()
		throws SailException
	{
		RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
		loadAxiomStatements(parser, "/schema/sp.ttl");
		loadAxiomStatements(parser, "/schema/spin.ttl");
		loadAxiomStatements(parser, "/schema/spl.spin.ttl");
	}

	private void loadAxiomStatements(RDFParser parser, String file)
		throws SailException
	{
		RDFInferencerInserter inserter = new RDFInferencerInserter(this, vf);
		parser.setRDFHandler(inserter);
		URL url = getClass().getResource(file);
		try {
			InputStream in = new BufferedInputStream(url.openStream());
			try {
				parser.parse(in, url.toString());
			}
			finally {
				in.close();
			}
		}
		catch (IOException ioe) {
			throw new SailException(ioe);
		}
		catch (OpenRDFException e) {
			throw new SailException(e);
		}
	}

	@Override
	protected void doInferencing()
		throws SailException
	{
		ruleExecutions = new HashMap<Resource,Executions>();
		super.doInferencing();
		ruleExecutions = null;
	}

	@Override
	protected int applyRules(Model iteration)
		throws SailException
	{
		try {
			return applyRulesInternal(iteration);
		}
		catch (SailException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new SailException(e);
		}
	}

	/**
	 * update spin:rules modify existing (non-inferred) statements directly.
	 * spin:constructors should be run after spin:rules for each subject of an
	 * RDF.TYPE statement.
	 */
	private int applyRulesInternal(Model iteration)
		throws OpenRDFException
	{
		int nofInferred = 0;
		for (Resource subj : iteration.subjects()) {
			List<Resource> classes = getClasses(subj);
			// build local class hierarchy
			Set<Resource> remainingClasses = new HashSet<Resource>(classes);
			List<Resource> classHierarchy = new ArrayList<Resource>(remainingClasses.size());
			while(!remainingClasses.isEmpty()) {
				for(Iterator<Resource> clsIter = remainingClasses.iterator(); clsIter.hasNext(); ) {
					Resource cls = clsIter.next();
					Set<Resource> children = getSubclasses(cls);
					boolean isTerminal = true;
					if(children != null) {
						for(Resource child : remainingClasses) {
							if(!child.equals(cls) && children.contains(child)) {
								isTerminal = false;
								break;
							}
						}
					}
					if(isTerminal) {
						classHierarchy.add(cls);
						clsIter.remove();
					}
				}
			}

			nofInferred += executeRules(subj, classHierarchy);
			nofInferred += executeConstructors(subj, classHierarchy);
			checkConstraints(subj, classHierarchy);
		}
		return nofInferred;
	}

	private List<Resource> getClasses(Resource subj)
		throws QueryEvaluationException
	{
		List<Resource> classes = new ArrayList<Resource>();
		CloseableIteration<? extends Resource, QueryEvaluationException> classIter = Statements.getObjectResources(subj,
				RDF.TYPE, tripleSource);
		Iterations.addAll(classIter, classes);
		return classes;
	}

	private int executeConstructors(Resource subj, List<Resource> classHierarchy)
		throws OpenRDFException
	{
		int nofInferred = 0;
		Set<Resource> constructed = new HashSet<Resource>(classHierarchy.size());
		CloseableIteration<? extends Resource, QueryEvaluationException> classIter = Statements.getObjectResources(subj,
				EXECUTED, tripleSource);
		Iterations.addAll(classIter, constructed);

		for(Resource cls : classHierarchy) {
			List<Resource> constructors = getConstructorsForClass(cls);
			for(Resource constructor : constructors) {
				if(constructed.add(constructor)) {
					nofInferred += executeRule(subj, constructor);
					addInferredStatement(subj, EXECUTED, constructor);
				}
			}
		}
		return nofInferred;
	}

	private List<Resource> getConstructorsForClass(Resource cls)
		throws OpenRDFException
	{
		List<Resource> constructors = new ArrayList<Resource>(2);
		CloseableIteration<? extends Resource, QueryEvaluationException> constructorIter = Statements.getObjectResources(cls,
				SPIN.CONSTRUCTOR_PROPERTY, tripleSource);
		Iterations.addAll(constructorIter, constructors);
		return constructors;
	}

	private int executeRules(Resource subj, List<Resource> classHierarchy)
		throws OpenRDFException
	{
		int nofInferred = 0;
		// get rule properties
		List<URI> ruleProps = getRuleProperties();

		// check each class of subj for rule properties
		for (Resource cls : classHierarchy) {
			Map<URI, List<Resource>> classRulesByProperty = getRulesForClass(cls, ruleProps);
			if (!classRulesByProperty.isEmpty()) {
				// execute rules
				for (Map.Entry<URI, List<Resource>> ruleEntry : classRulesByProperty.entrySet()) {
					RuleProperty ruleProperty = getRuleProperty(ruleEntry.getKey());
					int maxCount = ruleProperty.getMaxIterationCount();
					for (Resource rule : ruleEntry.getValue()) {
						Executions executions = null;
						if(maxCount != -1) {
							executions = ruleExecutions.get(rule);
							if(executions == null) {
								executions = new Executions();
								ruleExecutions.put(rule, executions);
							}
							if(executions.count >= maxCount) {
								continue;
							}
						}
						nofInferred += executeRule(subj, rule);
						if(executions != null) {
							executions.count++;
						}
					}
				}
			}
		}
		return nofInferred;
	}

	private int executeRule(Resource subj, Resource rule)
		throws OpenRDFException
	{
		int nofInferred;
		ParsedOperation parsedOp = parser.parse(rule, tripleSource);
		if (parsedOp instanceof ParsedGraphQuery) {
			ParsedGraphQuery graphQuery = (ParsedGraphQuery)parsedOp;
			GraphQuery queryOp = queryPreparer.prepare(graphQuery);
			addBindings(subj, rule, graphQuery, queryOp);
			CountingRDFInferencerInserter handler = new CountingRDFInferencerInserter(this, vf);
			queryOp.evaluate(handler);
			nofInferred = handler.getStatementCount();
		}
		else if (parsedOp instanceof ParsedUpdate) {
			ParsedUpdate graphUpdate = (ParsedUpdate)parsedOp;
			Update updateOp = queryPreparer.prepare(graphUpdate);
			addBindings(subj, rule, graphUpdate, updateOp);
			UpdateCountListener listener = new UpdateCountListener();
			addConnectionListener(listener);
			updateOp.execute();
			removeConnectionListener(listener);
			// number of statement changes
			nofInferred = listener.getAddedStatementCount() + listener.getRemovedStatementCount();
		}
		else {
			throw new MalformedSpinException("Invalid rule: " + rule);
		}

		return nofInferred;
	}

	/**
	 * @return Map with rules in execution order.
	 */
	private Map<URI, List<Resource>> getRulesForClass(Resource cls, List<URI> ruleProps)
		throws QueryEvaluationException
	{
		// NB: preserve ruleProp order!
		Map<URI, List<Resource>> classRulesByProperty = new LinkedHashMap<URI, List<Resource>>(ruleProps.size());
		for (URI ruleProp : ruleProps) {
			List<Resource> rules = new ArrayList<Resource>(2);
			CloseableIteration<? extends Resource, QueryEvaluationException> ruleIter = Statements.getObjectResources(cls,
					ruleProp, tripleSource);
			Iterations.addAll(ruleIter, rules);
			if (!rules.isEmpty()) {
				if (rules.size() > 1) {
					// sort by comments
					final Map<Resource, String> comments = new HashMap<Resource, String>(rules.size());
					for (Resource rule : rules) {
						String comment = getHighestComment(rule);
						if (comment != null) {
							comments.put(rule, comment);
						}
					}
					Collections.sort(rules, new Comparator<Resource>() {

						@Override
						public int compare(Resource rule1, Resource rule2) {
							String comment1 = comments.get(rule1);
							String comment2 = comments.get(rule2);
							if (comment1 != null && comment2 != null) {
								return comment1.compareTo(comment2);
							}
							else if (comment1 != null && comment2 == null) {
								return 1;
							}
							else if (comment1 == null && comment2 != null) {
								return -1;
							}
							else {
								return 0;
							}
						}
					});
				}
				classRulesByProperty.put(ruleProp, rules);
			}
		}
		return classRulesByProperty;
	}

	private String getHighestComment(Resource subj)
		throws QueryEvaluationException
	{
		String comment = null;
		CloseableIteration<? extends Literal, QueryEvaluationException> iter = Statements.getObjectLiterals(subj,
				RDFS.COMMENT, tripleSource);
		try {
			while (iter.hasNext()) {
				Literal l = iter.next();
				String label = l.getLabel();
				if ((comment != null && label.compareTo(comment) > 0) || (comment == null)) {
					comment = label;
				}
			}
		}
		finally {
			iter.close();
		}
		return comment;
	}

	private void checkConstraints(Resource subj, List<Resource> classHierarchy)
		throws OpenRDFException
	{
		Map<Resource, List<Resource>> constraintsByClass = getConstraintsForSubject(subj, classHierarchy);

		// check constraints
		for (Map.Entry<Resource, List<Resource>> clsEntry : constraintsByClass.entrySet()) {
			List<Resource> constraints = clsEntry.getValue();
			for (Resource constraint : constraints) {
				checkConstraint(subj, constraint);
			}
		}
	}

	private void checkConstraint(Resource subj, Resource constraint)
		throws OpenRDFException
	{
		ParsedQuery parsedQuery = parser.parseQuery(constraint, tripleSource);
		if (parsedQuery instanceof ParsedBooleanQuery) {
			ParsedBooleanQuery askQuery = (ParsedBooleanQuery)parsedQuery;
			BooleanQuery queryOp = queryPreparer.prepare(askQuery);
			addBindings(subj, constraint, askQuery, queryOp);
			if (!queryOp.evaluate()) {
				ConstraintViolation violation = parser.parseConstraintViolation(constraint, tripleSource);
				handleConstraintViolation(violation);
			}
		}
		else if (parsedQuery instanceof ParsedGraphQuery) {
			ParsedGraphQuery graphQuery = (ParsedGraphQuery)parsedQuery;
			GraphQuery queryOp = queryPreparer.prepare(graphQuery);
			addBindings(subj, constraint, graphQuery, queryOp);
			ConstraintViolationRDFHandler handler = new ConstraintViolationRDFHandler();
			queryOp.evaluate(handler);
			if(handler.getConstraintViolation() != null) {
				handleConstraintViolation(handler.getConstraintViolation());
			}
		}
		else {
			throw new MalformedSpinException("Invalid constraint: " + constraint);
		}
	}

	private void handleConstraintViolation(ConstraintViolation violation)
		throws ConstraintViolationException
	{
		switch (violation.getLevel()) {
			case INFO:
				logger.info(constraintViolationMarker, CONSTRAINT_VIOLATION_MESSAGE, getConstraintViolationLogMessageArgs(violation));
				break;
			case WARNING:
				logger.warn(constraintViolationMarker, CONSTRAINT_VIOLATION_MESSAGE, getConstraintViolationLogMessageArgs(violation));
				break;
			case ERROR:
				logger.error(constraintViolationMarker, CONSTRAINT_VIOLATION_MESSAGE, getConstraintViolationLogMessageArgs(violation));
				throw new ConstraintViolationException(violation);
			case FATAL:
				logger.error(constraintViolationMarker, CONSTRAINT_VIOLATION_MESSAGE, getConstraintViolationLogMessageArgs(violation));
				throw new ConstraintViolationException(violation);
		}
	}

	private Object[] getConstraintViolationLogMessageArgs(ConstraintViolation violation) {
		return new Object[] {
			violation.getMessage() != null ? violation.getMessage() : "No message",
			Strings.nullToEmpty(violation.getRoot()),
			Strings.nullToEmpty(violation.getPath()),
			Strings.nullToEmpty(violation.getValue())
		};
	}

	private Map<Resource, List<Resource>> getConstraintsForSubject(Resource subj, List<Resource> classHierarchy)
		throws QueryEvaluationException
	{
		Map<Resource, List<Resource>> constraintsByClass = new LinkedHashMap<Resource, List<Resource>>(
				classHierarchy.size());
		// check each class of subj for constraints
		for (Resource cls : classHierarchy) {
			List<Resource> constraints = getConstraintsForClass(cls);
			if (!constraints.isEmpty()) {
				constraintsByClass.put(cls, constraints);
			}
		}
		return constraintsByClass;
	}

	private List<Resource> getConstraintsForClass(Resource cls)
		throws QueryEvaluationException
	{
		List<Resource> constraints = new ArrayList<Resource>(2);
		CloseableIteration<? extends Resource, QueryEvaluationException> constraintIter = Statements.getObjectResources(
				cls, SPIN.CONSTRAINT_PROPERTY, tripleSource);
		Iterations.addAll(constraintIter, constraints);
		return constraints;
	}

	private void addBindings(Resource subj, Resource opResource, ParsedOperation parsedOp, Operation op)
		throws OpenRDFException
	{
		if (!parser.isThisUnbound(opResource, tripleSource)) {
			op.setBinding(THIS_VAR, subj);
		}
		if (parsedOp instanceof ParsedTemplate) {
			for(Binding b : ((ParsedTemplate)parsedOp).getBindings()) {
				op.setBinding(b.getName(), b.getValue());
			}
		}
	}



	private class SubclassListener implements SailConnectionListener {

		@Override
		public void statementAdded(Statement st) {
			if(RDFS.SUBCLASSOF.equals(st.getPredicate()) && st.getObject() instanceof Resource) {
				resetSubclasses();
			}
		}

		@Override
		public void statementRemoved(Statement st) {
			if(RDFS.SUBCLASSOF.equals(st.getPredicate())) {
				resetSubclasses();
			}
		}
	}

	private class RulePropertyListener implements SailConnectionListener {

		@Override
		public void statementAdded(Statement st) {
			updateRuleProperties(st);
		}

		@Override
		public void statementRemoved(Statement st) {
			updateRuleProperties(st);
		}

		private void updateRuleProperties(Statement st) {
			boolean changed = false;
			URI pred = st.getPredicate();
			if (RDFS.SUBPROPERTYOF.equals(pred) && SPIN.RULE_PROPERTY.equals(st.getObject())) {
				changed = true;
			}
			else if (SPIN.NEXT_RULE_PROPERTY_PROPERTY.equals(pred)) {
				changed = true;
			}
			else if (SPIN.RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY.equals(pred)) {
				changed = true;
			}
			if (changed) {
				resetRuleProperties();
			}
		}
	}

	private class InvalidationListener implements SailConnectionListener {

		@Override
		public void statementAdded(Statement st) {
			invalidate(st.getSubject());
		}

		@Override
		public void statementRemoved(Statement st) {
			invalidate(st.getSubject());
		}

		private void invalidate(Resource subj) {
			if(subj instanceof URI) {
				parser.reset((URI) subj);
				String key = subj.stringValue();
				Function func = functionRegistry.get(key);
				if(func instanceof TransientFunction) {
					functionRegistry.remove(func);
				}
				TupleFunction tupleFunc = tupleFunctionRegistry.get(key);
				if(tupleFunc instanceof TransientTupleFunction) {
					tupleFunctionRegistry.remove(tupleFunc);
				}
			}
		}
	}



	private static final class Executions {
		int count;
	}

	private static class UpdateCountListener implements SailConnectionListener {

		private int addedCount;

		private int removedCount;

		@Override
		public void statementAdded(Statement st) {
			addedCount++;
		}

		@Override
		public void statementRemoved(Statement st) {
			removedCount++;
		}

		public int getAddedStatementCount() {
			return addedCount;
		}

		public int getRemovedStatementCount() {
			return removedCount;
		}
	}

	private static class CountingRDFInferencerInserter extends RDFInferencerInserter {

		private int stmtCount;

		public CountingRDFInferencerInserter(InferencerConnection con, ValueFactory vf) {
			super(con, vf);
		}

		@Override
		protected void addStatement(Resource subj, URI pred, Value obj, Resource ctxt)
			throws OpenRDFException
		{
			super.addStatement(subj, pred, obj, ctxt);
			stmtCount++;
		}

		public int getStatementCount() {
			return stmtCount;
		}
	}
}
