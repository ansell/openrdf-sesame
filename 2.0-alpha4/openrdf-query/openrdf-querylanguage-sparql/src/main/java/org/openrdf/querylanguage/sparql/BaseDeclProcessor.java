/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.sparql;

import org.openrdf.querylanguage.MalformedQueryException;
import org.openrdf.querylanguage.sparql.ast.ASTBaseDecl;
import org.openrdf.querylanguage.sparql.ast.ASTIRI;
import org.openrdf.querylanguage.sparql.ast.ASTQueryContainer;
import org.openrdf.querylanguage.sparql.ast.VisitorException;
import org.openrdf.util.ParsedURI;

/**
 * @author arjohn
 */
class BaseDeclProcessor {

	public static void process(ASTQueryContainer qc)
		throws MalformedQueryException
	{
		ASTBaseDecl baseDecl = qc.getBaseDecl();
		if (baseDecl != null) {
			ParsedURI parsedBaseIRI = new ParsedURI(baseDecl.getIRI());

			if (!parsedBaseIRI.isAbsolute()) {
				throw new MalformedQueryException("BASE IRI is not an absolute IRI: " + baseDecl.getIRI());
			}

			RelativeIRIResolver visitor = new RelativeIRIResolver(parsedBaseIRI);
			try {
				qc.jjtAccept(visitor, null);
			}
			catch (VisitorException e) {
				throw new MalformedQueryException(e);
			}
		}
	}

	private static class RelativeIRIResolver extends ASTVisitorBase {

		private ParsedURI _parsedBaseIRI;

		public RelativeIRIResolver(ParsedURI parsedBaseIRI) {
			_parsedBaseIRI = parsedBaseIRI;
		}

		@Override
		public Object visit(ASTIRI node, Object data)
			throws VisitorException
		{
			ParsedURI resolvedIRI = _parsedBaseIRI.resolve(node.getValue());
			node.setValue(resolvedIRI.toString());

			return super.visit(node, data);
		}
	}
}
