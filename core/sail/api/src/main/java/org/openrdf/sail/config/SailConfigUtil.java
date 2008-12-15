/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.config;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.model.util.ModelUtilException;
import org.openrdf.store.StoreConfigException;

public class SailConfigUtil {

	public static SailImplConfig parseRepositoryImpl(Model model, Resource implNode)
		throws StoreConfigException
	{
		try {
			Literal typeLit = model.filter(implNode, SailConfigSchema.SAILTYPE, null).literal();

			if (typeLit != null) {
				SailFactory factory = SailRegistry.getInstance().get(typeLit.getLabel());

				if (factory != null) {
					SailImplConfig implConfig = factory.getConfig();
					implConfig.parse(model, implNode);
					return implConfig;
				}
				else {
					throw new StoreConfigException("Unsupported Sail type: " + typeLit.getLabel());
				}
			}

			return null;
		}
		catch (ModelUtilException e) {
			throw new StoreConfigException(e.getMessage(), e);
		}
	}
}
