/**
 * (c) 2013 Ontology-Partners Ltd.  All rights reserved.
 * Creator: rory
 * Created: 22 Jul 2013
 */

package org.openrdf.repository;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.concurrent.CountDownLatch;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 * @author rory
 */
public class AggregateLockTest {

	@Rule
	public Timeout timeout = new Timeout(30000);

	private static final String NS = "urn:test:aggregate#";

	private static final String QUERY = "prefix test: <urn:test:aggregate#>\n"
			+ "select ?state (count(?state) as ?count) {\n" + "  ?obj a test:Type .\n" + "  {\n"
			+ "    ?obj test:property ?state .\n" + "  }\n" + "  union\n" + "  {\n" + "    select ?state {\n"
			+ "      ?obj a test:Type .\n" + "      filter not exists { ?obj test:property ?state . }\n"
			+ "      bind(false as ?state)\n" + "    }\n" + "  }\n" + "}\n" + "group by ?state";

	private static final int QUERY_TIME = 500;

	private Repository m_repository;

	@Before
	public void setUp()
		throws Exception
	{
		m_repository = new SailRepository(new MemoryStore());
		m_repository.initialize();

		RepositoryConnection connection = m_repository.getConnection();
		try {
			connection.begin();
			ValueFactory factory = connection.getValueFactory();
			URI type = factory.createURI(NS, "Type");
			URI property = factory.createURI(NS, "property");

			try {
				for (int i = 1; i <= 1000; i++) {
					URI thing = factory.createURI(NS, "Thing" + i);
					connection.add(thing, RDF.TYPE, type);

					if (i % 7 == 0) {
						connection.add(thing, property, factory.createLiteral(i));
					}
				}
			}
			finally {
				connection.commit();
			}
		}
		finally {
			connection.close();
		}
	}

	@After
	public void tearDown()
		throws Exception
	{
		m_repository.shutDown();
	}

	@Test
	public void testAggregateClose()
		throws Exception
	{
		RepositoryConnection connection = m_repository.getConnection();
		try {
			final TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, QUERY);
			query.setMaxQueryTime(QUERY_TIME);

			final TupleQueryResult[] result = new TupleQueryResult[1];
			final CountDownLatch startLatch = new CountDownLatch(1);
			final CountDownLatch closeLatch = new CountDownLatch(1);
			final CountDownLatch finishLatch = new CountDownLatch(1);
			final long[] times = new long[] { -1, -1, -1, -1 };

			Runnable queryRunner = new Runnable() {

				@Override
				public void run() {
					try {
						startLatch.await();
						System.out.println(">>>>>>>> query evaluating");
						times[0] = System.currentTimeMillis();
						result[0] = query.evaluate();
						times[1] = System.currentTimeMillis();

						System.out.println(">>>>>>>> query evaluation complete in : " + (times[1] - times[0]));
						
						while (result[0].hasNext()) {
							System.out.println(">>>>>>>> query found result : time since query evaluation = "
									+ (System.currentTimeMillis() - times[1]));
							result[0].next();
						}

						System.out.println(">>>>>>>> query result iteration finished");
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
					finally {
						closeLatch.countDown();
					}
				}
			};

			Runnable closeRunner = new Runnable() {

				@Override
				public void run() {
					try {
						System.out.println("<<<<<<<<< waiting for query result iteration to complete");
						closeLatch.await();

						System.out.println("<<<<<<<<< closing query");

						times[2] = System.currentTimeMillis();
						result[0].close();
						times[3] = System.currentTimeMillis();
						System.out.println("<<<<<<<<< query closed");

					}
					catch (QueryEvaluationException ex) {
						ex.printStackTrace();
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					finally {
						finishLatch.countDown();
					}
				}
			};

			run(queryRunner, "<QUERY>");
			run(closeRunner, "<CLOSER>");

			startLatch.countDown();

			finishLatch.await();

			assertTrue("the query should have been evaluated within the query timeout",
					(times[1] - times[0]) < QUERY_TIME);
			// assertEquals("the query runner should not have set an end time as it should have been cancelled",
			// -1, times[0]);
		}
		finally {
			connection.close();
		}
	}

	private static void run(Runnable runnable, String name) {
		Thread thread = new Thread(runnable);
		thread.setName(name);
		thread.start();
	}
}
