/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.contextaware;

import org.openrdf.model.URI;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryWrapper;

/**
 * Allows contexts to be specified at the repository level.
 * 
 * @author James Leigh
 */
public class ContextAwareRepository extends RepositoryWrapper {

	private static final URI[] ALL_CONTEXTS = new URI[0];

	private boolean includeInferred = true;

	private QueryLanguage ql = QueryLanguage.SPARQL;

	private URI[] readContexts = ALL_CONTEXTS;

	private URI[] addContexts = ALL_CONTEXTS;

	private URI[] removeContexts = ALL_CONTEXTS;

	private URI[] archiveContexts = ALL_CONTEXTS;

	public ContextAwareRepository(Repository delegate) {
		super(delegate);
	}

	/**
	 * @return
	 * @see ContextAwareConnection#getAddContexts()
	 */
	public URI[] getAddContexts() {
		return addContexts;
	}

	/**
	 * @return
	 * @see ContextAwareConnection#getArchiveContexts()
	 */
	public URI[] getArchiveContexts() {
		return archiveContexts;
	}

	/**
	 * @return
	 * @see ContextAwareConnection#getQueryLanguage()
	 */
	public QueryLanguage getQueryLanguage() {
		return ql;
	}

	/**
	 * @return
	 * @see ContextAwareConnection#getReadContexts()
	 */
	public URI[] getReadContexts() {
		return readContexts;
	}

	/**
	 * @return
	 * @see ContextAwareConnection#getRemoveContexts()
	 */
	public URI[] getRemoveContexts() {
		return removeContexts;
	}

	/**
	 * @param includeInferred
	 * @see ContextAwareConnection#isIncludeInferred()
	 */
	public boolean isIncludeInferred() {
		return includeInferred;
	}

	/**
	 * @param addContexts
	 * @see ContextAwareConnection#setAddContexts(URI[])
	 */
	public void setAddContexts(URI... addContexts) {
		this.addContexts = addContexts;
	}

	/**
	 * @param archiveContexts
	 * @see ContextAwareConnection#setArchiveContexts(URI[])
	 */
	public void setArchiveContexts(URI... archiveContexts) {
		this.archiveContexts = archiveContexts;
	}

	/**
	 * @param includeInferred
	 * @see ContextAwareConnection#setIncludeInferred(boolean)
	 */
	public void setIncludeInferred(boolean includeInferred) {
		this.includeInferred = includeInferred;
	}

	/**
	 * @param ql
	 * @see ContextAwareConnection#setQueryLanguage(QueryLanguage)
	 */
	public void setQueryLanguage(QueryLanguage ql) {
		this.ql = ql;
	}

	/**
	 * @param readContexts
	 * @see ContextAwareConnection#setReadContexts(URI[])
	 */
	public void setReadContexts(URI... readContexts) {
		this.readContexts = readContexts;
	}

	/**
	 * @param removeContexts
	 * @see ContextAwareConnection#setRemoveContexts(URI[])
	 */
	public void setRemoveContexts(URI... removeContexts) {
		this.removeContexts = removeContexts;
	}

	@Override
	public ContextAwareConnection getConnection()
		throws RepositoryException
	{
		ContextAwareConnection con;
		con = new ContextAwareConnection(this, super.getConnection());
		return con;
	}

}
