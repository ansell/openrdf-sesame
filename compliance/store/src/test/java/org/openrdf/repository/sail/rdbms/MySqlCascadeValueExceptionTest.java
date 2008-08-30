package org.openrdf.repository.sail.rdbms;

import org.openrdf.repository.CascadeValueExceptionTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.rdbms.mysql.MySqlStore;

public class MySqlCascadeValueExceptionTest extends CascadeValueExceptionTest {

	protected Repository newRepository() {
		MySqlStore sail = new MySqlStore("sesame_test");
		sail.setUser("sesame");
		sail.setPassword("opensesame");
		return new SailRepository(sail);
	}

}
