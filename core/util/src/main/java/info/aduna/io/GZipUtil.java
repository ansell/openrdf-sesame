/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * GZip-related utilities.
 */
public class GZipUtil {

	/**
	 * GZIP header magic number bytes, like found in a gzipped files, which are
	 * encoded in Intel format (i&#x2e;e&#x2e; little indian).
	 */
	private final static byte MAGIC_NUMBER[] = { (byte)0x1f, (byte)0x8b };

	public static boolean isGZipStream(InputStream in)
		throws IOException
	{
		in.mark(MAGIC_NUMBER.length);
		byte[] fileHeader = IOUtil.readBytes(in, MAGIC_NUMBER.length);
		in.reset();
		return Arrays.equals(MAGIC_NUMBER, fileHeader);
	}
}
