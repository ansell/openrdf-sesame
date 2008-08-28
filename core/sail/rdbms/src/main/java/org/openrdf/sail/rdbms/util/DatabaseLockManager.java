/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.util;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.concurrent.locks.Lock;

import org.openrdf.sail.LockManager;
import org.openrdf.sail.SailLockedException;

/**
 * 
 * @author James Leigh
 */
public class DatabaseLockManager implements LockManager {
	private static final String CREATE_LOCKED = "CREATE TABLE locked ( process VARCHAR(128) )";
	private static final String INSERT = "INSERT INTO locked VALUES ('";
	private static final String SELECT = "SELECT process FROM locked";
	private static final String DROP = "DROP TABLE locked";
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

	public boolean isLocked() {
		try {
			ResultSet rs = null;
			Statement st = null;
			Connection con = getConnection();
			try {
				st = con.createStatement();
				rs = st.executeQuery(SELECT);
				return rs.next();
			} finally {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				con.close();
			}
		} catch (SQLException exc) {
			logger.warn(exc.toString(), exc);
			return false;
		}
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

	public Lock lockOrFail() throws SailLockedException {
		Lock lock = tryLock();
		if (lock != null)
			return lock;
		String requestedBy = getProcessName();
		String lockedBy = getLockedBy();
		if (lockedBy != null)
			throw new SailLockedException(lockedBy, requestedBy, this);
		lock = tryLock();
		if (lock != null)
			return lock;
		throw new SailLockedException(requestedBy);
	}

	/**
	 * Revokes a lock owned by another process.
	 * 
	 * @return <code>true</code> if a lock was successfully revoked.
	 */
	public boolean revokeLock() {
		try {
			Statement st = null;
			Connection con = getConnection();
			try {
				st = con.createStatement();
				st.execute(DROP);
				return true;
			} finally {
				if (st != null) {
					st.close();
				}
				con.close();
			}
		} catch (SQLException exc) {
			logger.warn(exc.toString(), exc);
			return false;
		}
	}

	private String getLockedBy() {
		try {
			ResultSet rs = null;
			Statement st = null;
			Connection con = getConnection();
			try {
				st = con.createStatement();
				rs = st.executeQuery(SELECT);
				if (!rs.next())
					return null;
				return rs.getString(1);
			} finally {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
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
							st.execute(DROP);
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
