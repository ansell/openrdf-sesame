package org.openrdf.sail.spin;

import info.aduna.iteration.CloseableIteration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.StatementSource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.Statements;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedOperation;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.fc.AbstractForwardChainingInferencerConnection;
import org.openrdf.sail.inferencer.util.RDFInferencerInserter;
import org.openrdf.spin.MalformedSPINException;
import org.openrdf.spin.RuleProperty;
import org.openrdf.spin.SPINParser;

public class SPINSailConnection extends AbstractForwardChainingInferencerConnection implements
		StatementSource<SailException>
{

	private static final URI AXIOM_CONTEXT = ValueFactoryImpl.getInstance().createURI("local:axioms");

	private final ValueFactory vf;

	private final SPINParser parser = new SPINParser();

	private final Object rulePropertyLock = new Object();

	private List<URI> orderedRuleProperties;

	private Map<URI, RuleProperty> rulePropertyMap;

	public SPINSailConnection(Sail sail, InferencerConnection con) {
		super(sail, con);
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

	@Override
	protected Model createModel() {
		return new TreeModel();
	}

	@Override
	protected void resetInferred()
		throws SailException
	{
		List<Resource> contexts = new ArrayList<Resource>();
		CloseableIteration<? extends Resource, SailException> iter = getContextIDs();
		while (iter.hasNext()) {
			Resource ctx = iter.next();
			if (!AXIOM_CONTEXT.equals(ctx)) {
				contexts.add(ctx);
			}
		}
		clearInferred(contexts.toArray(new Resource[contexts.size()]));
	}

	@Override
	protected void addAxiomStatements()
		throws SailException
	{

		RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
		loadAxiomStatements(parser, "/schema/sp.ttl", this);
		loadAxiomStatements(parser, "/schema/spin.ttl", this);
		loadAxiomStatements(parser, "/schema/spl.spin.ttl", this);
	}

	private void loadAxiomStatements(RDFParser parser, String file, InferencerConnection con)
		throws SailException
	{
		RDFInferencerInserter inserter = new RDFInferencerInserter(con, vf);
		inserter.enforceContext(AXIOM_CONTEXT);
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
			// get rule properties
			List<URI> ruleProps = getRuleProperties();
			Map<Resource, Map<URI, Resource>> rulesByClass = getRulesForSubject(subj, ruleProps);

			// build class hierarchy
			// TODO
			Collection<Resource> classHierarchy = rulesByClass.keySet();

			// execute rules
			for (Resource cls : classHierarchy) {
				Map<URI, Resource> rules = rulesByClass.get(cls);
				for (Map.Entry<URI, Resource> ruleEntry : rules.entrySet()) {
					nofInferred += executeRule(subj, ruleEntry.getValue(), getRuleProperty(ruleEntry.getKey()));
				}
			}
		}
		return nofInferred;
	}

	private int executeRule(Resource subj, Resource rule, RuleProperty ruleProp)
		throws OpenRDFException
	{
		int nofInferred;
		ParsedOperation query = parser.parse(rule, this);
		if(query instanceof ParsedGraphQuery) {
			ParsedGraphQuery graphQuery = (ParsedGraphQuery) query;
			// TODO
			nofInferred = 0;
		}
		else if(query instanceof ParsedUpdate) {
			ParsedUpdate update = (ParsedUpdate) query;
			// TODO
			nofInferred = 0;
		}
		else {
			throw new MalformedSPINException("Invalid rule: "+rule);
		}

		return nofInferred;
	}

	private Map<Resource, Map<URI, Resource>> getRulesForSubject(Resource subj, List<URI> ruleProps)
		throws OpenRDFException
	{
		Map<Resource, Map<URI, Resource>> rulesByClass = new HashMap<Resource, Map<URI, Resource>>();
		// check each class of subj for rule properties
		CloseableIteration<? extends Resource, ? extends OpenRDFException> classIter = Statements.getObjectResources(
				subj, RDF.TYPE, this);
		try {
			while (classIter.hasNext()) {
				Resource cls = classIter.next();
				Map<URI, Resource> classRulesByProperty = getRulesForClass(cls, ruleProps);
				if (!classRulesByProperty.isEmpty()) {
					rulesByClass.put(cls, classRulesByProperty);
				}
			}
		}
		finally {
			classIter.close();
		}
		return rulesByClass;
	}

	private Map<URI, Resource> getRulesForClass(Resource cls, List<URI> ruleProps)
		throws OpenRDFException
	{
		Map<URI, Resource> classRulesByProperty = new HashMap<URI, Resource>();
		for (URI ruleProp : ruleProps) {
			CloseableIteration<? extends Resource, ? extends OpenRDFException> ruleIter = Statements.getObjectResources(
					cls, ruleProp, this);
			try {
				while (ruleIter.hasNext()) {
					classRulesByProperty.put(ruleProp, ruleIter.next());
				}
			}
			finally {
				ruleIter.close();
			}
		}
		return classRulesByProperty;
	}

	@Override
	public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, URI pred,
			Value obj, Resource... contexts)
		throws SailException
	{
		return getStatements(subj, pred, obj, true, contexts);
	}
}
