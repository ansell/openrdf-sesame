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
