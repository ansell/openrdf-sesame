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
package org.eclipse.rdf4j.repository.contextaware;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.base.RepositoryWrapper;

/**
 * Allows contexts to be specified at the repository level.
 * 
 * @author James Leigh
 */
public class ContextAwareRepository extends RepositoryWrapper {

	private static final IRI[] ALL_CONTEXTS = new IRI[0];

	private boolean includeInferred = true;

	private int maxQueryTime;

	private QueryLanguage ql = QueryLanguage.SPARQL;

	private String baseURI;

	private IRI[] readContexts = ALL_CONTEXTS;

	private IRI[] addContexts = ALL_CONTEXTS;

	private IRI[] removeContexts = ALL_CONTEXTS;

	private IRI[] archiveContexts = ALL_CONTEXTS;

	private IRI insertContext = null;

	public ContextAwareRepository() {
		super();
	}

	public ContextAwareRepository(Repository delegate) {
		super(delegate);
	}

	public int getMaxQueryTime() {
		return maxQueryTime;
	}

	public void setMaxQueryTime(int maxQueryTime) {
		this.maxQueryTime = maxQueryTime;
	}

	/**
	 * @see ContextAwareConnection#getAddContexts()
	 */
	@Deprecated
	public IRI[] getAddContexts() {
		return addContexts;
	}

	/**
	 * @see ContextAwareConnection#getArchiveContexts()
	 */
	@Deprecated
	public IRI[] getArchiveContexts() {
		return archiveContexts;
	}

	/**
	 * @see ContextAwareConnection#getInsertContext()
	 */
	public IRI getInsertContext() {
		return insertContext;
	}

	/**
	 * @see ContextAwareConnection#getQueryLanguage()
	 */
	public QueryLanguage getQueryLanguage() {
		return ql;
	}

	
	/**
	 * @return Returns the default baseURI.
	 */
	public String getBaseURI() {
		return baseURI;
	}

	/**
	 * @see ContextAwareConnection#getReadContexts()
	 */
	public IRI[] getReadContexts() {
		return readContexts;
	}

	/**
	 * @see ContextAwareConnection#getRemoveContexts()
	 */
	public IRI[] getRemoveContexts() {
		return removeContexts;
	}

	/**
	 * @see ContextAwareConnection#isIncludeInferred()
	 */
	public boolean isIncludeInferred() {
		return includeInferred;
	}

	/**
	 * @see ContextAwareConnection#setAddContexts(IRI[])
	 */
	@Deprecated
	public void setAddContexts(IRI... addContexts) {
		this.addContexts = addContexts;
	}

	/**
	 * @see ContextAwareConnection#setArchiveContexts(IRI[])
	 */
	@Deprecated
	public void setArchiveContexts(IRI... archiveContexts) {
		this.archiveContexts = archiveContexts;
	}

	/**
	 * @see ContextAwareConnection#setInsertContext(IRI)
	 */
	public void setInsertContext(IRI insertContext) {
		this.insertContext = insertContext;
	}

	/**
	 * @see ContextAwareConnection#setIncludeInferred(boolean)
	 */
	public void setIncludeInferred(boolean includeInferred) {
		this.includeInferred = includeInferred;
	}

	/**
	 * @see ContextAwareConnection#setQueryLanguage(QueryLanguage)
	 */
	public void setQueryLanguage(QueryLanguage ql) {
		this.ql = ql;
	}

	
	/**
	 * @param baseURI The default baseURI to set.
	 */
	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	/**
	 * @see ContextAwareConnection#setReadContexts(IRI[])
	 */
	public void setReadContexts(IRI... readContexts) {
		this.readContexts = readContexts;
	}

	/**
	 * @see ContextAwareConnection#setRemoveContexts(IRI[])
	 */
	public void setRemoveContexts(IRI... removeContexts) {
		this.removeContexts = removeContexts;
	}

	@Override
	public ContextAwareConnection getConnection()
		throws RepositoryException
	{
		ContextAwareConnection con = new ContextAwareConnection(this, super.getConnection());
		con.setIncludeInferred(isIncludeInferred());
		con.setMaxQueryTime(getMaxQueryTime());
		con.setQueryLanguage(getQueryLanguage());
		con.setBaseURI(getBaseURI());
		con.setReadContexts(getReadContexts());
		con.setAddContexts(getAddContexts());
		con.setRemoveContexts(getRemoveContexts());
		con.setArchiveContexts(getArchiveContexts());
		con.setInsertContext(getInsertContext());
		return con;
	}

}
