/*
 * Copyright Aduna, DFKI and L3S (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.lucene.config;

import static org.openrdf.sail.lucene.config.LuceneSailConfigSchema.INDEX_DIR;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.sail.config.DelegatingSailImplConfigBase;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailImplConfig;

public class LuceneSailConfig extends DelegatingSailImplConfigBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String indexDir;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public LuceneSailConfig() {
		super(LuceneSailFactory.SAIL_TYPE);
	}

	public LuceneSailConfig(SailImplConfig delegate) {
		super(LuceneSailFactory.SAIL_TYPE, delegate);
	}

	public LuceneSailConfig(String luceneDir) {
		super(LuceneSailFactory.SAIL_TYPE);
		setIndexDir(luceneDir);
	}

	public LuceneSailConfig(String luceneDir, SailImplConfig delegate) {
		super(LuceneSailFactory.SAIL_TYPE, delegate);
		setIndexDir(luceneDir);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getIndexDir() {
		return indexDir;
	}

	public void setIndexDir(String luceneDir) {
		this.indexDir = luceneDir;
	}

	@Override
	public Resource export(Graph graph) {
		Resource implNode = super.export(graph);

		if (indexDir != null) {
			graph.add(implNode, INDEX_DIR, graph.getValueFactory().createLiteral(indexDir));
		}

		return implNode;
	}

	@Override
	public void parse(Graph graph, Resource implNode)
		throws SailConfigException
	{
		super.parse(graph, implNode);

		try {
			Literal indexDirLit = GraphUtil.getOptionalObjectLiteral(graph, implNode, INDEX_DIR);
			if (indexDirLit != null) {
				setIndexDir(indexDirLit.getLabel());
			}
		}
		catch (GraphUtilException e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}
}
