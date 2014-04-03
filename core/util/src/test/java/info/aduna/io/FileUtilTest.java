package info.aduna.io;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;


public class FileUtilTest {
	@Test
	public void getRelativePathWorksWithRootDirectory() {
		assertEquals(new File(""),
				FileUtil.getRelativeFile(new File("/"), new File("/")));
	}
}
