package org.openrdf.sail.rdbms.mysql;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestSuite;

import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.rdbms.base.EquivalentTest;


public class MySqlEquivalentTest extends EquivalentTest {

	public static TestSuite suite() throws Exception {
		return EquivalentTest.suite(MySqlEquivalentTest.class);
	}

	public MySqlEquivalentTest() {
		super();
	}

	public MySqlEquivalentTest(String name) {
		super(name);
	}

	@Override
	protected Repository newRepository() {
		//enableLogging("org.openrdf");
		return new SailRepository(new MySqlStore("sesame_test"));
	}

	private void enableLogging(String pkg) {
		Logger logger = Logger.getLogger(pkg);
		ConsoleHandler handler = new ConsoleHandler();
		logger.addHandler(handler);
		handler.setLevel(Level.FINE);
		logger.setLevel(Level.FINE);
	}

}
