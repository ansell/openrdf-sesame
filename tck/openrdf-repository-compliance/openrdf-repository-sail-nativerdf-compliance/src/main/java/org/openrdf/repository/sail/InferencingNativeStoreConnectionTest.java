package org.openrdf.repository.sail;

import java.io.File;

import info.aduna.io.FileUtil;

import org.openrdf.repository.InferencingRepositoryConnectionTest;
import org.openrdf.repository.Repository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.nativerdf.NativeStore;

public class InferencingNativeStoreConnectionTest extends InferencingRepositoryConnectionTest {

	private File dataDir;

	public InferencingNativeStoreConnectionTest(String name) {
		super(name);
	}

	@Override
	protected Repository createRepository()
		throws Exception
	{
		dataDir = FileUtil.createTempDir("nativestore");
		return new SailRepository(new ForwardChainingRDFSInferencer(new NativeStore(dataDir, "spoc")));
	}
}
