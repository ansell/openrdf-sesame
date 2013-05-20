package org.openrdf.sesame.spin;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
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

	public List<ParsedQuery> parse() {
		SailRepositoryConnection connection;
		List<ParsedQuery> queries = new ArrayList<ParsedQuery>();
		try {
			connection = myRepository.getConnection();

			recoverConstructQueriesFromTriples(queries, connection);

		} catch (RepositoryException e) {
			log.error("SPIN parsing failed", e);
		}

		return queries;
	}

	private void recoverConstructQueriesFromTriples(List<ParsedQuery> queries,
			RepositoryConnection connection) throws RepositoryException {
		RepositoryResult<Statement> queryStarts = connection.getStatements(
				null, RDF.TYPE, SP.CONSTRUCT, true);
		while (queryStarts.hasNext()) {
			Statement queryStart = queryStarts.next();
			recoverConstructQueryFromTriples(queryStart, queries, connection);
		}
		queryStarts.close();
	}

	private void recoverConstructQueryFromTriples(Statement queryStart,
			List<ParsedQuery> queries, RepositoryConnection connection)
			throws RepositoryException {

		List<Resource> types = getWhere(queryStart.getSubject(), connection);
		for (Resource where : types) {
			List<Resource> variables = getVariables(where, connection);
			TupleExpr query = new Projection();

			ParsedGraphQuery parsedGraphQuery = new ParsedGraphQuery(query);
			queries.add(parsedGraphQuery);
		}
	}

	private List<Resource> getVariables(Resource where, RepositoryConnection connection) throws RepositoryException {
		List<Resource> types = new ArrayList<Resource>();
		URI predicate = SP.VAR_NAME;
		return findSubjects(where, connection, types, predicate);
	}

	private List<Resource> getWhere(Resource subject, RepositoryConnection connection)
			throws RepositoryException {
		List<Resource> types = new ArrayList<Resource>();
		URI predicate = SP.WHERE;
		return findSubjects(subject, connection, types, predicate);
	}

	protected List<Resource> findSubjects(Resource subject,
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
