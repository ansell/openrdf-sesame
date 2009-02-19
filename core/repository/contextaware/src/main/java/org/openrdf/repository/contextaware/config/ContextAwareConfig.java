/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.contextaware.config;

import static org.openrdf.repository.contextaware.config.ContextAwareSchema.ADD_CONTEXT;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.ARCHIVE_CONTEXT;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.INCLUDE_INFERRED;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.MAX_QUERY_TIME;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.QUERY_LANGUAGE;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.QUERY_RESULT_LIMIT;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.READ_CONTEXT;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.REMOVE_CONTEXT;

import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.config.DelegatingRepositoryImplConfigBase;
import org.openrdf.repository.contextaware.ContextAwareConnection;
import org.openrdf.store.StoreConfigException;

/**
 * @author James Leigh
 */
public class ContextAwareConfig extends DelegatingRepositoryImplConfigBase {

	private static final URI[] ALL_CONTEXTS = new URI[0];

	private Boolean includeInferred = true;

	private int maxQueryTime = 0;

	private int queryResultLimit = -1;

	private QueryLanguage queryLanguage = QueryLanguage.SPARQL;

	private URI[] readContexts = ALL_CONTEXTS;

	private URI[] addContexts = ALL_CONTEXTS;

	private URI[] removeContexts = ALL_CONTEXTS;

	private URI[] archiveContexts = ALL_CONTEXTS;

	public ContextAwareConfig() {
		super(ContextAwareFactory.REPOSITORY_TYPE);
	}

	public int getMaxQueryTime() {
		return maxQueryTime;
	}

	public void setMaxQueryTime(int maxQueryTime) {
		this.maxQueryTime = maxQueryTime;
	}

	public int getQueryResultLimit() {
		return queryResultLimit;
	}

	public void setQueryResultLimit(int queryResultLimit) {
		this.queryResultLimit = queryResultLimit;
	}

	/**
	 * @see ContextAwareConnection#getAddContexts()
	 */
	public URI[] getAddContexts() {
		return addContexts;
	}

	/**
	 * @see ContextAwareConnection#getArchiveContexts()
	 */
	public URI[] getArchiveContexts() {
		return archiveContexts;
	}

	/**
	 * @see ContextAwareConnection#getQueryLanguage()
	 */
	public QueryLanguage getQueryLanguage() {
		return queryLanguage;
	}

	/**
	 * @see ContextAwareConnection#getReadContexts()
	 */
	public URI[] getReadContexts() {
		return readContexts;
	}

	/**
	 * @see ContextAwareConnection#getRemoveContexts()
	 */
	public URI[] getRemoveContexts() {
		return removeContexts;
	}

	/**
	 * @see ContextAwareConnection#isIncludeInferred()
	 */
	public boolean isIncludeInferred() {
		return includeInferred == null || includeInferred;
	}

	/**
	 * @see ContextAwareConnection#setAddContexts(URI[])
	 */
	public void setAddContexts(URI... addContexts) {
		this.addContexts = addContexts;
	}

	/**
	 * @see ContextAwareConnection#setArchiveContexts(URI[])
	 */
	public void setArchiveContexts(URI... archiveContexts) {
		this.archiveContexts = archiveContexts;
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
	 * @see ContextAwareConnection#setReadContexts(URI[])
	 */
	public void setReadContexts(URI... readContexts) {
		this.readContexts = readContexts;
	}

	/**
	 * @see ContextAwareConnection#setRemoveContexts(URI[])
	 */
	public void setRemoveContexts(URI... removeContexts) {
		this.removeContexts = removeContexts;
	}

	@Override
	public Resource export(Model model) {
		Resource repImplNode = super.export(model);

		ValueFactory vf = ValueFactoryImpl.getInstance();

		if (includeInferred != null) {
			Literal bool = vf.createLiteral(includeInferred);
			model.add(repImplNode, INCLUDE_INFERRED, bool);
		}
		if (maxQueryTime > 0) {
			model.add(repImplNode, MAX_QUERY_TIME, vf.createLiteral(maxQueryTime));
		}
		if (queryResultLimit >= 0) {
			model.add(repImplNode, QUERY_RESULT_LIMIT, vf.createLiteral(queryResultLimit));
		}
		if (queryLanguage != null) {
			model.add(repImplNode, QUERY_LANGUAGE, vf.createLiteral(queryLanguage.getName()));
		}
		for (URI uri : readContexts) {
			model.add(repImplNode, READ_CONTEXT, uri);
		}
		for (URI resource : addContexts) {
			model.add(repImplNode, ADD_CONTEXT, resource);
		}
		for (URI resource : removeContexts) {
			model.add(repImplNode, REMOVE_CONTEXT, resource);
		}
		for (URI resource : archiveContexts) {
			model.add(repImplNode, ARCHIVE_CONTEXT, resource);
		}

		return repImplNode;
	}

	@Override
	public void parse(Model model, Resource implNode)
		throws StoreConfigException
	{
		super.parse(model, implNode);

		try {
			Literal includeInferred = model.filter(implNode, INCLUDE_INFERRED, null).objectLiteral();
			if (includeInferred != null) {
				setIncludeInferred(includeInferred.booleanValue());
			}
			Literal maxQueryTime = model.filter(implNode, MAX_QUERY_TIME, null).objectLiteral();
			if (maxQueryTime != null) {
				setMaxQueryTime(maxQueryTime.intValue());
			}
			Literal queryResultLimit = model.filter(implNode, QUERY_RESULT_LIMIT, null).objectLiteral();
			if (queryResultLimit != null) {
				setQueryResultLimit(queryResultLimit.intValue());
			}
			Literal queryLanguage = model.filter(implNode, QUERY_LANGUAGE, null).objectLiteral();
			if (queryLanguage != null) {
				setQueryLanguage(QueryLanguage.valueOf(queryLanguage.getLabel()));
			}

			Set<Value> objects = model.filter(implNode, READ_CONTEXT, null).objects();
			setReadContexts(objects.toArray(new URI[objects.size()]));

			objects = model.filter(implNode, ADD_CONTEXT, null).objects();
			setAddContexts(objects.toArray(new URI[objects.size()]));

			objects = model.filter(implNode, REMOVE_CONTEXT, null).objects();
			setRemoveContexts(objects.toArray(new URI[objects.size()]));

			objects = model.filter(implNode, ARCHIVE_CONTEXT, null).objects();
			setArchiveContexts(objects.toArray(new URI[objects.size()]));
		}
		catch (ModelException e) {
			throw new StoreConfigException(e);
		}
		catch (ArrayStoreException e) {
			throw new StoreConfigException(e);
		}
	}
}
