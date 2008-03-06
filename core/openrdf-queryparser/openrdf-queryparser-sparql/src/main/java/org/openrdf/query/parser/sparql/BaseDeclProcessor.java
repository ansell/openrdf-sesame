/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import info.aduna.net.ParsedURI;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.sparql.ast.ASTBaseDecl;
import org.openrdf.query.parser.sparql.ast.ASTIRI;
import org.openrdf.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.query.parser.sparql.ast.VisitorException;

/**
 * Resolves relative URIs in a query model using either an external base URI or
 * using the base URI specified in the query model itself. The former takes
 * precedence over the latter.
 * 
 * @author Arjohn Kampman
 */
class BaseDeclProcessor {

	/**
	 * Resolves relative URIs in the supplied query model using either the
	 * specified <tt>externalBaseURI</tt> or, if this parameter is
	 * <tt>null</tt>, the base URI specified in the query model itself.
	 * 
	 * @param qc
	 *        The query model to resolve relative URIs in.
	 * @param externalBaseURI
	 *        The external base URI to use for resolving relative URIs, or
	 *        <tt>null</tt> if the base URI that is specified in the query
	 *        model should be used.
	 * @throws IllegalArgumentException
	 *         If an external base URI is specified that is not an absolute URI.
	 * @throws MalformedQueryException
	 *         If the base URI specified in the query model is not an absolute
	 *         URI.
	 */
	public static void process(ASTQueryContainer qc, String externalBaseURI)
		throws MalformedQueryException
	{
		ParsedURI parsedBaseURI = null;

		if (externalBaseURI != null) {
			parsedBaseURI = new ParsedURI(externalBaseURI);

			if (!parsedBaseURI.isAbsolute()) {
				throw new IllegalArgumentException("Supplied base URI is not an absolute IRI: " + externalBaseURI);
			}
		}
		else {
			// Use the query model's own base URI, if available
			ASTBaseDecl baseDecl = qc.getBaseDecl();

			if (baseDecl != null) {
				parsedBaseURI = new ParsedURI(baseDecl.getIRI());

				if (!parsedBaseURI.isAbsolute()) {
					throw new MalformedQueryException("BASE IRI is not an absolute IRI: " + externalBaseURI);
				}
			}
			else {
				// FIXME: use the "Default Base URI"?
			}
		}

		if (parsedBaseURI != null) {
			ParsedURI parsedBaseIRI = new ParsedURI(externalBaseURI);

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
