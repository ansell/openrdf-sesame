/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.console;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import info.aduna.text.StringUtil;

import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.ntriples.NTriplesUtil;

/**
 * @author dale
 */
public class TupleAndGraphQueryEvaluator {

	private final ConsoleIO consoleIO;

	private final ConsoleState state;

	private final ConsoleParameters parameters;

	TupleAndGraphQueryEvaluator(ConsoleIO consoleIO, ConsoleState state, ConsoleParameters parameters) {
		this.consoleIO = consoleIO;
		this.state = state;
		this.parameters = parameters;
	}

	protected void evaluateTupleQuery(final QueryLanguage queryLn, final String queryString)
		throws UnsupportedQueryLanguageException, MalformedQueryException, QueryEvaluationException,
		RepositoryException
	{
		Repository repository = state.getRepository();
		if (repository == null) {
			consoleIO.writeUnopenedError();
			return;
		}
		final RepositoryConnection con = repository.getConnection();
		try {
			consoleIO.writeln("Evaluating query...");
			final long startTime = System.nanoTime();
			final TupleQueryResult tupleQueryResult = con.prepareTupleQuery(queryLn, queryString).evaluate();
			try {
				int resultCount = 0;
				final List<String> bindingNames = tupleQueryResult.getBindingNames();
				if (bindingNames.isEmpty()) {
					while (tupleQueryResult.hasNext()) {
						tupleQueryResult.next();
						resultCount++;
					}
				}
				else {
					int consoleWidth = parameters.getWidth();
					final int columnWidth = (consoleWidth - 1) / bindingNames.size() - 3;

					// Build table header
					final StringBuilder builder = new StringBuilder(consoleWidth);
					for (String bindingName : bindingNames) {
						builder.append("| ").append(bindingName);
						StringUtil.appendN(' ', columnWidth - bindingName.length(), builder);
					}
					builder.append("|");
					final String header = builder.toString();

					// Build separator line
					builder.setLength(0);
					for (int i = bindingNames.size(); i > 0; i--) {
						builder.append('+');
						StringUtil.appendN('-', columnWidth + 1, builder);
					}
					builder.append('+');
					final String separatorLine = builder.toString();

					// consoleIO.write table header
					consoleIO.writeln(separatorLine);
					consoleIO.writeln(header);
					consoleIO.writeln(separatorLine);

					// consoleIO.write table rows
					final Collection<Namespace> namespaces = con.getNamespaces().addTo(new ArrayList<Namespace>());
					while (tupleQueryResult.hasNext()) {
						final BindingSet bindingSet = tupleQueryResult.next();
						resultCount++;
						builder.setLength(0);
						for (String bindingName : bindingNames) {
							final Value value = bindingSet.getValue(bindingName);
							final String valueStr = getStringRepForValue(value, namespaces);
							builder.append("| ").append(valueStr);
							StringUtil.appendN(' ', columnWidth - valueStr.length(), builder);
						}
						builder.append("|");
						consoleIO.writeln(builder.toString());
					}
					consoleIO.writeln(separatorLine);
				}
				final long endTime = System.nanoTime();
				consoleIO.writeln(resultCount + " result(s) (" + (endTime - startTime) / 1000000 + " ms)");
			}
			finally {
				tupleQueryResult.close();
			}
		}
		finally {
			con.close();
		}
	}

	protected void evaluateGraphQuery(final QueryLanguage queryLn, final String queryString)
		throws UnsupportedQueryLanguageException, MalformedQueryException, QueryEvaluationException,
		RepositoryException
	{
		Repository repository = state.getRepository();
		if (repository == null) {
			consoleIO.writeUnopenedError();
			return;
		}
		final RepositoryConnection con = repository.getConnection();
		try {
			consoleIO.writeln("Evaluating query...");
			final long startTime = System.nanoTime();
			final Collection<Namespace> namespaces = con.getNamespaces().addTo(new ArrayList<Namespace>());
			final GraphQueryResult queryResult = con.prepareGraphQuery(queryLn, queryString).evaluate();
			try {
				int resultCount = 0;
				while (queryResult.hasNext()) {
					final Statement statement = queryResult.next(); // NOPMD
					resultCount++;
					consoleIO.write(getStringRepForValue(statement.getSubject(), namespaces));
					consoleIO.write("   ");
					consoleIO.write(getStringRepForValue(statement.getPredicate(), namespaces));
					consoleIO.write("   ");
					consoleIO.write(getStringRepForValue(statement.getObject(), namespaces));
					consoleIO.writeln();
				}
				final long endTime = System.nanoTime();
				consoleIO.writeln(resultCount + " results (" + (endTime - startTime) / 1000000 + " ms)");
			}
			finally {
				queryResult.close();
			}
		}
		finally {
			con.close();
		}
	}

	private String getStringRepForValue(final Value value, final Collection<Namespace> namespaces) {
		String result = "";
		if (value != null) {
			if (parameters.isShowPrefix() && value instanceof URI) {
				final URI uri = (URI)value;
				final String prefix = getPrefixForNamespace(uri.getNamespace(), namespaces);
				if (prefix == null) {
					result = NTriplesUtil.toNTriplesString(value);
				}
				else {
					result = prefix + ":" + uri.getLocalName();
				}
			}
			else {
				result = NTriplesUtil.toNTriplesString(value);
			}
		}
		return result;
	}

	private String getPrefixForNamespace(final String namespace, final Collection<Namespace> namespaces) {
		String result = null;
		for (Namespace ns : namespaces) {
			if (namespace.equals(ns.getName())) {
				result = ns.getPrefix();
				break;
			}
		}
		return result;
	}

}
