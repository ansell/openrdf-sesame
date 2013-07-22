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

import java.util.concurrent.atomic.AtomicBoolean;

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
	public Timeout timeout = new Timeout(10000);

	private static final String NS = "urn:test:aggregate#";

	private static final String QUERY = "prefix test: <urn:test:aggregate#>\n"
			+ "select ?state (count(?state) as ?count) {\n" + "  ?obj a test:Type .\n" + "  {\n"
			+ "    ?obj test:property ?state .\n" + "  }\n" + "  union\n" + "  {\n" + "    select ?state {\n"
			+ "      ?obj a test:Type .\n" + "      filter not exists { ?obj test:property ?state . }\n"
			+ "      bind(false as ?state)\n" + "    }\n" + "  }\n" + "}\n" + "group by ?state";

	private static final int QUERY_TIME = 500;

	private Repository m_repository;

	@Before
	public void setup()
		throws OpenRDFException
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
				for (int i = 1; i <= 10000; i++) {
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
	public void teardown()
		throws OpenRDFException
	{
		m_repository.shutDown();
	}

	@Test
	public void testAggregateClose()
		throws OpenRDFException
	{
		RepositoryConnection connection = m_repository.getConnection();
		try {
			final TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, QUERY);
			query.setMaxQueryTime(QUERY_TIME);

			final TupleQueryResult[] result = new TupleQueryResult[1];
			final AtomicBoolean stop = new AtomicBoolean(false);
			final long[] times = new long[] { -1, -1 };

			Runnable queryRunner = new Runnable() {

				@Override
				public void run() {
					try {
						synchronized (result) {
							result[0] = query.evaluate();
							System.out.println(">>>>>>>> query evaluating");
						}

						while (result[0].hasNext()) {
							System.out.println(">>>>>>>> query found result");
							result[0].next();
						}

						times[0] = System.currentTimeMillis();
						System.out.println(">>>>>>>> query finished");

						try {
							result[0].close();
						}
						catch (QueryEvaluationException ex) {
							ex.printStackTrace();
						}
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
					finally {
						stop.set(true);
					}
				}
			};

			Runnable closeRunner = new Runnable() {

				@Override
				public void run() {
					System.out.println("<<<<<<<<< waiting for query");
					boolean doClose = false;
					while (true) {
						if (stop.get()) {
							break;
						}

						synchronized (result) {
							if (result[0] != null) {
								doClose = true;
								break;
							}
						}

						sleep(100);
					}

					if (doClose) {
						sleep(200);
						try {
							System.out.println("<<<<<<<<< closing query");

							result[0].close();
							times[1] = System.currentTimeMillis();
							System.out.println("<<<<<<<<< query closed");
							stop.set(true);
						}
						catch (QueryEvaluationException ex) {
							ex.printStackTrace();
						}
					}
					else {
						stop.set(true);
					}
				}
			};

			run(queryRunner, "<QUERY>");
			run(closeRunner, "<CLOSER>");

			long start = System.currentTimeMillis();
			while (!stop.get()) {
				sleep(100);
			}

			System.out.printf("QUERY RUNNER: took = %s\n", times[0] - start);
			System.out.printf("CLOSE RUNNER: took = %s\n", times[1] - start);

			assertTrue("the query should have been closed within the query timeout", times[0] < QUERY_TIME);
			assertEquals("the query runner should not have set an end time as it should have been cancelled",
					-1, times[0]);
		}
		finally {
			connection.close();
		}
	}

	private static void sleep(long time) {
		try {
			Thread.sleep(time);
		}
		catch (InterruptedException ex) { /* .... */
		}
	}

	private static void run(Runnable runnable, String name) {
		Thread thread = new Thread(runnable);
		thread.setName(name);
		thread.start();
	}
}
