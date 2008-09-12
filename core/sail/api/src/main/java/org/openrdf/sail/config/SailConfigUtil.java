/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.config;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;

public class SailConfigUtil {

	public static SailImplConfig parseRepositoryImpl(Model model, Resource implNode)
		throws SailConfigException
	{
		try {
			for (Value obj : model.objects(implNode, SailConfigSchema.SAILTYPE)) {
				Literal typeLit = (Literal)obj;

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
		catch (SailConfigException e) {
			throw e;
		}
		catch (Exception e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}
}
