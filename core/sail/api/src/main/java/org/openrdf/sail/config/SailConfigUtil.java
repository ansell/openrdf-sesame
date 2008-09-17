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

public class SailConfigUtil {

	public static SailImplConfig parseRepositoryImpl(Model model, Resource implNode)
		throws SailConfigException
	{
		try {
			Literal typeLit = ModelUtil.getOptionalObjectLiteral(model, implNode, SailConfigSchema.SAILTYPE);

			if (typeLit != null) {
				SailFactory factory = SailRegistry.getInstance().get(typeLit.getLabel());

				if (factory != null) {
					SailImplConfig implConfig = factory.getConfig();
					implConfig.parse(model, implNode);
					return implConfig;
				}
				else {
					throw new SailConfigException("Unsupported Sail type: " + typeLit.getLabel());
				}
			}

			return null;
		}
		catch (ModelUtilException e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}
}
