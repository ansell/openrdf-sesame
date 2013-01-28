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
package org.openrdf.query.parser.serql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.query.parser.serql.ast.ASTProjectionElem;
import org.openrdf.query.parser.serql.ast.ASTSelect;
import org.openrdf.query.parser.serql.ast.ASTString;
import org.openrdf.query.parser.serql.ast.ASTVar;
import org.openrdf.query.parser.serql.ast.Node;
import org.openrdf.query.parser.serql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.query.parser.serql.ast.VisitorException;

/**
 * Processes projection aliases, verifying that the specified aliases are unique
 * and generating aliases for the elements for which no alias has been specified
 * but that do require one.
 * 
 * @author Arjohn Kampman
 */
class ProjectionAliasProcessor extends ASTVisitorBase {

	@Override
	public Object visit(ASTSelect node, Object data)
		throws VisitorException
	{
		// Iterate over all projection elements to retrieve the defined aliases
		Set<String> aliases = new HashSet<String>();
		List<Node> unaliasedNodes = new ArrayList<Node>();

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			ASTProjectionElem projElem = (ASTProjectionElem)node.jjtGetChild(i);
			
			String alias = projElem.getAlias();
			if (alias == null && projElem.getValueExpr() instanceof ASTVar) {
				alias = ((ASTVar)projElem.getValueExpr()).getName();
			}
			
			if (alias != null) {
				boolean isUnique = aliases.add(alias);
				
				if (!isUnique) {
					throw new VisitorException("Duplicate projection element names: '" + alias + "'");
				}
			}
			else {
				unaliasedNodes.add(projElem);
			}
		}

		// Iterate over the unaliased nodes and generate aliases for them
		int aliasNo = 1;
		for (Node projElem : unaliasedNodes) {
			// Generate unique alias for projection element
			String alias;
			while (aliases.contains(alias = "_" + aliasNo++)) {
				// try again
			}

			aliases.add(alias);

			ASTString aliasNode = new ASTString(SyntaxTreeBuilderTreeConstants.JJTSTRING);
			aliasNode.setValue(alias);
			aliasNode.jjtSetParent(projElem);
			projElem.jjtAppendChild(aliasNode);
		}

		return data;
	}
}
