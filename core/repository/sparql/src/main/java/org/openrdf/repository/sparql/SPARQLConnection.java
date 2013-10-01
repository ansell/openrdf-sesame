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
package org.openrdf.repository.sparql;

import static org.openrdf.query.QueryLanguage.SPARQL;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.ExceptionConvertingIteration;
import info.aduna.iteration.SingletonIteration;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.util.Literals;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.query.parser.sparql.SPARQLUtil;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.UnknownTransactionStateException;
import org.openrdf.repository.base.RepositoryConnectionBase;
import org.openrdf.repository.sparql.query.SPARQLBooleanQuery;
import org.openrdf.repository.sparql.query.SPARQLGraphQuery;
import org.openrdf.repository.sparql.query.SPARQLTupleQuery;
import org.openrdf.repository.sparql.query.SPARQLUpdate;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.repository.util.RDFLoader;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * Provides a {@link RepositoryConnection} interface to any SPARQL endpoint.
 * 
 * @author James Leigh
 */
public class SPARQLConnection extends RepositoryConnectionBase {

	private static final String EVERYTHING = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";

	private static final String SOMETHING = "ASK { ?s ?p ?o }";

	private static final String NAMEDGRAPHS = "SELECT DISTINCT ?_ WHERE { GRAPH ?_ { ?s ?p ?o } }";

	private StringBuffer sparqlTransaction;

	private Object transactionLock = new Object();

	public SPARQLConnection(SPARQLRepository repository) {
		super(repository);
	}

	@Override
	public String toString() {
		return getRepository().getHTTPClient().getQueryURL();
	}

	public void exportStatements(Resource subj, URI pred, Value obj, boolean includeInferred,
			RDFHandler handler, Resource... contexts)
		throws RepositoryException, RDFHandlerException
	{
		try {
			GraphQuery query = prepareGraphQuery(SPARQL, EVERYTHING, "");
			setBindings(query, subj, pred, obj, contexts);
			query.evaluate(handler);
		}
		catch (MalformedQueryException e) {
			throw new RepositoryException(e);
		}
		catch (QueryEvaluationException e) {
			throw new RepositoryException(e);
		}
	}

	public RepositoryResult<Resource> getContextIDs()
		throws RepositoryException
	{
		try {
			TupleQuery query = prepareTupleQuery(SPARQL, NAMEDGRAPHS, "");
			TupleQueryResult result = query.evaluate();
			return new RepositoryResult<Resource>(
					new ExceptionConvertingIteration<Resource, RepositoryException>(
							new ConvertingIteration<BindingSet, Resource, QueryEvaluationException>(result) {

								@Override
								protected Resource convert(BindingSet bindings)
									throws QueryEvaluationException
								{
									return (Resource)bindings.getValue("_");
								}
							})
					{

						@Override
						protected RepositoryException convert(Exception e) {
							return new RepositoryException(e);
						}
					});
		}
		catch (MalformedQueryException e) {
			throw new RepositoryException(e);
		}
		catch (QueryEvaluationException e) {
			throw new RepositoryException(e);
		}
	}

	public String getNamespace(String prefix)
		throws RepositoryException
	{
		return null;
	}

	public RepositoryResult<Namespace> getNamespaces()
		throws RepositoryException
	{
		return new RepositoryResult<Namespace>(new EmptyIteration<Namespace, RepositoryException>());
	}

	public long size(Resource... contexts)
		throws RepositoryException
	{
		RepositoryResult<Statement> stmts = getStatements(null, null, null, true, contexts);
		try {
			long i = 0;
			while (stmts.hasNext()) {
				stmts.next();
				i++;
			}
			return i;
		}
		finally {
			stmts.close();
		}
	}

	public RepositoryResult<Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws RepositoryException
	{
		try {
			if (subj != null && pred != null && obj != null) {
				if (hasStatement(subj, pred, obj, includeInferred, contexts)) {
					Statement st = new StatementImpl(subj, pred, obj);
					CloseableIteration<Statement, RepositoryException> cursor;
					cursor = new SingletonIteration<Statement, RepositoryException>(st);
					return new RepositoryResult<Statement>(cursor);
				}
				else {
					return new RepositoryResult<Statement>(new EmptyIteration<Statement, RepositoryException>());
				}
			}
			GraphQuery query = prepareGraphQuery(SPARQL, EVERYTHING, "");
			setBindings(query, subj, pred, obj, contexts);
			GraphQueryResult result = query.evaluate();
			return new RepositoryResult<Statement>(
					new ExceptionConvertingIteration<Statement, RepositoryException>(result) {

						@Override
						protected RepositoryException convert(Exception e) {
							return new RepositoryException(e);
						}
					});
		}
		catch (MalformedQueryException e) {
			throw new RepositoryException(e);
		}
		catch (QueryEvaluationException e) {
			throw new RepositoryException(e);
		}
	}

	public boolean hasStatement(Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
		throws RepositoryException
	{
		try {
			BooleanQuery query = prepareBooleanQuery(SPARQL, SOMETHING, "");
			setBindings(query, subj, pred, obj, contexts);
			return query.evaluate();
		}
		catch (MalformedQueryException e) {
			throw new RepositoryException(e);
		}
		catch (QueryEvaluationException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public SPARQLRepository getRepository() {
		return (SPARQLRepository)super.getRepository();
	}

	public Query prepareQuery(QueryLanguage ql, String query, String base)
		throws RepositoryException, MalformedQueryException
	{
		if (SPARQL.equals(ql)) {
			String strippedQuery = QueryParserUtil.removeSPARQLQueryProlog(query).toUpperCase();
			if (strippedQuery.startsWith("SELECT")) {
				return prepareTupleQuery(ql, query, base);
			}
			else if (strippedQuery.startsWith("ASK")) {
				return prepareBooleanQuery(ql, query, base);
			}
			else {
				return prepareGraphQuery(ql, query, base);
			}
		}
		throw new UnsupportedOperationException("Unsupported query language " + ql);
	}

	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query, String base)
		throws RepositoryException, MalformedQueryException
	{
		if (SPARQL.equals(ql)) {
			return new SPARQLBooleanQuery(getRepository().getHTTPClient(), base, query);
		}
		throw new UnsupportedQueryLanguageException("Unsupported query language " + ql);
	}

	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query, String base)
		throws RepositoryException, MalformedQueryException
	{
		if (SPARQL.equals(ql)) {
			return new SPARQLGraphQuery(getRepository().getHTTPClient(), base, query);
		}
		throw new UnsupportedQueryLanguageException("Unsupported query language " + ql);
	}

	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query, String base)
		throws RepositoryException, MalformedQueryException
	{
		if (SPARQL.equals(ql))
			return new SPARQLTupleQuery(getRepository().getHTTPClient(), base, query);
		throw new UnsupportedQueryLanguageException("Unsupported query language " + ql);
	}

	public void commit()
		throws RepositoryException
	{
		synchronized (transactionLock) {
			if (isActive()) {
				synchronized (transactionLock) {
					SPARQLUpdate transaction = new SPARQLUpdate(getRepository().getHTTPClient(), null,
							sparqlTransaction.toString());
					try {
						transaction.execute();
					}
					catch (UpdateExecutionException e) {
						throw new RepositoryException("error executing transaction", e);
					}

					sparqlTransaction = null;
				}
			}
			else {
				throw new RepositoryException("no transaction active.");
			}
		}
	}

	public void rollback()
		throws RepositoryException
	{
		synchronized (transactionLock) {
			if (isActive()) {
				synchronized (transactionLock) {
					sparqlTransaction = null;
				}
			}
			else {
				throw new RepositoryException("no transaction active.");
			}
		}
	}

	public void begin()
		throws RepositoryException
	{
		synchronized (transactionLock) {
			if (!isActive()) {
				synchronized (transactionLock) {
					sparqlTransaction = new StringBuffer();
				}
			}
			else {
				throw new RepositoryException("active transaction already exists");
			}
		}
	}

	@Override
	public void add(File file, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		// to preserve bnode identity, we need to make sure all statements are
		// processed in a single INSERT DATA command
		StatementCollector collector = new StatementCollector();

		boolean localTransaction = startLocalTransaction();

		try {
			RDFLoader loader = new RDFLoader(getParserConfig(), getValueFactory());
			loader.load(file, baseURI, dataFormat, collector);
			add(collector.getStatements(), contexts);
			conditionalCommit(localTransaction);
		}
		catch (RDFHandlerException e) {
			conditionalRollback(localTransaction);

			// RDFInserter only throws wrapped RepositoryExceptions
			throw (RepositoryException)e.getCause();
		}
		catch (RDFParseException e) {
			conditionalRollback(localTransaction);
			throw e;
		}
		catch (IOException e) {
			conditionalRollback(localTransaction);
			throw e;
		}
		catch (RuntimeException e) {
			conditionalRollback(localTransaction);
			throw e;
		}
	}

	@Override
	public void add(URL url, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		// to preserve bnode identity, we need to make sure all statements are
		// processed in a single INSERT DATA command
		StatementCollector collector = new StatementCollector();
		boolean localTransaction = startLocalTransaction();

		try {
			RDFLoader loader = new RDFLoader(getParserConfig(), getValueFactory());
			loader.load(url, baseURI, dataFormat, collector);
			add(collector.getStatements(), contexts);
			conditionalCommit(localTransaction);
		}
		catch (RDFHandlerException e) {
			conditionalRollback(localTransaction);

			// RDFInserter only throws wrapped RepositoryExceptions
			throw (RepositoryException)e.getCause();
		}
		catch (RDFParseException e) {
			conditionalRollback(localTransaction);
			throw e;
		}
		catch (IOException e) {
			conditionalRollback(localTransaction);
			throw e;
		}
		catch (RuntimeException e) {
			conditionalRollback(localTransaction);
			throw e;
		}
	}

	@Override
	public void add(InputStream in, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		// to preserve bnode identity, we need to make sure all statements are
		// processed in a single INSERT DATA command
		StatementCollector collector = new StatementCollector();

		boolean localTransaction = startLocalTransaction();

		try {
			RDFLoader loader = new RDFLoader(getParserConfig(), getValueFactory());
			loader.load(in, baseURI, dataFormat, collector);
			add(collector.getStatements(), contexts);
			conditionalCommit(localTransaction);
		}
		catch (RDFHandlerException e) {
			conditionalRollback(localTransaction);

			// RDFInserter only throws wrapped RepositoryExceptions
			throw (RepositoryException)e.getCause();
		}
		catch (RDFParseException e) {
			conditionalRollback(localTransaction);
			throw e;
		}
		catch (IOException e) {
			conditionalRollback(localTransaction);
			throw e;
		}
		catch (RuntimeException e) {
			conditionalRollback(localTransaction);
			throw e;
		}
	}

	@Override
	public void add(Reader reader, String baseURI, RDFFormat dataFormat, Resource... contexts)
		throws IOException, RDFParseException, RepositoryException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		// to preserve bnode identity, we need to make sure all statements are
		// processed in a single INSERT DATA command
		StatementCollector collector = new StatementCollector();

		boolean localTransaction = startLocalTransaction();

		try {
			RDFLoader loader = new RDFLoader(getParserConfig(), getValueFactory());
			loader.load(reader, baseURI, dataFormat, collector);

			add(collector.getStatements(), contexts);
			conditionalCommit(localTransaction);
		}
		catch (RDFHandlerException e) {
			conditionalRollback(localTransaction);

			// RDFInserter only throws wrapped RepositoryExceptions
			throw (RepositoryException)e.getCause();
		}
		catch (RDFParseException e) {
			conditionalRollback(localTransaction);
			throw e;
		}
		catch (IOException e) {
			conditionalRollback(localTransaction);
			throw e;
		}
		catch (RuntimeException e) {
			conditionalRollback(localTransaction);
			throw e;
		}
	}

	public void add(Statement st, Resource... contexts)
		throws RepositoryException
	{
		boolean localTransaction = startLocalTransaction();

		List<Statement> list = new ArrayList<Statement>(1);
		list.add(st);
		String sparqlCommand = createInsertDataCommand(list, contexts);

		sparqlTransaction.append(sparqlCommand);
		sparqlTransaction.append("; ");

		try {
			conditionalCommit(localTransaction);
		}
		catch (RepositoryException e) {
			conditionalRollback(localTransaction);
			throw e;
		}
	}

	public void add(Iterable<? extends Statement> statements, Resource... contexts)
		throws RepositoryException
	{
		boolean localTransaction = startLocalTransaction();

		String sparqlCommand = createInsertDataCommand(statements, contexts);

		sparqlTransaction.append(sparqlCommand);
		sparqlTransaction.append("; ");

		try {
			conditionalCommit(localTransaction);
		}
		catch (RepositoryException e) {
			conditionalRollback(localTransaction);
			throw e;
		}
	}

	public void clear(Resource... contexts)
		throws RepositoryException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);
		boolean localTransaction = startLocalTransaction();

		if (contexts.length == 0) {
			sparqlTransaction.append("CLEAR ALL ");
			sparqlTransaction.append("; ");
		}
		else {
			for (Resource context : contexts) {
				if (context == null) {
					sparqlTransaction.append("CLEAR DEFAULT ");
					sparqlTransaction.append("; ");
				}
				else if (context instanceof URI) {
					sparqlTransaction.append("CLEAR GRAPH <" + context.stringValue() + "> ");
					sparqlTransaction.append("; ");
				}
				else {
					throw new RepositoryException(
							"SPARQL does not support named graphs identified by blank nodes.");
				}
			}
		}

		try {
			conditionalCommit(localTransaction);
		}
		catch (RepositoryException e) {
			conditionalRollback(localTransaction);
			throw e;
		}
	}

	public void clearNamespaces()
		throws RepositoryException
	{
		// silently ignore

		// throw new UnsupportedOperationException();
	}

	public void remove(Statement st, Resource... contexts)
		throws RepositoryException
	{
		boolean localTransaction = startLocalTransaction();

		List<Statement> list = new ArrayList<Statement>(1);
		list.add(st);
		String sparqlCommand = createDeleteDataCommand(list, contexts);

		sparqlTransaction.append(sparqlCommand);
		sparqlTransaction.append("; ");

		try {
			conditionalCommit(localTransaction);
		}
		catch (RepositoryException e) {
			conditionalRollback(localTransaction);
			throw e;
		}

	}

	public void remove(Iterable<? extends Statement> statements, Resource... contexts)
		throws RepositoryException
	{
		boolean localTransaction = startLocalTransaction();

		String sparqlCommand = createDeleteDataCommand(statements, contexts);

		sparqlTransaction.append(sparqlCommand);
		sparqlTransaction.append("; ");

		try {
			conditionalCommit(localTransaction);
		}
		catch (RepositoryException e) {
			conditionalRollback(localTransaction);
			throw e;
		}
	}

	public void removeNamespace(String prefix)
		throws RepositoryException
	{
		// no-op, ignore silently
		// throw new UnsupportedOperationException();
	}

	public void setNamespace(String prefix, String name)
		throws RepositoryException
	{
		// no-op, ignore silently
		// throw new UnsupportedOperationException();
	}

	public Update prepareUpdate(QueryLanguage ql, String update, String baseURI)
		throws RepositoryException, MalformedQueryException
	{
		if (SPARQL.equals(ql)) {
			return new SPARQLUpdate(getRepository().getHTTPClient(), baseURI, update);
		}
		throw new UnsupportedQueryLanguageException("Unsupported query language " + ql);
	}

	/* protected/private methods */

	private void setBindings(Query query, Resource subj, URI pred, Value obj, Resource... contexts)
		throws RepositoryException
	{
		if (subj != null) {
			query.setBinding("s", subj);
		}
		if (pred != null) {
			query.setBinding("p", pred);
		}
		if (obj != null) {
			query.setBinding("o", obj);
		}
		if (contexts != null && contexts.length > 0) {
			DatasetImpl dataset = new DatasetImpl();
			for (Resource ctx : contexts) {
				if (ctx == null || ctx instanceof URI) {
					dataset.addDefaultGraph((URI)ctx);
				}
				else {
					throw new RepositoryException("Contexts must be URIs");
				}
			}
			query.setDataset(dataset);
		}
	}

	private String createInsertDataCommand(Iterable<? extends Statement> statements, Resource... contexts) {
		StringBuilder qb = new StringBuilder();
		qb.append("INSERT DATA \n");
		qb.append("{ \n");
		if (contexts.length > 0) {
			for (Resource context : contexts) {
				if (context != null) {
					String namedGraph = context.stringValue();
					if (context instanceof BNode) {
						// SPARQL does not allow blank nodes as named graph
						// identifiers, so we need to skolemize
						// the blank node id.
						namedGraph = "urn:nodeid:" + context.stringValue();
					}
					qb.append("    GRAPH <" + namedGraph + "> { \n");
				}
				createDataBody(qb, statements);
				if (context != null && context instanceof URI) {
					qb.append(" } \n");
				}
			}
		}
		else {
			createDataBody(qb, statements);
		}
		qb.append("}");

		return qb.toString();
	}

	private String createDeleteDataCommand(Iterable<? extends Statement> statements, Resource... contexts) {
		StringBuilder qb = new StringBuilder();
		qb.append("DELETE DATA \n");
		qb.append("{ \n");
		if (contexts.length > 0) {
			for (Resource context : contexts) {
				if (context != null) {
					String namedGraph = context.stringValue();
					if (context instanceof BNode) {
						// SPARQL does not allow blank nodes as named graph
						// identifiers, so we need to skolemize
						// the blank node id.
						namedGraph = "urn:nodeid:" + context.stringValue();
					}
					qb.append("    GRAPH <" + namedGraph + "> { \n");
				}
				createDataBody(qb, statements);
				if (context != null && context instanceof URI) {
					qb.append(" } \n");
				}
			}
		}
		else {
			createDataBody(qb, statements);
		}
		qb.append("}");

		return qb.toString();
	}

	private void createDataBody(StringBuilder qb, Iterable<? extends Statement> statements) {
		for (Statement st : statements) {
			if (st.getSubject() instanceof BNode) {
				qb.append("_:" + st.getSubject().stringValue() + " ");
			}
			else {
				qb.append("<" + st.getSubject().stringValue() + "> ");
			}

			qb.append("<" + st.getPredicate().stringValue() + "> ");

			if (st.getObject() instanceof Literal) {
				Literal lit = (Literal)st.getObject();
				qb.append("\"");
				qb.append(SPARQLUtil.encodeString(lit.getLabel()));
				qb.append("\"");

				if (Literals.isLanguageLiteral(lit)) {
					qb.append("@");
					qb.append(lit.getLanguage());
				}
				else {
					qb.append("^^<" + lit.getDatatype().stringValue() + ">");
				}
				qb.append(" ");
			}
			else if (st.getObject() instanceof BNode) {
				qb.append("_:" + st.getObject().stringValue() + " ");
			}
			else {
				qb.append("<" + st.getObject().stringValue() + "> ");
			}
			qb.append(". \n");
		}
	}

	public boolean isActive()
		throws UnknownTransactionStateException, RepositoryException
	{
		synchronized (transactionLock) {
			return sparqlTransaction != null;
		}
	}

	@Override
	protected void addWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		ValueFactory f = getValueFactory();

		Statement st = f.createStatement(subject, predicate, object);

		List<Statement> list = new ArrayList<Statement>(1);
		list.add(st);
		String sparqlCommand = createInsertDataCommand(list, contexts);

		sparqlTransaction.append(sparqlCommand);
		sparqlTransaction.append("; ");
	}

	@Override
	protected void removeWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		String sparqlCommand = "";
		if (subject != null && predicate != null && object != null) {
			ValueFactory f = getValueFactory();

			Statement st = f.createStatement(subject, predicate, object);

			List<Statement> list = new ArrayList<Statement>(1);
			list.add(st);
			sparqlCommand = createDeleteDataCommand(list, contexts);
		}
		else {
			sparqlCommand = createDeletePatternCommand(subject, predicate, object, contexts);
		}

		sparqlTransaction.append(sparqlCommand);
		sparqlTransaction.append("; ");
	}

	/**
	 * @param subject
	 * @param predicate
	 * @param object
	 * @param contexts
	 * @return
	 */
	private String createDeletePatternCommand(Resource subject, URI predicate, Value object,
			Resource[] contexts)
	{
		StringBuilder qb = new StringBuilder();
		qb.append("DELETE WHERE \n");
		qb.append("{ \n");
		if (contexts.length > 0) {
			for (Resource context : contexts) {
				if (context != null) {
					String namedGraph = context.stringValue();
					if (context instanceof BNode) {
						// SPARQL does not allow blank nodes as named graph
						// identifiers, so we need to skolemize
						// the blank node id.
						namedGraph = "urn:nodeid:" + context.stringValue();
					}
					qb.append("    GRAPH <" + namedGraph + "> { \n");
				}
				createBGP(qb, subject, predicate, object);
				if (context != null && context instanceof URI) {
					qb.append(" } \n");
				}
			}
		}
		else {
			createBGP(qb, subject, predicate, object);
		}
		qb.append("}");

		return qb.toString();
	}

	private void createBGP(StringBuilder qb, Resource subject, URI predicate, Value object) {
		if (subject != null) {
			if (subject instanceof BNode) {
				qb.append("_:" + subject.stringValue() + " ");
			}
			else {
				qb.append("<" + subject.stringValue() + "> ");
			}
		}
		else {
			qb.append("?subj");
		}

		if (predicate != null) {
			qb.append("<" + predicate.stringValue() + "> ");
		}
		else {
			qb.append("?pred");
		}

		if (object != null) {
			if (object instanceof Literal) {
				Literal lit = (Literal)object;
				qb.append("\"");
				qb.append(SPARQLUtil.encodeString(lit.getLabel()));
				qb.append("\"");

				if (Literals.isLanguageLiteral(lit)) {
					qb.append("@");
					qb.append(lit.getLanguage());
				}
				else {
					qb.append("^^<" + lit.getDatatype().stringValue() + ">");
				}
				qb.append(" ");
			}
			else if (object instanceof BNode) {
				qb.append("_:" + object.stringValue() + " ");
			}
			else {
				qb.append("<" + object.stringValue() + "> ");
			}
		}
		else {
			qb.append("?obj");
		}
		qb.append(". \n");
	}
}
