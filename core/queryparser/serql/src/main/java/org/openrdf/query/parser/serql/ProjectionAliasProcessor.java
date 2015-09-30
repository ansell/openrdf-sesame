/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
class ProjectionAliasProcessor extends AbstractASTVisitor {

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
