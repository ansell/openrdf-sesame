package org.openrdf.repository.sail.nativerdf;

import java.io.File;
import java.io.IOException;

import info.aduna.io.FileUtil;

import org.openrdf.repository.CascadeValueExceptionTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

public class NativeCascadeValueExceptionTest extends CascadeValueExceptionTest {

	private File dataDir;

	@Override
	protected Repository newRepository() throws IOException {
		dataDir = FileUtil.createTempDir("nativestore");
		return new SailRepository(new NativeStore(dataDir, "spoc"));
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			super.tearDown();
		} finally {
			FileUtil.deleteDir(dataDir);
		}
	}

}
