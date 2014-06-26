package org.openrdf.sesame.spin;

import info.aduna.iteration.Iterations;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class SPINParser {
	private Logger log = LoggerFactory.getLogger(SPINParser.class);
	private SailRepository myRepository;

	public SPINParser(Reader reader, String baseURI, RDFFormat dataFormat)
			throws RDFParseException, RepositoryException, IOException {
		myRepository = new SailRepository(new MemoryStore());
		myRepository.initialize();
		SailRepositoryConnection connection = myRepository.getConnection();
		connection.begin();
		connection.add(reader, baseURI, dataFormat);
		connection.commit();
		connection.close();
	}

	public Multimap<Resource, ParsedQuery> parseConstraint()
			throws MalformedQueryException, MalformedRuleException {
		SailRepositoryConnection connection;
		Multimap<Resource, ParsedQuery> queries = HashMultimap.create(1, 1);
		try {
			connection = myRepository.getConnection();
			recoverConstructQueriesFromTriples(queries, connection);
		} catch (RepositoryException e) {
			log.error("SPIN parsing failed", e);
		}
		return queries;
	}

	private void recoverConstructQueriesFromTriples(
			Multimap<Resource, ParsedQuery> queries,
			RepositoryConnection connection) throws RepositoryException,
			MalformedQueryException, MalformedRuleException {
		RepositoryResult<Statement> queryStarts = connection.getStatements(
				null, RDF.TYPE, SP.CONSTRUCT, true);
		while (queryStarts.hasNext()) {
			Statement queryStart = queryStarts.next();
			recoverConstructQueryFromTriples(queryStart, queries, connection);
		}
		queryStarts.close();
	}

	private void recoverConstructQueryFromTriples(Statement queryStart,
			Multimap<Resource, ParsedQuery> queries,
			RepositoryConnection connection) throws RepositoryException,
			MalformedQueryException, MalformedRuleException {
		List<Statement> list = Iterations.asList(connection.getStatements(null,
				SPIN.CONSTRAINT_PROPERTY, queryStart.getSubject(), true));
		int numAttachedResources = list.size();
		if (1 != numAttachedResources) {
			throw new MalformedRuleException(
					"Expected 1 attached resource, got " + numAttachedResources);
		}
		Resource resource = list.get(0).getSubject();
		List<Literal> texts = findTexts(queryStart.getSubject(), connection);
		for (Literal text : texts) {
			RepositoryResult<Namespace> namespaces = connection.getNamespaces();
			StringBuilder query = new StringBuilder(text.stringValue());
			while (namespaces.hasNext()) {
				Namespace namespace = namespaces.next();
				query.insert(0, ">\n");
				query.insert(0, namespace.getName());
				query.insert(0, ": <");
				query.insert(0, namespace.getPrefix());
				query.insert(0, "PREFIX ");
			}
			queries.put(
					resource,
					QueryParserUtil.parseGraphQuery(QueryLanguage.SPARQL,
							query.toString(), null));
		}
		List<Resource> types = getWhere(queryStart.getSubject(), connection);
		for (Resource where : types) {
			List<Resource> variables = getVariables(where, connection);
			queries.put(resource, new ParsedGraphQuery(new Projection()));
		}
	}

	private List<Resource> getVariables(Resource where,
			RepositoryConnection connection) throws RepositoryException {
		return findSubjects(where, connection, SP.VAR_NAME);
	}

	private List<Resource> getWhere(Resource subject,
			RepositoryConnection connection) throws RepositoryException {
		return findSubjects(subject, connection, SP.WHERE);
	}

	private List<Resource> findSubjects(Resource subject,
			RepositoryConnection connection, URI predicate)
			throws RepositoryException {
		return findSubjects(subject, connection, new ArrayList<Resource>(),
				predicate);
	}

	private List<Literal> findTexts(Resource subject,
			RepositoryConnection connection) throws RepositoryException {
		List<Literal> result = new ArrayList<Literal>();
		RepositoryResult<Statement> queryStarts = connection.getStatements(
				subject, SP.TEXT, null, true);
		while (queryStarts.hasNext()) {
			Statement queryStart = queryStarts.next();
			result.add((Literal) queryStart.getObject());
		}
		return result;
	}

	private List<Resource> findSubjects(Resource subject,
			RepositoryConnection connection, List<Resource> types, URI predicate)
			throws RepositoryException {
		RepositoryResult<Statement> queryStarts = connection.getStatements(
				subject, predicate, null, true);
		while (queryStarts.hasNext()) {
			Statement queryStart = queryStarts.next();
			types.add((Resource) queryStart.getObject());
		}
		return types;
	}

	@Override
	public void finalize() {
		if (myRepository != null) {
			try {
				myRepository.shutDown();
			} catch (RepositoryException e) {
				log.error(
						"Repository shutdown failed in finalizer of SPINParser please make sure to close the parser after use",
						e);
			}
		}
	}
}
