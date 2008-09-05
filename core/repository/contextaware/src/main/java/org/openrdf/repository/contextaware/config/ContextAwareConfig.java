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
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.READ_CONTEXT;
import static org.openrdf.repository.contextaware.config.ContextAwareSchema.REMOVE_CONTEXT;

import java.util.Set;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.config.DelegatingRepositoryImplConfigBase;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.contextaware.ContextAwareConnection;

/**
 * @author James Leigh
 */
public class ContextAwareConfig extends DelegatingRepositoryImplConfigBase {

	private static final URI[] ALL_CONTEXTS = new URI[0];

	private Boolean includeInferred = true;

	private int maxQueryTime = 0;

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
	public Resource export(Graph graph) {
		Resource repImplNode = super.export(graph);

		ValueFactory vf = graph.getValueFactory();

		if (includeInferred != null) {
			Literal bool = vf.createLiteral(includeInferred);
			graph.add(repImplNode, INCLUDE_INFERRED, bool);
		}
		if (maxQueryTime > 0) {
			graph.add(repImplNode, MAX_QUERY_TIME, vf.createLiteral(maxQueryTime));
		}
		if (queryLanguage != null) {
			graph.add(repImplNode, QUERY_LANGUAGE, vf.createLiteral(queryLanguage.getName()));
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

		return repImplNode;
	}

	@Override
	public void parse(Graph graph, Resource implNode)
		throws RepositoryConfigException
	{
		super.parse(graph, implNode);

		try {
			Literal lit = GraphUtil.getOptionalObjectLiteral(graph, implNode, INCLUDE_INFERRED);
			if (lit != null) {
				setIncludeInferred(lit.booleanValue());
			}
			lit = GraphUtil.getOptionalObjectLiteral(graph, implNode, MAX_QUERY_TIME);
			if (lit != null) {
				setMaxQueryTime(lit.intValue());
			}
			lit = GraphUtil.getOptionalObjectLiteral(graph, implNode, QUERY_LANGUAGE);
			if (lit != null) {
				setQueryLanguage(QueryLanguage.valueOf(lit.getLabel()));
			}

			Set<Value> objects = GraphUtil.getObjects(graph, implNode, READ_CONTEXT);
			setReadContexts(objects.toArray(new URI[objects.size()]));

			objects = GraphUtil.getObjects(graph, implNode, ADD_CONTEXT);
			setAddContexts(objects.toArray(new URI[objects.size()]));

			objects = GraphUtil.getObjects(graph, implNode, REMOVE_CONTEXT);
			setRemoveContexts(objects.toArray(new URI[objects.size()]));

			objects = GraphUtil.getObjects(graph, implNode, ARCHIVE_CONTEXT);
			setArchiveContexts(objects.toArray(new URI[objects.size()]));
		}
		catch (GraphUtilException e) {
			throw new RepositoryConfigException(e);
		}
		catch (ArrayStoreException e) {
			throw new RepositoryConfigException(e);
		}
	}
}
