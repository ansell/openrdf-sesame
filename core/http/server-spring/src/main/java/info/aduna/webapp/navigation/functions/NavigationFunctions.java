/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
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
