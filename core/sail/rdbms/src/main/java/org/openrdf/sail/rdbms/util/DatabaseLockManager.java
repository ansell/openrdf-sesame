/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.util;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.concurrent.locks.Lock;

/**
 * 
 * @author James Leigh
 */
public class DatabaseLockManager {
	private Logger logger = LoggerFactory.getLogger(DatabaseLockManager.class);
	private DataSource ds;

	private String user;

	private String password;

	public DatabaseLockManager(DataSource ds) {
		this.ds = ds;
	}

	public DatabaseLockManager(DataSource ds, String user, String password) {
		this.ds = ds;
		this.user = user;
		this.password = password;
	}

	public Lock tryLock() {
		try {
			Connection con = getConnection();
			try {
				Statement st = con.createStatement();
				try {
					st.execute("CREATE TABLE LOCKED ( process VARCHAR(128) )");
					Lock lock = createLock();
					st.execute("INSERT INTO LOCKED VALUES ('" + getProcessName() + "')");
					return lock;
				} finally {
					st.close();
				}
			} finally {
				con.close();
			}
		} catch (SQLException exc) {
			logger.warn(exc.toString(), exc);
			return null;
		}
	}

	private Connection getConnection() throws SQLException {
		if (user == null)
			return ds.getConnection();
		return ds.getConnection(user, password);
	}

	private String getProcessName() {
		return ManagementFactory.getRuntimeMXBean().getName();
	}

	private Lock createLock() {
		return new Lock() {
			private boolean active = true;

			public boolean isActive() {
				return active;
			}

			public void release() {
				active = false;
				try {
					Connection con = getConnection();
					try {
						Statement st = con.createStatement();
						try {
							st.execute("DROP TABLE LOCKED");
						} finally {
							st.close();
						}
					} finally {
						con.close();
					}
				} catch (SQLException exc) {
					logger.error(exc.toString(), exc);
				}
			}

			@Override
			protected void finalize() throws Throwable {
				if (active) {
					release();
				}
				super.finalize();
			}
		};
	}
}
