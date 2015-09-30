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
package org.eclipse.rdf4j.query.parser.serql;

import org.eclipse.rdf4j.query.parser.serql.ast.ASTAnd;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTArgList;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTBNode;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTBasicPathExpr;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTBasicPathExprTail;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTBooleanConstant;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTBound;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTCompOperator;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTCompare;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTCompareAll;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTCompareAny;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTConstruct;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTConstructQuery;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTDatatype;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTEdge;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTExists;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTFrom;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTFunctionCall;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTGraphIntersect;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTGraphMinus;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTGraphUnion;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTIn;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTInList;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTIsBNode;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTIsLiteral;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTIsResource;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTIsURI;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTLabel;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTLang;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTLangMatches;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTLike;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTLimit;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTLiteral;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTLocalName;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTNamespace;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTNamespaceDecl;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTNode;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTNodeElem;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTNot;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTNull;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTOffset;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTOptPathExpr;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTOptPathExprTail;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTOr;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTOrderBy;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTOrderExpr;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTPathExprList;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTPathExprUnion;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTProjectionElem;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTQName;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTQueryBody;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTQueryContainer;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTRegex;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTReifiedStat;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTSameTerm;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTSelect;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTSelectQuery;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTStr;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTString;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTTupleIntersect;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTTupleMinus;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTTupleUnion;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTURI;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTVar;
import org.eclipse.rdf4j.query.parser.serql.ast.ASTWhere;
import org.eclipse.rdf4j.query.parser.serql.ast.SimpleNode;
import org.eclipse.rdf4j.query.parser.serql.ast.SyntaxTreeBuilderVisitor;
import org.eclipse.rdf4j.query.parser.serql.ast.VisitorException;

public abstract class AbstractASTVisitor implements SyntaxTreeBuilderVisitor {

	public Object visit(SimpleNode node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTQueryContainer node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTNamespaceDecl node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTTupleUnion node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTTupleMinus node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTTupleIntersect node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTGraphUnion node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTGraphMinus node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTGraphIntersect node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTSelectQuery node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTSelect node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTProjectionElem node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTConstructQuery node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTConstruct node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTQueryBody node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTFrom node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTWhere node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTOrderBy node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTOrderExpr node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTLimit node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTOffset node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTPathExprList node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTPathExprUnion node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBasicPathExpr node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTOptPathExpr node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBasicPathExprTail node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTOptPathExprTail node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTEdge node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTNodeElem node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTNode node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTReifiedStat node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTOr node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTAnd node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBooleanConstant node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTNot node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBound node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTIsResource node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTIsLiteral node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTIsURI node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTIsBNode node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTLangMatches node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTExists node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTSameTerm node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTCompare node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTCompareAny node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTCompareAll node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTLike node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTRegex node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTIn node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTInList node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTCompOperator node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTVar node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTDatatype node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTLang node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTLabel node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTNamespace node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTLocalName node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTStr node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTFunctionCall node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTArgList node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTURI node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTQName node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBNode node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTLiteral node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTString node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTNull node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}
}
