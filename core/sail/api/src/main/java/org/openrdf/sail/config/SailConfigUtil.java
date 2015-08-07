/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.config;

import java.util.Optional;

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
				Optional<SailFactory> factory = SailRegistry.getInstance().get(typeLit.getLabel());

				if (factory.isPresent()) {
					SailImplConfig implConfig = factory.get().getConfig();
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
