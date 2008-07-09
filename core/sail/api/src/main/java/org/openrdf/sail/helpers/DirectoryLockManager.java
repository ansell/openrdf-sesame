/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.concurrent.locks.Lock;

/**
 * Used to create a lock in a directory.
 * 
 * @author James Leigh
 */
public class DirectoryLockManager {
	private Logger logger = LoggerFactory.getLogger(DirectoryLockManager.class);
	private File dir;

	public DirectoryLockManager(File dir) {
		this.dir = dir;
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

	private Lock createLock(final File lockDir, final File lockFile) {
		return new Lock() {
			private boolean active = true;

			public boolean isActive() {
				return active;
			}

			public void release() {
				active = false;
				lockFile.delete();
				lockDir.delete();
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
