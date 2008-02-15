package org.openrdf.sail.rdbms.mysql;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.sail.RDFStoreTest;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

public class MySqlTest extends RDFStoreTest {

	public MySqlTest(String name) {
		super(name);
	}

	@Override
	protected Sail createSail() throws SailException {
		enableLogging("org.openrdf");
		MySqlStore sail = new MySqlStore("sesame_test");
		sail.initialize();
		SailConnection conn = sail.getConnection();
		try {
			conn.clear();
			conn.clearNamespaces();
			conn.commit();
		} finally {
			conn.close();
		}
		return sail;
	}

	private void enableLogging(String pkg) {
		Logger logger = Logger.getLogger(pkg);
		ConsoleHandler handler = new ConsoleHandler();
		logger.addHandler(handler);
		handler.setLevel(Level.FINE);
		logger.setLevel(Level.FINE);
	}

}
