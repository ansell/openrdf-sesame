package org.openrdf.sail.spin;

import info.aduna.iteration.CloseableIteration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.fc.AbstractForwardChainingInferencerConnection;
import org.openrdf.sail.inferencer.util.RDFInferencerInserter;
import org.openrdf.spin.SPINParser;

public class SPINSailConnection extends AbstractForwardChainingInferencerConnection implements StatementSource<SailException> {

	private static final URI AXIOM_CONTEXT = ValueFactoryImpl.getInstance().createURI("local:axioms");

	private final ValueFactory vf;
	private final SPINParser parser = new SPINParser();

	public SPINSailConnection(Sail sail, InferencerConnection con) {
		super(sail, con);
		this.vf = sail.getValueFactory();
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

	@Override
	protected int applyRules(Model iteration)
		throws SailException
	{
		try {
			return applyRulesInternal(iteration);
		}
		catch(SailException e) {
			throw e;
		}
		catch(OpenRDFException e) {
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
		int count = 0;
		for (Resource subj : iteration.subjects()) {
			// get rule properties
			List<URI> ruleProps = null;
			// get classes
			CloseableIteration<? extends Resource,? extends OpenRDFException> classIter = Statements.getObjectResources(subj, RDF.TYPE, this);
			try {
				while(classIter.hasNext()) {
					Resource cls = classIter.next();
					List<Object> rules = null;
					for(URI ruleProp : ruleProps) {
						CloseableIteration<? extends Resource,? extends OpenRDFException> ruleIter = Statements.getObjectResources(cls, ruleProp, this);
						try {
							while(ruleIter.hasNext()) {
								Resource rule = ruleIter.next();
								//rules.add(new Rule(rule, ruleProp));
							}
						}
						finally {
							ruleIter.close();
						}
					}
					if(!rules.isEmpty()) {
						//rulesByClass.put(cls, rules);
					}
				}
			}
			finally {
				classIter.close();
			}

			// class hierarchy
			/*
			for(Resource cls : classesWithRules) {
				List<Resource> rules = getRules(cls, ruleProps);
				for(Resource rule : rules) {
					count += executeRule(rule);
				}
			}
			*/
		}
		return count;
	}

	private int executeRule(Resource rule)
			throws SailException
	{
		return 0;
	}

	private List<Resource> getRules(Resource cls, Set<Resource> props)
		throws SailException
	{
		return null;
	}

	private List<Resource> getClassesWith(Set<Resource> classes, Set<Resource> props)
		throws SailException
	{
		return null;
	}

	@Override
	public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj,
			URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		return getStatements(subj, pred, obj, true, contexts);
	}
}
