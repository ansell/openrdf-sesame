package org.openrdf.repository.optimistic;

import static org.junit.Assert.assertNull;

import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.OptimisticIsolationTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

public class DeadLockTest {
	private Repository repo;
	private RepositoryConnection a;
	private RepositoryConnection b;
	private IsolationLevel level = IsolationLevels.SNAPSHOT_READ;
	private String NS = "http://rdf.example.org/";
	private URI PAINTER;
	private URI PICASSO;
	private URI REMBRANDT;

	@Before
	public void setUp() throws Exception {
		repo = OptimisticIsolationTest.getEmptyInitializedRepository(DeadLockTest.class);
		ValueFactory uf = repo.getValueFactory();
		PAINTER = uf.createURI(NS, "Painter");
		PICASSO = uf.createURI(NS, "picasso");
		REMBRANDT = uf.createURI(NS, "rembrandt");
		a = repo.getConnection();
		b = repo.getConnection();
	}

	@After
	public void tearDown() throws Exception {
		a.close();
		b.close();
		repo.shutDown();
	}

	@Test
	public void test() throws Exception {
		final CountDownLatch start = new CountDownLatch(2);
		final CountDownLatch end = new CountDownLatch(2);
		final CountDownLatch commit = new CountDownLatch(1);
		final Exception e1 = new Exception();
		new Thread(new Runnable() {
			public void run() {
				try {
					start.countDown();
					a.begin(level);
					a.add(PICASSO, RDF.TYPE, PAINTER);
					commit.await();
					a.commit();
				} catch (Exception e) {
					e1.initCause(e);
				} finally {
					end.countDown();
				}
			}
		}).start();
		final Exception e2 = new Exception();
		new Thread(new Runnable() {
			public void run() {
				try {
					start.countDown();
					b.begin(level);
					b.add(REMBRANDT, RDF.TYPE, PAINTER);
					commit.await();
					b.commit();
				} catch (Exception e) {
					e2.initCause(e);
				} finally {
					end.countDown();
				}
			}
		}).start();
		start.await();
		commit.countDown();
		Thread.sleep(500);
		end.await();
		assertNull(e1.getCause());
		assertNull(e2.getCause());
	}

}
