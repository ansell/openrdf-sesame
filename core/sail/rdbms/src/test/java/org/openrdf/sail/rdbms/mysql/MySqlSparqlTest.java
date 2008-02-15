package org.openrdf.sail.rdbms.mysql;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestSuite;

import org.openrdf.query.Dataset;
import org.openrdf.query.parser.sparql.ManifestTest;
import org.openrdf.query.parser.sparql.SPARQLQueryTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.dataset.DatasetRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;

public class MySqlSparqlTest extends SPARQLQueryTest {

	public static class Factory extends SPARQLQueryTest.Factory {
		@Override
		public SPARQLQueryTest createSPARQLQueryTest(String testURI,
				String name, String queryFileURL, String resultFileURL,
				Dataset dataSet) {
			return new MySqlSparqlTest(testURI, name, queryFileURL,
					resultFileURL, dataSet);
		}
	}

	public static TestSuite suite() throws Exception {
		return ManifestTest.suite(new MySqlSparqlTest.Factory());
	}

	public MySqlSparqlTest(String testURI, String name, String queryFileURL,
			String resultFileURL, Dataset dataSet) {
		super(testURI, name, queryFileURL, resultFileURL, dataSet);
	}

	@Override
	protected Repository createRepository() throws RepositoryException {
		//enableLogging("org.openrdf");
		Sail sail = new MySqlStore("sesame_test");
		Repository dataRep = new DatasetRepository(new SailRepository(sail));
		dataRep.initialize();
		RepositoryConnection conn = dataRep.getConnection();
		try {
			conn.clear();
			conn.clearNamespaces();
		} finally {
			conn.close();
		}
		return dataRep;
	}

	private void enableLogging(String pkg) {
		Logger logger = Logger.getLogger(pkg);
		ConsoleHandler handler = new ConsoleHandler();
		logger.addHandler(handler);
		handler.setLevel(Level.FINE);
		logger.setLevel(Level.FINE);
	}

}
