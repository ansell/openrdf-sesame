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

package org.eclipse.rdf4j.common.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Zip-related utilities.
 */
public class ZipUtil {

	/**
	 * Magic number for ZIP files (4 bytes: <tt>0x04034b50</tt>).
	 */
	private final static byte MAGIC_NUMBER[] = { (byte)0x50, (byte)0x4B, (byte)0x03, (byte)0x04 };

	public static boolean isZipStream(InputStream in)
		throws IOException
	{
		in.mark(MAGIC_NUMBER.length);
		byte[] fileHeader = IOUtil.readBytes(in, MAGIC_NUMBER.length);
		in.reset();
		return Arrays.equals(MAGIC_NUMBER, fileHeader);
	}

	/**
	 * Extract the contents of a zipfile to a directory.
	 * 
	 * @param zipFile
	 *        the zip file to extract
	 * @param destDir
	 *        the destination directory
	 * @throws IOException
	 *         when something untowards happens during the extraction process
	 */
	public static void extract(File zipFile, File destDir)
		throws IOException
	{
		ZipFile zf = new ZipFile(zipFile);
		try {
			extract(zf, destDir);
		}
		finally {
			zf.close();
		}
	}

	/**
	 * Extract the contents of a zipfile to a directory.
	 * 
	 * @param zipFile
	 *        the zip file to extract
	 * @param destDir
	 *        the destination directory
	 * @throws IOException
	 *         when something untowards happens during the extraction process
	 */
	public static void extract(ZipFile zipFile, File destDir)
		throws IOException
	{
		assert destDir.isDirectory();

		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			writeEntry(zipFile, entry, destDir);
		}
	}

	/**
	 * Write an entry to a zip file.
	 * 
	 * @param zipFile
	 *        the zip file to read from
	 * @param entry
	 *        the entry to process
	 * @param destDir
	 *        the file to write to
	 * @throws IOException
	 *         if the entry could not be processed
	 */
	public static void writeEntry(ZipFile zipFile, ZipEntry entry, File destDir)
		throws IOException
	{
		File outFile = new File(destDir, entry.getName());

		if (entry.isDirectory()) {
			outFile.mkdirs();
		}
		else {
			outFile.getParentFile().mkdirs();

			InputStream in = zipFile.getInputStream(entry);
			try {
				IOUtil.writeStream(in, outFile);
			}
			finally {
				in.close();
			}
		}
	}
}
