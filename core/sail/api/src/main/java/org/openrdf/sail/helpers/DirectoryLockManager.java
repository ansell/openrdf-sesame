/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
import java.security.AccessControlException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.concurrent.locks.Lock;

import org.openrdf.sail.LockManager;
import org.openrdf.sail.SailLockedException;

/**
 * Used to create a lock in a directory.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public class DirectoryLockManager implements LockManager {

	private static final String LOCK_DIR_NAME = "lock";

	private static final String LOCK_FILE_NAME = "locked";

	private static final String INFO_FILE_NAME = "process";

	private final Logger logger = LoggerFactory.getLogger(DirectoryLockManager.class);

	private final File dir;

	public DirectoryLockManager(File dir) {
		this.dir = dir;
	}

	@Override
	public String getLocation() {
		return dir.toString();
	}

	private File getLockDir() {
		return new File(dir, LOCK_DIR_NAME);
	}

	/**
	 * Determines if the directory is locked.
	 * 
	 * @return <code>true</code> if the directory is already locked.
	 */
	@Override
	public boolean isLocked() {
		return getLockDir().exists();
	}

	/**
	 * Creates a lock in a directory if it does not yet exist.
	 * 
	 * @return a newly acquired lock or null if the directory is already locked.
	 */
	@Override
	public Lock tryLock() {
		File lockDir = getLockDir();

		if (lockDir.exists()) {
			removeInvalidLock(lockDir);
		}

		if (!lockDir.mkdir()) {
			return null;
		}

		Lock lock = null;

		try {
			File infoFile = new File(lockDir, INFO_FILE_NAME);
			File lockedFile = new File(lockDir, LOCK_FILE_NAME);

			RandomAccessFile raf = new RandomAccessFile(lockedFile, "rw");
			try {
				FileLock fileLock = raf.getChannel().lock();
				lock = createLock(raf, fileLock);
				sign(infoFile);
			}
			catch (IOException e) {
				if (lock != null) {
					// Also closes raf
					lock.release();
				}
				else {
					raf.close();
				}
				throw e;
			}
		}
		catch (IOException e) {
			logger.error(e.toString(), e);
		}

		return lock;
	}

	/**
	 * Creates a lock in a directory if it does not yet exist.
	 * 
	 * @return a newly acquired lock.
	 * @throws SailLockedException
	 *         if the directory is already locked.
	 */
	@Override
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
	@Override
	public boolean revokeLock() {
		File lockDir = getLockDir();
		File lockedFile = new File(lockDir, LOCK_FILE_NAME);
		File infoFile = new File(lockDir, INFO_FILE_NAME);
		lockedFile.delete();
		infoFile.delete();
		return lockDir.delete();
	}

	private void removeInvalidLock(File lockDir) {
		try {
			boolean revokeLock = false;

			File lockedFile = new File(lockDir, LOCK_FILE_NAME);
			RandomAccessFile raf = new RandomAccessFile(lockedFile, "rw");
			try {
				FileLock fileLock = raf.getChannel().tryLock();

				if (fileLock != null) {
					logger.warn("Removing invalid lock {}", getLockedBy());
					fileLock.release();
					revokeLock = true;
				}
			}
			catch (OverlappingFileLockException exc) {
				// lock is still valid
			}
			finally {
				raf.close();
			}

			if (revokeLock) {
				revokeLock();
			}
		}
		catch (IOException e) {
			logger.warn(e.toString(), e);
		}
	}

	private String getLockedBy() {
		try {
			File lockDir = getLockDir();
			File infoFile = new File(lockDir, INFO_FILE_NAME);
			BufferedReader reader = new BufferedReader(new FileReader(infoFile));
			try {
				return reader.readLine();
			}
			finally {
				reader.close();
			}
		}
		catch (IOException e) {
			logger.warn(e.toString(), e);
			return null;
		}
	}

	private Lock createLock(final RandomAccessFile raf, final FileLock fileLock) {
		return new Lock() {

			private Thread hook;
			{
				try {
					Thread hook = new Thread(new Runnable() {

						public void run() {
							delete();
						}
					});
					Runtime.getRuntime().addShutdownHook(hook);
					this.hook = hook;
				}
				catch (AccessControlException e) {
					// okay, just remember to close it yourself
				}
			}

			public boolean isActive() {
				return fileLock.isValid() || hook != null;
			}

			public void release() {
				try {
					if (hook != null) {
						Runtime.getRuntime().removeShutdownHook(hook);
						hook = null;
					}
				}
				catch (IllegalStateException e) {
					// already shutting down
				}
				catch (AccessControlException e) {
					logger.warn(e.toString(), e);
				}
				delete();
			}

			synchronized void delete() {
				try {
					if (raf.getChannel().isOpen()) {
						fileLock.release();
						raf.close();
					}
				}
				catch (IOException e) {
					logger.warn(e.toString(), e);
				}

				revokeLock();
			}
		};
	}

	private void sign(File infoFile)
		throws IOException
	{
		FileWriter out = new FileWriter(infoFile);
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
