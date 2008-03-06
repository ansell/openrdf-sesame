/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.querylanguage.serql.ast.ASTNamespaceDecl;
import org.openrdf.querylanguage.serql.ast.ASTURI;
import org.openrdf.querylanguage.serql.ast.VisitorException;


/**
 * Processes the namespace declarations in a SeRQL query model.
 */
class NamespaceDeclProcessor extends ASTVisitorBase {

	private Map<String, String> _namespaces = new HashMap<String, String>();

	public Map<String, String> getNamespaces() {
		return _namespaces;
	}

	public Object visit(ASTNamespaceDecl node, Object data)
		throws VisitorException
	{
		// Get namespace URI from child URI node
		String namespace = (String)super.visit(node, null);
		String prefix = node.getPrefix();

		if (_namespaces.containsKey(prefix)) {
			// Prefix already defined

			if (_namespaces.get(prefix).equals(namespace)) {
				// duplicate, ignore
			}
			else {
				throw new VisitorException("Multiple namespace declarations for prefix '" + prefix + "'");
			}
		}
		else {
			_namespaces.put(prefix, namespace);
		}

		return data;
	}

	public String visit(ASTURI node, Object data)
		throws VisitorException
	{
		return node.getValue();
	}
}
