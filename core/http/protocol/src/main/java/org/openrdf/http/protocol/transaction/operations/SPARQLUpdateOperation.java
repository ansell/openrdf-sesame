/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.http.protocol.transaction.operations;

import java.io.Serializable;

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
public class SPARQLUpdateOperation implements TransactionOperation, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4432275498318918582L;

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
			
			if (getBindings() != null) {
				for (Binding binding : getBindings()) {
					preparedUpdate.setBinding(binding.getName(), binding.getValue());
				}
			}
			
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
