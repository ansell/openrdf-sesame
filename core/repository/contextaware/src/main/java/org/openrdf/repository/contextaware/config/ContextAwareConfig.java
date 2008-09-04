/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.contextaware.config;

import static org.openrdf.model.util.GraphUtil.getObjects;
import static org.openrdf.model.util.GraphUtil.getOptionalObjectLiteral;
import static org.openrdf.model.util.GraphUtil.getUniqueObjectResource;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.*;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.ARCHIVE_CONTEXT;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.DELEGATE;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.INCLUDE_INFERRED;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.QL;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.READ_CONTEXT;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.REMOVE_CONTEXT;

import java.util.Set;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryImplConfigBase;
import org.openrdf.repository.contextaware.ContextAwareConnection;

/**
 * @author James Leigh
 */
public class ContextAwareConfig extends RepositoryImplConfigBase {

	private static final URI[] ALL_CONTEXTS = new URI[0];

	private Boolean includeInferred = true;

	private long maxQueryTime;

	private QueryLanguage ql = QueryLanguage.SPARQL;

	private URI[] readContexts = ALL_CONTEXTS;

	private URI[] addContexts = ALL_CONTEXTS;

	private URI[] removeContexts = ALL_CONTEXTS;

	private URI[] archiveContexts = ALL_CONTEXTS;

	private RepositoryImplConfig delegate;

	public ContextAwareConfig() {
		super(ContextAwareFactory.REPOSITORY_TYPE);
	}

	public long getMaxQueryTime() {
		return maxQueryTime;
	}

	public void setMaxQueryTime(long maxQueryTime) {
		this.maxQueryTime = maxQueryTime;
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
		return includeInferred == null || includeInferred;
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

	public RepositoryImplConfig getDelegate() {
		return delegate;
	}

	public void setDelegate(RepositoryImplConfig delegate) {
		this.delegate = delegate;
	}

	@Override
	public Resource export(Graph graph) {
		ValueFactory vf = new ValueFactoryImpl();
		Resource repImplNode = super.export(graph);

		if (includeInferred != null) {
			Literal bool = vf.createLiteral(includeInferred);
			graph.add(repImplNode, INCLUDE_INFERRED, bool);
		}
		if (maxQueryTime > 0) {
			graph.add(repImplNode, MAX_QUERY_TIME, vf.createLiteral(maxQueryTime));
		}
		if (ql != null) {
			graph.add(repImplNode, QL, vf.createLiteral(ql.getName()));
		}
		for (URI uri : readContexts) {
			graph.add(repImplNode, READ_CONTEXT, uri);
		}
		for (URI resource : addContexts) {
			graph.add(repImplNode, ADD_CONTEXT, resource);
		}
		for (URI resource : removeContexts) {
			graph.add(repImplNode, REMOVE_CONTEXT, resource);
		}
		for (URI resource : archiveContexts) {
			graph.add(repImplNode, ARCHIVE_CONTEXT, resource);
		}
		if (delegate != null) {
			Resource del = delegate.export(graph);
			graph.add(repImplNode, DELEGATE, del);
		}

		return repImplNode;
	}

	@Override
	public void parse(Graph graph, Resource node)
		throws RepositoryConfigException
	{
		Literal lit;
		Set<Value> objects;

		try {
			lit = getOptionalObjectLiteral(graph, node, INCLUDE_INFERRED);
			if (lit != null) {
				setIncludeInferred(lit.booleanValue());
			}
			lit = getOptionalObjectLiteral(graph, node, MAX_QUERY_TIME);
			if (lit != null) {
				setMaxQueryTime(lit.longValue());
			}
			lit = getOptionalObjectLiteral(graph, node, QL);
			if (lit != null) {
				setQueryLanguage(QueryLanguage.valueOf(lit.getLabel()));
			}

			objects = getObjects(graph, node, READ_CONTEXT);
			setReadContexts(objects.toArray(new URI[objects.size()]));

			objects = getObjects(graph, node, ADD_CONTEXT);
			setAddContexts(objects.toArray(new URI[objects.size()]));

			objects = getObjects(graph, node, REMOVE_CONTEXT);
			setRemoveContexts(objects.toArray(new URI[objects.size()]));

			objects = getObjects(graph, node, ARCHIVE_CONTEXT);
			setArchiveContexts(objects.toArray(new URI[objects.size()]));

			Resource del = getUniqueObjectResource(graph, node, DELEGATE);
			setDelegate(create(graph, del));
		}
		catch (GraphUtilException e) {
			throw new RepositoryConfigException(e);
		}
		catch (ArrayStoreException e) {
			throw new RepositoryConfigException(e);
		}
	}
}
