/**
 * (c) 2013 Ontology-Partners Ltd.  All rights reserved.
 * Creator: nickg
 * Created: 1 Aug 2013
 */
package org.openrdf.repository.sail.memory;

import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

public class MemoryStoreStackOverflowTest {

	@Rule
	public Timeout timeout = new Timeout(30000);

	@Test
	public void testStackOverflowWhenGettingLock()
		throws Exception
	{
		String oldProperty = System.setProperty("info.aduna.concurrent.locks.trackLocks", "true");
		try {
			Repository repository = new SailRepository(new MemoryStore());
			repository.initialize();
			try {
				final RepositoryConnection connection = repository.getConnection();
				try {
					Runnable runnable = new Runnable() {

						@Override
						public void run() {
							try {
								connection.hasStatement(null, null, null, true);
							}
							catch (RepositoryException e) {
								throw new RuntimeException(e);
							}
						}
					};
					try {
						runAtStackOverflowDepth(runnable);
						fail("Should overflow stack");
					}
					catch (StackOverflowError e) {
						e.printStackTrace();
						// Expected
						System.out.println("Stack overflowed, as expected");
					}
				}
				finally {
					try {
						if (connection.isActive()) {
							connection.rollback();
						}
					}
					finally {
						System.out.println("Closing connection");
						connection.close();
						System.out.println("Connection closed");
					}
				}
			}
			finally {
				System.out.println("Shutting down repository");
				// Will hang here, because it tries to get a write lock
				repository.shutDown();
				System.out.println("Repository shut down");
			}
		}
		finally {
			if (oldProperty != null) {
				System.setProperty("info.aduna.concurrent.locks.trackLocks", oldProperty);
			}
		}
	}

	private void runAtStackOverflowDepth(Runnable runnable) {
		runnable.run();

		// If did not blow up, recurse...
		runAtStackOverflowDepth(runnable);
	}

}
