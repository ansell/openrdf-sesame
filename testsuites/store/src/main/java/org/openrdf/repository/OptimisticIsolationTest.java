/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
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
