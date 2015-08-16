package org.openrdf.sail.spin;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iterations;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.StatementSource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.util.Statements;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedOperation;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.repository.sail.AbstractSailUpdate;
import org.openrdf.repository.sail.SailBooleanQuery;
import org.openrdf.repository.sail.SailGraphQuery;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.fc.AbstractForwardChainingInferencerConnection;
import org.openrdf.sail.inferencer.util.RDFInferencerInserter;
import org.openrdf.spin.ConstraintViolation;
import org.openrdf.spin.MalformedSPINException;
import org.openrdf.spin.RuleProperty;
import org.openrdf.spin.SPINParser;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

class SPINSailConnection extends AbstractForwardChainingInferencerConnection implements
		StatementSource<SailException>
{

	private static final String THIS_VAR = "this";
	private static final Marker constraintViolationMarker = MarkerFactory.getMarker("ConstraintViolation");
	private static final String CONSTRAINT_VIOLATION_MESSAGE = "{}: {} {} {}";

	private final SPINSail inferencer;

	private final ValueFactory vf;

	private final SPINParser parser = new SPINParser();

	private final Object rulePropertyLock = new Object();

	private List<URI> orderedRuleProperties;

	private Map<URI, RuleProperty> rulePropertyMap;

	private volatile ParserConfig parserConfig = new ParserConfig();

	public SPINSailConnection(SPINSail sail, InferencerConnection con) {
		super(sail, con);
		this.inferencer = sail;
		this.vf = sail.getValueFactory();
		con.addConnectionListener(new SailConnectionListener() {

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
		});
	}

	public void setParserConfig(ParserConfig parserConfig) {
		this.parserConfig = parserConfig;
	}

	public ParserConfig getParserConfig() {
		return parserConfig;
	}

	@Override
	protected Model createModel() {
		return new TreeModel();
	}

	@Override
	protected void resetInferred()
		throws SailException
	{
		Resource axiomContext = inferencer.getAxionContext();
		if(axiomContext != null) {
			// optimised reset
			List<Resource> contexts = new ArrayList<Resource>();
			CloseableIteration<? extends Resource, SailException> iter = getContextIDs();
			while (iter.hasNext()) {
				Resource ctx = iter.next();
				if (!axiomContext.equals(ctx)) {
					contexts.add(ctx);
				}
			}
			clearInferred(contexts.toArray(new Resource[contexts.size()]));
		}
		else {
			super.resetInferred();
		}
	}

	@Override
	protected void addAxiomStatements()
		throws SailException
	{

		RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
		loadAxiomStatements(parser, "/schema/sp.ttl", getWrappedConnection());
		loadAxiomStatements(parser, "/schema/spin.ttl", getWrappedConnection());
		loadAxiomStatements(parser, "/schema/spl.spin.ttl", getWrappedConnection());
	}

	private void loadAxiomStatements(RDFParser parser, String file, InferencerConnection con)
		throws SailException
	{
		RDFInferencerInserter inserter = new RDFInferencerInserter(con, vf);
		Resource axiomContext = inferencer.getAxionContext();
		if(axiomContext != null) {
			inserter.enforceContext(axiomContext);
		}
		parser.setRDFHandler(inserter);
		URL url = getClass().getResource(file);
		try {
			InputStream in = url.openStream();
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

	private void initRuleProperties()
		throws OpenRDFException
	{
		rulePropertyMap = parser.parseRuleProperties(this);
		// order rules
		// TODO
		orderedRuleProperties = new ArrayList<URI>(rulePropertyMap.keySet());
	}

	private void resetRuleProperties() {
		synchronized (rulePropertyLock) {
			orderedRuleProperties = null;
			rulePropertyMap = null;
		}
	}

	private List<URI> getRuleProperties()
		throws OpenRDFException
	{
		synchronized (rulePropertyLock) {
			if (orderedRuleProperties == null) {
				initRuleProperties();
			}
			return orderedRuleProperties;
		}
	}

	private RuleProperty getRuleProperty(URI ruleProp)
		throws OpenRDFException
	{
		synchronized (rulePropertyLock) {
			if (rulePropertyMap == null) {
				initRuleProperties();
			}
			return rulePropertyMap.get(ruleProp);
		}
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
			// build class hierarchy
			// TODO
			List<Resource> classHierarchy = classes;

			nofInferred += executeRules(subj, classHierarchy);
			checkConstraints(subj, classHierarchy);
		}
		return nofInferred;
	}

	private List<Resource> getClasses(Resource subj) throws SailException
	{
		List<Resource> classes = new ArrayList<Resource>();
		CloseableIteration<? extends Resource, SailException> classIter = Statements.getObjectResources(
				subj, RDF.TYPE, this);
		Iterations.addAll(classIter, classes);
		return classes;
	}

	private int executeRules(Resource subj, List<Resource> classHierarchy)
		throws OpenRDFException
	{
		int nofInferred = 0;
		// get rule properties
		List<URI> ruleProps = getRuleProperties();
		Map<Resource, Map<URI, List<Resource>>> rulesByClass = getRulesForSubject(subj, classHierarchy, ruleProps);
		// execute rules
		for (Map.Entry<Resource, Map<URI, List<Resource>>> clsEntry : rulesByClass.entrySet()) {
			Map<URI, List<Resource>> rules = clsEntry.getValue();
			for (Map.Entry<URI, List<Resource>> ruleEntry : rules.entrySet()) {
				RuleProperty ruleProperty = getRuleProperty(ruleEntry.getKey());
				for(Resource rule : ruleEntry.getValue()) {
					nofInferred += executeRule(subj, rule, ruleProperty);
				}
			}
		}
		return nofInferred;
	}

	private int executeRule(Resource subj, Resource rule, RuleProperty ruleProp)
		throws OpenRDFException
	{
		int nofInferred;
		ParsedOperation parsedOp = parser.parse(rule, this);
		if(parsedOp instanceof ParsedGraphQuery) {
			ParsedGraphQuery graphQuery = (ParsedGraphQuery) parsedOp;
			GraphQuery queryOp = new SailGraphQuery(graphQuery, getWrappedConnection(), vf);
			if(!parser.isThisUnbound(rule, this)) {
				queryOp.setBinding(THIS_VAR, subj);
			}
			CountingRDFInferencerInserter handler = new CountingRDFInferencerInserter(getWrappedConnection(), vf);
			queryOp.evaluate(handler);
			nofInferred = handler.getStatementCount();
		}
		else if(parsedOp instanceof ParsedUpdate) {
			ParsedUpdate graphUpdate = (ParsedUpdate) parsedOp;
			Update updateOp = new Update(graphUpdate, getWrappedConnection(), vf, parserConfig);
			if(!parser.isThisUnbound(rule, this)) {
				updateOp.setBinding(THIS_VAR, subj);
			}
			UpdateCountListener listener = new UpdateCountListener();
			getWrappedConnection().addConnectionListener(listener);
			updateOp.execute();
			getWrappedConnection().removeConnectionListener(listener);
			// number of statement changes
			nofInferred = listener.getAddedStatementCount() + listener.getRemovedStatementCount();
		}
		else {
			throw new MalformedSPINException("Invalid rule: "+rule);
		}

		return nofInferred;
	}

	private Map<Resource, Map<URI, List<Resource>>> getRulesForSubject(Resource subj, List<Resource> classHierarchy, List<URI> ruleProps)
		throws SailException
	{
		Map<Resource, Map<URI, List<Resource>>> rulesByClass = new LinkedHashMap<Resource, Map<URI, List<Resource>>>(classHierarchy.size());
		// check each class of subj for rule properties
		for (Resource cls : classHierarchy) {
			Map<URI, List<Resource>> classRulesByProperty = getRulesForClass(cls, ruleProps);
			if (!classRulesByProperty.isEmpty()) {
				rulesByClass.put(cls, classRulesByProperty);
			}
		}
		return rulesByClass;
	}

	/**
	 * @return Map with rules in execution order.
	 */
	private Map<URI, List<Resource>> getRulesForClass(Resource cls, List<URI> ruleProps)
		throws SailException
	{
		// NB: preserve ruleProp order!
		Map<URI, List<Resource>> classRulesByProperty = new LinkedHashMap<URI, List<Resource>>(ruleProps.size());
		for (URI ruleProp : ruleProps) {
			List<Resource> rules = new ArrayList<Resource>(2);
			CloseableIteration<? extends Resource, SailException> ruleIter = Statements.getObjectResources(
					cls, ruleProp, this);
			Iterations.addAll(ruleIter, rules);
			if(!rules.isEmpty()) {
				if(rules.size() > 1) {
					// sort by comments
					final Map<Resource,String> comments = new HashMap<Resource,String>(rules.size());
					for(Resource rule : rules) {
						String comment = getHighestComment(rule);
						if(comment != null) {
							comments.put(rule, comment);
						}
					}
					Collections.sort(rules, new Comparator<Resource>()
					{
						@Override
						public int compare(Resource rule1, Resource rule2) {
							String comment1 = comments.get(rule1);
							String comment2 = comments.get(rule2);
							if(comment1 != null && comment2 != null) {
								return comment1.compareTo(comment2);
							}
							else if(comment1 != null && comment2 == null) {
								return 1;
							}
							else if(comment1 == null && comment2 != null) {
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
		throws SailException
	{
		String comment = null;
		CloseableIteration<? extends Literal,SailException> iter = Statements.getObjectLiterals(subj, RDFS.COMMENT, this);
		try {
			while(iter.hasNext()) {
				Literal l = iter.next();
				String label = l.getLabel();
				if((comment != null && label.compareTo(comment) > 0) || (comment == null)) {
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
		for (Map.Entry<Resource,List<Resource>> clsEntry : constraintsByClass.entrySet()) {
			List<Resource> constraints = clsEntry.getValue();
			for(Resource constraint : constraints) {
				checkConstraint(subj, constraint);
			}
		}
	}

	private void checkConstraint(Resource subj, Resource constraint)
		throws OpenRDFException
	{
		ParsedQuery parsedQuery = parser.parseQuery(constraint, this);
		if(parsedQuery instanceof ParsedBooleanQuery) {
			ParsedBooleanQuery askQuery = (ParsedBooleanQuery) parsedQuery;
			BooleanQuery queryOp = new SailBooleanQuery(askQuery, getWrappedConnection());
			if(!parser.isThisUnbound(constraint, this)) {
				queryOp.setBinding(THIS_VAR, subj);
			}
			if(!queryOp.evaluate()) {
				ConstraintViolation violation = parser.parseConstraintViolation(constraint, this);
				switch(violation.getLevel()) {
					case INFO:
						logger.info(constraintViolationMarker, CONSTRAINT_VIOLATION_MESSAGE, violation.getMessage(), violation.getRoot(), violation.getPath(), violation.getValue());
						break;
					case WARNING:
						logger.warn(constraintViolationMarker, CONSTRAINT_VIOLATION_MESSAGE, violation.getMessage(), violation.getRoot(), violation.getPath(), violation.getValue());
						break;
					case ERROR:
						logger.error(constraintViolationMarker, CONSTRAINT_VIOLATION_MESSAGE, violation.getMessage(), violation.getRoot(), violation.getPath(), violation.getValue());
						throw new ConstraintViolationException(violation);
					case FATAL:
						logger.error(constraintViolationMarker, CONSTRAINT_VIOLATION_MESSAGE, violation.getMessage(), violation.getRoot(), violation.getPath(), violation.getValue());
						throw new ConstraintViolationException(violation);
				}
			}
		}
		else if(parsedQuery instanceof ParsedGraphQuery) {
			// TODO
		}
		else {
			throw new MalformedSPINException("Invalid constraint: "+constraint);
		}
	}

	private Map<Resource, List<Resource>> getConstraintsForSubject(Resource subj, List<Resource> classHierarchy)
		throws SailException
	{
		Map<Resource, List<Resource>> constraintsByClass = new LinkedHashMap<Resource, List<Resource>>(classHierarchy.size());
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
			throws SailException
	{
		List<Resource> constraints = new ArrayList<Resource>(2);
		CloseableIteration<? extends Resource, SailException> constraintIter = Statements.getObjectResources(
					cls, SPIN.CONSTRAINT_PROPERTY, this);
		Iterations.addAll(constraintIter, constraints);
		return constraints;
	}

	@Override
	public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, URI pred,
			Value obj, Resource... contexts)
		throws SailException
	{
		return getStatements(subj, pred, obj, true, contexts);
	}



	static class Update extends AbstractSailUpdate {

		protected Update(ParsedUpdate parsedUpdate, SailConnection con, ValueFactory vf,
				ParserConfig parserConfig)
		{
			super(parsedUpdate, con, vf, parserConfig);
		}

		@Override
		protected boolean isLocalTransaction()
			throws OpenRDFException
		{
			return !getSailConnection().isActive();
		}

		@Override
		protected void beginLocalTransaction()
			throws OpenRDFException
		{
			getSailConnection().begin();
		}

		@Override
		protected void commitLocalTransaction()
			throws OpenRDFException
		{
			getSailConnection().commit();
		}
	}

	
	static class UpdateCountListener implements SailConnectionListener {
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



	static class CountingRDFInferencerInserter extends RDFInferencerInserter {
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
