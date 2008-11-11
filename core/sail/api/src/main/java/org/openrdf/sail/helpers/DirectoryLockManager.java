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
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

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

	Logger logger = LoggerFactory.getLogger(DirectoryLockManager.class);

	private File dir;

	public DirectoryLockManager(File dir) {
		this.dir = dir;
	}

	public String getLocation() {
		return dir.toString();
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
		if (lockDir.exists()) {
			removeInValidLock(lockDir);
		}
		if (!lockDir.mkdir()) {
			return null;
		}

		try {
			File infoFile = new File(lockDir, "process");
			File lockedFile = new File(lockDir, "locked");
			RandomAccessFile raf = new RandomAccessFile(lockedFile, "rw");
			FileLock locked = raf.getChannel().lock();
			Lock lock = createLock(lockDir, infoFile, lockedFile, raf, locked);
			try {
				sign(infoFile);
				return lock;
			}
			catch (IOException exc) {
				lock.release();
				throw exc;
			}
		} catch (IOException exc) {
			logger.error(exc.toString(), exc);
			return null;
		}
	}

	/**
	 * Creates a lock in a directory if it does not yet exist.
	 * 
	 * @return a newly acquired lock.
	 * @throws SailLockedException
	 *         if the directory is already locked.
	 */
	public Lock lockOrFail()
		throws SailLockedException
	{
		Lock lock = tryLock();
		if (lock != null) {
			return lock;
		}

		String requestedBy = getProcessName();
		String lockedBy = getLockedBy();
		if (lockedBy != null) {
			throw new SailLockedException(lockedBy, requestedBy, this);
		}

		lock = tryLock();
		if (lock != null) {
			return lock;
		}

		throw new SailLockedException(requestedBy);
	}

	/**
	 * Revokes a lock owned by another process.
	 * 
	 * @return <code>true</code> if a lock was successfully revoked.
	 */
	public boolean revokeLock() {
		File lockDir = new File(dir, "lock");
		File lockedFile = new File(lockDir, "locked");
		File infoFile = new File(lockDir, "process");
		lockedFile.delete();
		infoFile.delete();
		return lockDir.delete();
	}

	private void removeInValidLock(File lockDir) {
		try {
			File lockedFile = new File(lockDir, "locked");
			RandomAccessFile raf = new RandomAccessFile(lockedFile, "rw");
			try {
				FileLock locked = raf.getChannel().tryLock();
				if (locked != null) {
					logger.warn("Removing invalid lock {}", getLockedBy());
					locked.release();
					raf.close();
					revokeLock();
				} else {
					raf.close();
				}
			} catch (OverlappingFileLockException exc) {
				raf.close();
			}
		} catch (IOException exc) {
			logger.warn(exc.toString(), exc);
		}
	}

	private String getLockedBy() {
		try {
			File lockDir = new File(dir, "lock");
			File lockFile = new File(lockDir, "process");
			BufferedReader reader = new BufferedReader(new FileReader(lockFile));
			try {
				return reader.readLine();
			}
			finally {
				reader.close();
			}
		}
		catch (IOException exc) {
			logger.warn(exc.toString(), exc);
			return null;
		}
	}

	private Lock createLock(final File lockDir, final File infoFile, final File lockedFile,
			final RandomAccessFile raf, final FileLock locked)
	{
		return new Lock() {

			private boolean active = true;

			private Thread hook = new Thread(new Runnable() {

				public void run() {
					delete();
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
				delete();
			}

			void delete() {
				try {
					locked.release();
					raf.close();
				}
				catch (IOException e) {
					logger.warn(e.toString(), e);
				}
				lockedFile.delete();
				infoFile.delete();
				lockDir.delete();
			}
		};
	}

	private void sign(File lockFile)
		throws IOException
	{
		FileWriter out = new FileWriter(lockFile);
		try {
			out.write(getProcessName());
			out.flush();
		}
		finally {
			out.close();
		}
	}

	private String getProcessName() {
		return ManagementFactory.getRuntimeMXBean().getName();
	}
}
