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
	private static final String CREATE_LOCKED = "CREATE TABLE LOCKED ( process VARCHAR(128) )";
	private static final String INSERT = "INSERT INTO LOCKED VALUES ('";
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

	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	public Lock tryLock() {
		Lock lock = null;
		try {
			Statement st = null;
			Connection con = getConnection();
			try {
				st = con.createStatement();
				st.execute(CREATE_LOCKED);
				lock = createLock();
				st.execute(INSERT + getProcessName() + "')");
				return lock;
			} finally {
				if (st != null) {
					st.close();
				}
				con.close();
			}
		} catch (SQLException exc) {
			logger.warn(exc.toString(), exc);
			if (lock != null) {
				lock.release();
			}
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
