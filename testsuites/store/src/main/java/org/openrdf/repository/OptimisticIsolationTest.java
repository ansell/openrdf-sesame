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
package org.openrdf.repository;

import java.io.File;
import java.io.IOException;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import info.aduna.io.FileUtil;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.optimistic.DeadLockTest;
import org.openrdf.repository.optimistic.DeleteInsertTest;
import org.openrdf.repository.optimistic.LinearTest;
import org.openrdf.repository.optimistic.ModificationTest;
import org.openrdf.repository.optimistic.MonotonicTest;
import org.openrdf.repository.optimistic.RemoveIsolationTest;
import org.openrdf.repository.optimistic.SailIsolationLevelTest;
import org.openrdf.repository.optimistic.SerializableTest;
import org.openrdf.repository.optimistic.SnapshotTest;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.repository.sail.config.SailRepositoryFactory;
import org.openrdf.sail.config.SailFactory;

/**
 * @author James Leigh
 */
@RunWith(Suite.class)
@SuiteClasses({
		DeadLockTest.class,
		DeleteInsertTest.class,
		LinearTest.class,
		ModificationTest.class,
		RemoveIsolationTest.class,
		SailIsolationLevelTest.class,
		MonotonicTest.class,
		SnapshotTest.class,
		SerializableTest.class })
public abstract class OptimisticIsolationTest {

	private static RepositoryFactory factory;

	private static File dataDir;

	public static void setRepositoryFactory(RepositoryFactory factory)
		throws IOException
	{
		if (dataDir != null && dataDir.isDirectory()) {
			FileUtil.deleteDir(dataDir);
			dataDir = null;
		}
		OptimisticIsolationTest.factory = factory;
	}

	public static void setSailFactory(final SailFactory factory)
		throws IOException
	{
		setRepositoryFactory(new SailRepositoryFactory() {

			@Override
			public RepositoryImplConfig getConfig() {
				return new SailRepositoryConfig(factory.getConfig());
			}
		});
	}

	public static Repository getEmptyInitializedRepository(Class<?> caller)
		throws OpenRDFException, IOException
	{
		if (dataDir != null && dataDir.isDirectory()) {
			FileUtil.deleteDir(dataDir);
			dataDir = null;
		}
		dataDir = FileUtil.createTempDir(caller.getSimpleName());
		Repository repository = factory.getRepository(factory.getConfig());
		repository.setDataDir(dataDir);
		repository.initialize();
		RepositoryConnection con = repository.getConnection();
		try {
			con.clear();
			con.clearNamespaces();
		}
		finally {
			con.close();
		}
		return repository;
	}
}
