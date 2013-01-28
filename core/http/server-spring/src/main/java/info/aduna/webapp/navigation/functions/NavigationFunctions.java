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
package info.aduna.webapp.navigation.functions;

import info.aduna.webapp.navigation.NavigationNode;

/**
 * JSTL functions for navigation.
 * 
 * @author Herko ter Horst
 */
public class NavigationFunctions {
	/**
	 * Is the specified potential parent indeed a parent of the specified node.
	 * 
	 * @param potentialParent
	 *            the potential parent
	 * @param node
	 *            the node
	 * @return true if the potential parent is part of the hierarchical string
	 *         of parents for the specified node
	 */
	public static boolean isParent(NavigationNode potentialParent,
			NavigationNode node) {
		return potentialParent.isParent(node);
	}
}
