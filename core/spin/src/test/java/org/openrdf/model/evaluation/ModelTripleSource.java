package org.openrdf.spin;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;


public class ModelTripleSource implements TripleSource {

	private final Model model;

	public ModelTripleSource(Model m) {
		this.model = m;
	}

	@Override
	public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(Resource subj,
			URI pred, Value obj, Resource... contexts)
		throws QueryEvaluationException
	{
		return new CloseableIteratorIteration<Statement,QueryEvaluationException>(model.filter(subj, pred, obj, contexts).iterator());
	}

	@Override
	public ValueFactory getValueFactory() {
		return ValueFactoryImpl.getInstance();
	}

}
