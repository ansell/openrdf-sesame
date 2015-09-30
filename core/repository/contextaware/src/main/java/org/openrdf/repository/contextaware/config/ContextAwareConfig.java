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
package org.openrdf.repository.contextaware.config;

import static org.openrdf.repository.contextaware.config.ContextAwareSchema.ADD_CONTEXT;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.ARCHIVE_CONTEXT;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.BASE_URI;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.INCLUDE_INFERRED;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.INSERT_CONTEXT;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.MAX_QUERY_TIME;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.QUERY_LANGUAGE;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.READ_CONTEXT;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.REMOVE_CONTEXT;

import java.util.Set;

import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.model.util.Models;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.config.AbstractDelegatingRepositoryImplConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.contextaware.ContextAwareConnection;

/**
 * @author James Leigh
 */
public class ContextAwareConfig extends AbstractDelegatingRepositoryImplConfig {

	private static final IRI[] ALL_CONTEXTS = new IRI[0];

	private Boolean includeInferred = true;

	private int maxQueryTime = 0;

	private QueryLanguage queryLanguage = QueryLanguage.SPARQL;

	private String baseURI;

	private IRI[] readContexts = ALL_CONTEXTS;

	private IRI[] addContexts = ALL_CONTEXTS;

	private IRI[] removeContexts = ALL_CONTEXTS;

	private IRI[] archiveContexts = ALL_CONTEXTS;

	private IRI insertContext = null;

	public ContextAwareConfig() {
		super(ContextAwareFactory.REPOSITORY_TYPE);
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
		return queryLanguage;
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
		return includeInferred == null || includeInferred;
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
		this.queryLanguage = ql;
	}

	/**
	 * @param baseURI
	 *        The default baseURI to set.
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
	public Resource export(Model model) {
		Resource repImplNode = super.export(model);

		ValueFactory vf = SimpleValueFactory.getInstance();

		if (includeInferred != null) {
			Literal bool = vf.createLiteral(includeInferred);
			model.add(repImplNode, INCLUDE_INFERRED, bool);
		}
		if (maxQueryTime > 0) {
			model.add(repImplNode, MAX_QUERY_TIME, vf.createLiteral(maxQueryTime));
		}
		if (queryLanguage != null) {
			model.add(repImplNode, QUERY_LANGUAGE, vf.createLiteral(queryLanguage.getName()));
		}
		if (baseURI != null) {
			model.add(repImplNode, BASE_URI, vf.createIRI(baseURI));
		}
		for (IRI uri : readContexts) {
			model.add(repImplNode, READ_CONTEXT, uri);
		}
		for (IRI resource : addContexts) {
			model.add(repImplNode, ADD_CONTEXT, resource);
		}
		for (IRI resource : removeContexts) {
			model.add(repImplNode, REMOVE_CONTEXT, resource);
		}
		for (IRI resource : archiveContexts) {
			model.add(repImplNode, ARCHIVE_CONTEXT, resource);
		}
		if (insertContext != null) {
			model.add(repImplNode, INSERT_CONTEXT, insertContext);
		}

		return repImplNode;
	}

	@Override
	public void parse(Model model, Resource resource)
		throws RepositoryConfigException
	{
		super.parse(model, resource);

		try {
			Models.objectLiteral(model.filter(resource, INCLUDE_INFERRED, null)).ifPresent(
					lit -> setIncludeInferred(lit.booleanValue()));

			Models.objectLiteral(model.filter(resource, MAX_QUERY_TIME, null)).ifPresent(
					lit -> setMaxQueryTime(lit.intValue()));

			Models.objectLiteral(model.filter(resource, QUERY_LANGUAGE, null)).ifPresent(
					lit -> setQueryLanguage(QueryLanguage.valueOf(lit.getLabel())));

			Models.objectIRI(model.filter(resource, QUERY_LANGUAGE, null)).ifPresent(
					iri -> setBaseURI(iri.stringValue()));

			Set<Value> objects = model.filter(resource, READ_CONTEXT, null).objects();
			setReadContexts(objects.toArray(new IRI[objects.size()]));

			objects = model.filter(resource, ADD_CONTEXT, null).objects();
			setAddContexts(objects.toArray(new IRI[objects.size()]));

			objects = model.filter(resource, REMOVE_CONTEXT, null).objects();
			setRemoveContexts(objects.toArray(new IRI[objects.size()]));

			objects = model.filter(resource, ARCHIVE_CONTEXT, null).objects();
			setArchiveContexts(objects.toArray(new IRI[objects.size()]));

			Models.objectIRI(model.filter(resource, INSERT_CONTEXT, null)).ifPresent(
					iri -> setInsertContext(iri));
		}
		catch (ArrayStoreException e) {
			throw new RepositoryConfigException(e);
		}
	}
}
