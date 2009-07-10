/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.algebra;

import static org.openrdf.sail.federation.query.QueryModelSerializer.LANGUAGE;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.cursor.Cursor;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.parser.TupleQueryModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.result.TupleResult;
import org.openrdf.sail.federation.evaluation.InsertBindingSetCursor;
import org.openrdf.sail.federation.query.QueryModelSerializer;
import org.openrdf.store.StoreException;

/**
 * Indicates that the argument should be evaluated in a particular member.
 * 
 * @author James Leigh
 */
public class OwnedTupleExpr extends UnaryTupleOperator {

	private final Logger logger = LoggerFactory.getLogger(OwnedTupleExpr.class);

	private final RepositoryConnection owner;

	private final Set<String> bindingNames;

	private TupleQuery preparedQuery;

	public OwnedTupleExpr(RepositoryConnection owner, TupleExpr arg) {
		super(arg);
		this.owner = owner;
		this.bindingNames = arg.getBindingNames();
	}

	public RepositoryConnection getOwner() {
		return owner;
	}

	@Override
	public Set<String> getBindingNames() {
		return bindingNames;
	}

	public void prepare()
		throws StoreException
	{
		assert preparedQuery == null;

		TupleQueryModel model = new TupleQueryModel(getArg());
		String encodedQuery = new QueryModelSerializer().writeQueryModel(model, "");

		try {
			preparedQuery = owner.prepareTupleQuery(LANGUAGE, encodedQuery);
		}
		catch (MalformedQueryException e) {
			logger.warn("Failed to prepare owned query", e);
		}
	}

	public Cursor<BindingSet> evaluate(Dataset dataset, BindingSet bindings)
		throws StoreException
	{
		if (preparedQuery == null) {
			return null;
		}

		synchronized (preparedQuery) {
			preparedQuery.clearBindings();

			for (String name : bindings.getBindingNames()) {
				if (bindingNames.contains(name)) {
					preparedQuery.setBinding(name, bindings.getValue(name));
				}
			}

			preparedQuery.setDataset(dataset);

//			long startTime = System.nanoTime();
			TupleResult result = preparedQuery.evaluate();
//			long endTime = System.nanoTime();
//			System.out.println("Received response in " + (endTime - startTime) / 1000000);
			return new InsertBindingSetCursor(result, bindings);
		}
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meetOther(this);
	}

	@Override
	public String getSignature() {
		return this.getClass().getSimpleName() + " " + owner.toString();
	}

}
