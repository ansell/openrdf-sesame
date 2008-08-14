/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.concurrent.locks.Lock;

import org.openrdf.sail.LockManager;
import org.openrdf.sail.SailLockedException;

/**
 * Used to create a lock in a directory.
 * 
 * @author James Leigh
 */
public class DirectoryLockManager implements LockManager {
	private Logger logger = LoggerFactory.getLogger(DirectoryLockManager.class);
	private File dir;

	public DirectoryLockManager(File dir) {
		this.dir = dir;
	}

	/**
	 * Determines if the directory is locked.
	 * 
	 * @return <code>true</code> if the directory is already locked.
	 */
	public boolean isLocked() {
		return new File(dir, "lock").exists();
	}

	/**
	 * Creates a lock in a directory if it does not yet exist.
	 * 
	 * @return a newly acquired lock or null if the directory is already locked.
	 */
	public Lock tryLock() {
		File lockDir = new File(dir, "lock");
		if (lockDir.exists() || !lockDir.mkdir())
			return null;
		File lockFile = new File(lockDir, "process");
		Lock lock = createLock(lockDir, lockFile);
		try {
			sign(lockFile);
		} catch (IOException exc) {
			lock.release();
			logger.error(exc.toString(), exc);
			return null;
		}
		return lock;
	}

	/**
	 * Creates a lock in a directory if it does not yet exist.
	 * 
	 * @return a newly acquired lock.
	 * @throws SailLockedException
	 *             if the directory is already locked.
	 */
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
		File lockDir = new File(dir, "lock");
		File lockFile = new File(lockDir, "process");
		return lockFile.delete() && lockDir.delete();
	}

	private String getLockedBy() {
		try {
			File lockDir = new File(dir, "lock");
			File lockFile = new File(lockDir, "process");
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(lockFile));
				return reader.readLine();
			} finally {
				reader.close();
			}
		} catch (IOException exc) {
			logger.warn(exc.toString(), exc);
			return null;
		}
	}

	private Lock createLock(final File lockDir, final File lockFile) {
		return new Lock() {
			private boolean active = true;
			private Thread hook = new Thread(new Runnable() {
				public void run() {
					active = false;
					lockFile.delete();
					lockDir.delete();
				}
			});
			{
				Runtime.getRuntime().addShutdownHook(hook);
			}

			public boolean isActive() {
				return active;
			}

			public void release() {
				active = false;
				Runtime.getRuntime().removeShutdownHook(hook);
				lockFile.delete();
				lockDir.delete();
			}
		};
	}

	private void sign(File lockFile) throws IOException {
		FileWriter out = new FileWriter(lockFile);
		out.write(getProcessName());
		out.flush();
		out.close();
	}

	private String getProcessName() {
		return ManagementFactory.getRuntimeMXBean().getName();
	}
}
