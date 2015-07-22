package org.openrdf.sail.spin;

import info.aduna.iteration.CloseableIteration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
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
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.fc.AbstractForwardChainingInferencerConnection;
import org.openrdf.sail.inferencer.util.RDFInferencerInserter;

public class SPINSailConnection extends AbstractForwardChainingInferencerConnection {

	private static final URI AXIOM_CONTEXT = ValueFactoryImpl.getInstance().createURI("local:axioms");

	private final ValueFactory vf;

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

	/**
	 * update spin:rules modify existing (non-inferred) statements directly.
	 * spin:constructors should be run after spin:rules for each subject of an
	 * RDF.TYPE statement.
	 */
	@Override
	protected int applyRules(Model iteration)
		throws SailException
	{
		int count = 0;
		for (Resource subj : iteration.subjects()) {
			// get rule properties
			Set<Resource> ruleProps = getSubjects(RDFS.SUBPROPERTYOF, SPIN.RULE_PROPERTY);
			// get classes
			Set<Value> classes = getObjects(subj, RDF.TYPE);
			// classes that have rules
			List<Resource> classesWithRules = getClassesOf(subj, ruleProps);
			// class hierarchy
			for(Resource cls : classesWithRules) {
				List<Resource> rules = getRules(cls, ruleProps);
				for(Resource rule : rules) {
					count += executeRule(rule);
				}
			}
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

	private List<Resource> getClassesOf(Resource res, Set<Resource> props)
		throws SailException
	{
		Set<Value> classes = getObjects(res, RDF.TYPE);
		return null;
	}

	private Set<Resource> getSubjects(URI prop, Value obj)
		throws SailException
	{
		final Set<Resource> subjs = new HashSet<Resource>();
		getStatements(null, prop, obj, new Handler<Statement>()
		{
			@Override
			public void handle(Statement t) {
				subjs.add(t.getSubject());
			}
		});
		return subjs;
	}

	private Set<Value> getObjects(Resource subj, URI prop)
		throws SailException
	{
		final Set<Value> objs = new HashSet<Value>();
		getStatements(subj, prop, null, new Handler<Statement>()
		{
			@Override
			public void handle(Statement t) {
				objs.add(t.getObject());
			}
		});
		return objs;
	}

	private void getStatements(Resource subj, URI prop, Value obj, Handler<Statement> handler)
		throws SailException
	{
		CloseableIteration<? extends Statement, SailException> iter = getStatements(null, prop,
				obj, true);
		try {
			while(iter.hasNext()) {
				handler.handle(iter.next());
			}
		}
		finally {
			iter.close();
		}
	}

	interface Handler<T>
	{
		void handle(T t);
	}
}
