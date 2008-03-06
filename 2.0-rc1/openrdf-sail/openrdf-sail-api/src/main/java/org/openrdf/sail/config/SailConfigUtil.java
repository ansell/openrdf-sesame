/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.config;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;

public class SailConfigUtil {

	public static SailImplConfig parseRepositoryImpl(Graph graph, Resource implNode)
		throws SailConfigException
	{
		try {
			Literal typeLit = GraphUtil.getOptionalObjectLiteral(graph, implNode, SailConfigSchema.SAILTYPE);

			if (typeLit != null) {
				SailFactory factory = SailRegistry.getInstance().get(typeLit.getLabel());

				if (factory != null) {
					SailImplConfig implConfig = factory.getConfig();
					implConfig.parse(graph, implNode);
					return implConfig;
				}
				else {
					throw new SailConfigException("Unsupported Sail type: " + typeLit.getLabel());
				}
			}

			return null;
		}
		catch (GraphUtilException e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}
}
