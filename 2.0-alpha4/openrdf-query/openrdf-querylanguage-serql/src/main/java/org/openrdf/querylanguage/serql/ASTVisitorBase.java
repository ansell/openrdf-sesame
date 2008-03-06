/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql;

import org.openrdf.querylanguage.serql.ast.ASTAnd;
import org.openrdf.querylanguage.serql.ast.ASTBNode;
import org.openrdf.querylanguage.serql.ast.ASTBasicPathExpr;
import org.openrdf.querylanguage.serql.ast.ASTBasicPathExprTail;
import org.openrdf.querylanguage.serql.ast.ASTCompOperator;
import org.openrdf.querylanguage.serql.ast.ASTCompare;
import org.openrdf.querylanguage.serql.ast.ASTCompareAll;
import org.openrdf.querylanguage.serql.ast.ASTCompareAny;
import org.openrdf.querylanguage.serql.ast.ASTConstruct;
import org.openrdf.querylanguage.serql.ast.ASTConstructQuery;
import org.openrdf.querylanguage.serql.ast.ASTDatatype;
import org.openrdf.querylanguage.serql.ast.ASTEdge;
import org.openrdf.querylanguage.serql.ast.ASTExists;
import org.openrdf.querylanguage.serql.ast.ASTFalse;
import org.openrdf.querylanguage.serql.ast.ASTFrom;
import org.openrdf.querylanguage.serql.ast.ASTGraphIntersect;
import org.openrdf.querylanguage.serql.ast.ASTGraphMinus;
import org.openrdf.querylanguage.serql.ast.ASTGraphUnion;
import org.openrdf.querylanguage.serql.ast.ASTIn;
import org.openrdf.querylanguage.serql.ast.ASTIsBNode;
import org.openrdf.querylanguage.serql.ast.ASTIsLiteral;
import org.openrdf.querylanguage.serql.ast.ASTIsResource;
import org.openrdf.querylanguage.serql.ast.ASTIsURI;
import org.openrdf.querylanguage.serql.ast.ASTLabel;
import org.openrdf.querylanguage.serql.ast.ASTLang;
import org.openrdf.querylanguage.serql.ast.ASTLike;
import org.openrdf.querylanguage.serql.ast.ASTLimit;
import org.openrdf.querylanguage.serql.ast.ASTLiteral;
import org.openrdf.querylanguage.serql.ast.ASTLocalName;
import org.openrdf.querylanguage.serql.ast.ASTNamespace;
import org.openrdf.querylanguage.serql.ast.ASTNamespaceDecl;
import org.openrdf.querylanguage.serql.ast.ASTNode;
import org.openrdf.querylanguage.serql.ast.ASTNodeElem;
import org.openrdf.querylanguage.serql.ast.ASTNot;
import org.openrdf.querylanguage.serql.ast.ASTNull;
import org.openrdf.querylanguage.serql.ast.ASTOffset;
import org.openrdf.querylanguage.serql.ast.ASTOptPathExpr;
import org.openrdf.querylanguage.serql.ast.ASTOptPathExprTail;
import org.openrdf.querylanguage.serql.ast.ASTOr;
import org.openrdf.querylanguage.serql.ast.ASTQueryContainer;
import org.openrdf.querylanguage.serql.ast.ASTProjectionElem;
import org.openrdf.querylanguage.serql.ast.ASTQName;
import org.openrdf.querylanguage.serql.ast.ASTQueryBody;
import org.openrdf.querylanguage.serql.ast.ASTReifiedStat;
import org.openrdf.querylanguage.serql.ast.ASTSelect;
import org.openrdf.querylanguage.serql.ast.ASTSelectQuery;
import org.openrdf.querylanguage.serql.ast.ASTString;
import org.openrdf.querylanguage.serql.ast.ASTTrue;
import org.openrdf.querylanguage.serql.ast.ASTTupleIntersect;
import org.openrdf.querylanguage.serql.ast.ASTTupleMinus;
import org.openrdf.querylanguage.serql.ast.ASTTupleUnion;
import org.openrdf.querylanguage.serql.ast.ASTURI;
import org.openrdf.querylanguage.serql.ast.ASTVar;
import org.openrdf.querylanguage.serql.ast.ASTWhere;
import org.openrdf.querylanguage.serql.ast.SimpleNode;
import org.openrdf.querylanguage.serql.ast.SyntaxTreeBuilderVisitor;
import org.openrdf.querylanguage.serql.ast.VisitorException;

public abstract class ASTVisitorBase implements SyntaxTreeBuilderVisitor {

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

	public Object visit(ASTTrue node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTFalse node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTNot node, Object data)
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

	public Object visit(ASTExists node, Object data)
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

	public Object visit(ASTIn node, Object data)
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
