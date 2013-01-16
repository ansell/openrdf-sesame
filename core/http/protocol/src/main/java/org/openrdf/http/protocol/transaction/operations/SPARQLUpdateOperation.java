/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction.operations;

import org.openrdf.query.Binding;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Encapsulation of a SPARQL 1.1 update operation executed as part of a transaction.
 * 
 * @author Jeen Broekstra
 * @since 2.7.0
 */
public class SPARQLUpdateOperation implements TransactionOperation {

	private String updateString;

	private String baseURI;

	private boolean includeInferred;

	private Dataset dataset;

	private Binding[] bindings;

	public SPARQLUpdateOperation() {
		super();
	}
	
	public SPARQLUpdateOperation(String updateString, String baseURI, boolean includeInferred,
			Dataset dataset, Binding... bindings)
	{
		this.setUpdateString(updateString);
		this.setBaseURI(baseURI);
		this.setIncludeInferred(includeInferred);
		this.setDataset(dataset);
		this.setBindings(bindings);
	}

	public void execute(RepositoryConnection con)
		throws RepositoryException
	{
		try {
			Update preparedUpdate = con.prepareUpdate(QueryLanguage.SPARQL, getUpdateString(), getBaseURI());
			preparedUpdate.setIncludeInferred(isIncludeInferred());
			preparedUpdate.setDataset(getDataset());
			
			preparedUpdate.execute();
		}
		catch (MalformedQueryException e) {
			throw new RepositoryException(e);
		}
		catch (UpdateExecutionException e) {
			throw new RepositoryException(e);
		}
		
	}

	/**
	 * @return Returns the updateString.
	 */
	public String getUpdateString() {
		return updateString;
	}

	/**
	 * @param updateString
	 *        The updateString to set.
	 */
	public void setUpdateString(String updateString) {
		this.updateString = updateString;
	}

	/**
	 * @return Returns the baseURI.
	 */
	public String getBaseURI() {
		return baseURI;
	}

	/**
	 * @param baseURI
	 *        The baseURI to set.
	 */
	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	/**
	 * @return Returns the includeInferred.
	 */
	public boolean isIncludeInferred() {
		return includeInferred;
	}

	/**
	 * @param includeInferred
	 *        The includeInferred to set.
	 */
	public void setIncludeInferred(boolean includeInferred) {
		this.includeInferred = includeInferred;
	}

	/**
	 * @return Returns the dataset.
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * @param dataset
	 *        The dataset to set.
	 */
	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	/**
	 * @return Returns the bindings.
	 */
	public Binding[] getBindings() {
		return bindings;
	}

	/**
	 * @param bindings
	 *        The bindings to set.
	 */
	public void setBindings(Binding[] bindings) {
		this.bindings = bindings;
	}

}
