/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.sparql;

import org.openrdf.querylanguage.sparql.ast.ASTAnd;
import org.openrdf.querylanguage.sparql.ast.ASTAskQuery;
import org.openrdf.querylanguage.sparql.ast.ASTBaseDecl;
import org.openrdf.querylanguage.sparql.ast.ASTBasicGraphPattern;
import org.openrdf.querylanguage.sparql.ast.ASTBlankNode;
import org.openrdf.querylanguage.sparql.ast.ASTBlankNodePropertyList;
import org.openrdf.querylanguage.sparql.ast.ASTBound;
import org.openrdf.querylanguage.sparql.ast.ASTCollection;
import org.openrdf.querylanguage.sparql.ast.ASTCompare;
import org.openrdf.querylanguage.sparql.ast.ASTConstraint;
import org.openrdf.querylanguage.sparql.ast.ASTConstruct;
import org.openrdf.querylanguage.sparql.ast.ASTConstructQuery;
import org.openrdf.querylanguage.sparql.ast.ASTDatasetClause;
import org.openrdf.querylanguage.sparql.ast.ASTDatatype;
import org.openrdf.querylanguage.sparql.ast.ASTDescribe;
import org.openrdf.querylanguage.sparql.ast.ASTDescribeQuery;
import org.openrdf.querylanguage.sparql.ast.ASTFalse;
import org.openrdf.querylanguage.sparql.ast.ASTFunctionCall;
import org.openrdf.querylanguage.sparql.ast.ASTGraphGraphPattern;
import org.openrdf.querylanguage.sparql.ast.ASTGraphPatternGroup;
import org.openrdf.querylanguage.sparql.ast.ASTIRI;
import org.openrdf.querylanguage.sparql.ast.ASTIsBlank;
import org.openrdf.querylanguage.sparql.ast.ASTIsIRI;
import org.openrdf.querylanguage.sparql.ast.ASTIsLiteral;
import org.openrdf.querylanguage.sparql.ast.ASTLang;
import org.openrdf.querylanguage.sparql.ast.ASTLangMatches;
import org.openrdf.querylanguage.sparql.ast.ASTLimit;
import org.openrdf.querylanguage.sparql.ast.ASTMath;
import org.openrdf.querylanguage.sparql.ast.ASTNot;
import org.openrdf.querylanguage.sparql.ast.ASTNumericLiteral;
import org.openrdf.querylanguage.sparql.ast.ASTObjectList;
import org.openrdf.querylanguage.sparql.ast.ASTOffset;
import org.openrdf.querylanguage.sparql.ast.ASTOptionalGraphPattern;
import org.openrdf.querylanguage.sparql.ast.ASTOr;
import org.openrdf.querylanguage.sparql.ast.ASTOrderClause;
import org.openrdf.querylanguage.sparql.ast.ASTOrderCondition;
import org.openrdf.querylanguage.sparql.ast.ASTPrefixDecl;
import org.openrdf.querylanguage.sparql.ast.ASTPropertyList;
import org.openrdf.querylanguage.sparql.ast.ASTQName;
import org.openrdf.querylanguage.sparql.ast.ASTQueryContainer;
import org.openrdf.querylanguage.sparql.ast.ASTRDFLiteral;
import org.openrdf.querylanguage.sparql.ast.ASTRegexExpression;
import org.openrdf.querylanguage.sparql.ast.ASTSelect;
import org.openrdf.querylanguage.sparql.ast.ASTSelectQuery;
import org.openrdf.querylanguage.sparql.ast.ASTStr;
import org.openrdf.querylanguage.sparql.ast.ASTString;
import org.openrdf.querylanguage.sparql.ast.ASTTriplesSameSubject;
import org.openrdf.querylanguage.sparql.ast.ASTTrue;
import org.openrdf.querylanguage.sparql.ast.ASTUnionGraphPattern;
import org.openrdf.querylanguage.sparql.ast.ASTVar;
import org.openrdf.querylanguage.sparql.ast.ASTWhereClause;
import org.openrdf.querylanguage.sparql.ast.SimpleNode;
import org.openrdf.querylanguage.sparql.ast.SyntaxTreeBuilderVisitor;
import org.openrdf.querylanguage.sparql.ast.VisitorException;

/**
 * Base class for visitors of the SPARQL AST.
 * 
 * @author arjohn
 */
abstract class ASTVisitorBase implements SyntaxTreeBuilderVisitor {

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

	public Object visit(ASTBaseDecl node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTPrefixDecl node, Object data)
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

	public Object visit(ASTDescribeQuery node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTDescribe node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTAskQuery node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTDatasetClause node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTWhereClause node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTOrderClause node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTOrderCondition node, Object data)
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

	public Object visit(ASTGraphPatternGroup node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBasicGraphPattern node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTOptionalGraphPattern node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTGraphGraphPattern node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTUnionGraphPattern node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTConstraint node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTFunctionCall node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTTriplesSameSubject node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTPropertyList node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTObjectList node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTIRI node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBlankNodePropertyList node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTCollection node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTVar node, Object data)
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

	public Object visit(ASTCompare node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTMath node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTNot node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTNumericLiteral node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTStr node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTLang node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTLangMatches node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTDatatype node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBound node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTIsIRI node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTIsBlank node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTIsLiteral node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTRegexExpression node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTRDFLiteral node, Object data)
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

	public Object visit(ASTString node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTQName node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBlankNode node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}
}
