/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public final class IOUtil {

	/**
	 * Transfers all bytes that can be read from <tt>in</tt> to <tt>out</tt>.
	 *
	 * @param in The InputStream to read data from.
	 * @param out The OutputStream to write data to.
	 * @return The total number of bytes transfered.
	 */
	public static final long transfer(InputStream in, OutputStream out)
		throws IOException
	{
		long totalBytes = 0;
		int bytesInBuf = 0;
		byte[] buf = new byte[4096];

		while ((bytesInBuf = in.read(buf)) != -1) {
			out.write(buf, 0, bytesInBuf);
			totalBytes += bytesInBuf;
		}

		return totalBytes;
	}

	/**
	 * Fully reads the bytes available from the supplied InputStream and returns
	 * these bytes in a byte array.
	 *
	 * @param in The InputStream to read the bytes from.
	 * @return A byte array containing the bytes that were read.
	 * @throws IOException If I/O error occurred.
	 */
	public static final byte[] readFully(InputStream in)
		throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		transfer(in, out);
		out.close();

		return out.toByteArray();
	}

	/**
	 * Reads at most <tt>maxBytes</tt> bytes from the supplied input stream and
	 * returns them as a byte array.
	 *
	 * @param in The InputStream supplying the bytes.
	 * @param maxBytes The maximum number of bytes to read from the input
	 * stream.
	 * @return A byte array of size <tt>maxBytes</tt> if the input stream can
	 * produce that amount of bytes, or a smaller byte containing all available
	 * bytes from the stream otherwise.
	 */
	public static final byte[] readBytes(InputStream in, int maxBytes)
		throws IOException
	{
		byte[] result = new byte[maxBytes];

		int bytesRead = in.read(result);
		int totalBytesRead = bytesRead;

		while (totalBytesRead < maxBytes && bytesRead >= 0) {
			// Read more bytes
			bytesRead = in.read(result, bytesRead, maxBytes - bytesRead);

			if (bytesRead > 0) {
				totalBytesRead += bytesRead;
			}
		}

		if (totalBytesRead < 0) {
			// InputStream at end-of-file
			result = new byte[0];
		}
		else if (totalBytesRead < maxBytes) {
			// Create smaller byte array
			byte[] tmp = new byte[totalBytesRead];
			System.arraycopy(result, 0, tmp, 0, totalBytesRead);
			result = tmp;
		}

		return result;
	}

	/**
	 * Writes all bytes from an <tt>InputStream</tt> to a file.
	 *
	 * @param in The <tt>InputStream</tt> containing the data to write to the
	 * file.
	 * @param file The file to write the data to.
	 * @return The total number of bytes written.
	 * @throws IOException If an I/O error occured while trying to write the
	 * data to the file.
	 */
	public static final long writeToFile(InputStream in, File file)
		throws IOException
	{
		FileOutputStream out = new FileOutputStream(file);

		try {
			return transfer(in, out);
		}
		finally {
			out.close();
		}
	}

	/**
	 * Transfers all characters that can be read from <tt>in</tt> to
	 * <tt>out</tt>.
	 *
	 * @param in The Reader to read characters from.
	 * @param out The Writer to write characters to.
	 * @return The total number of characters transfered.
	 */
	public static final long transfer(Reader in, Writer out)
		throws IOException
	{
		long totalChars = 0;
		int charsInBuf = 0;
		char[] buf = new char[4096];

		while ((charsInBuf = in.read(buf)) != -1) {
			out.write(buf, 0, charsInBuf);
			totalChars += charsInBuf;
		}

		return totalChars;
	}

	/**
	 * Fully reads the characters available from the supplied Reader
	 * and returns these characters as a String object.
	 *
	 * @param reader The Reader to read the characters from.
	 * @return A String existing of the characters that were read.
	 * @throws IOException If I/O error occurred.
	 */
	public static final String readFully(Reader reader)
		throws IOException
	{
		CharArrayWriter out = new CharArrayWriter(4096);
		transfer(reader, out);
		out.close();

		return out.toString();
	}

	/**
	 * Writes all characters from a <tt>Reader</tt> to a file using the default
	 * character encoding.
	 *
	 * @param reader The <tt>Reader</tt> containing the data to write to the
	 * file.
	 * @param file The file to write the data to.
	 * @return The total number of characters written.
	 * @throws IOException If an I/O error occured while trying to write the
	 * data to the file.
	 * @see java.io.FileWriter
	 */
	public static final long writeToFile(Reader reader, File file)
		throws IOException
	{
		FileWriter writer = new FileWriter(file);

		try {
			return transfer(reader, writer);
		}
		finally {
			writer.close();
		}
	}
	
	/**
	 * Creates a new and empty directory in the default temp directory using the
	 * given prefix. This methods uses {@link File#createTempFile} to create a
	 * new tmp file, deletes it and creates a directory for it instead.
	 * 
	 * @param prefix The prefix string to be used in generating the diretory's
	 * name; must be at least three characters long.
	 * @return A newly-created empty directory.
	 * @throws IOException If no directory could be created.
	 */
	public static File createTempDir(String prefix)
		throws IOException
	{
		String tmpDirStr = System.getProperty("java.io.tmpdir");
		if (tmpDirStr == null) {
			throw new IOException(
				"System property 'java.io.tmpdir' does not specify a tmp dir");
		}
		
		File tmpDir = new File(tmpDirStr);
		if (!tmpDir.exists()) {
			boolean created = tmpDir.mkdirs();
			if (!created) {
				throw new IOException("Unable to create tmp dir " + tmpDir);
			}
		}
		
		File resultDir = null;
		int suffix = (int)System.currentTimeMillis();
		int failureCount = 0;
		do {
			resultDir = new File(tmpDir, prefix + suffix % 10000);
			suffix++;
			failureCount++;
		}
		while (resultDir.exists() && failureCount < 50);
		
		if (resultDir.exists()) {
			throw new IOException(failureCount + 
				" attempts to generate a non-existent directory name failed, giving up");
		}
		boolean created = resultDir.mkdir();
		if (!created) {
			throw new IOException("Failed to create tmp directory");
		}
		
		return resultDir;
	}
	
	/**
	 * Deletes the specified diretory and any files and directories in it
	 * recursively.
	 * 
	 * @param dir The directory to remove.
	 * @throws IOException If the directory could not be removed.
	 */
	public static void deleteDir(File dir)
		throws IOException
	{
		if (!dir.isDirectory()) {
			throw new IOException("Not a directory " + dir);
		}
		
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			
			if (file.isDirectory()) {
				deleteDir(file);
			}
			else {
				boolean deleted = file.delete();
				if (!deleted) {
					throw new IOException("Unable to delete file" + file);
				}
			}
		}
		
		dir.delete();
	}
}
