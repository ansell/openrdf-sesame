/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.helpers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * FilePart represents a file uploaded through a multipart/form-data encoded
 * POST request.
 */
public class FilePart implements DataSource {

	private String name;

	private String contentType;

	private byte[] bytes = null;

	/**
	 * Construct a new filepart.
	 */
	public FilePart(String name, String contentType, byte[] contents) {
		this.name = name;
		this.contentType = contentType;
		this.bytes = contents;
	}

	/**
	 * @return an InputStream on the contents of the file. Note: a new stream is
	 *         returned each time.
	 */
	public InputStream getInputStream() throws IOException {
		InputStream result = null;
		if (bytes == null) {
			throw new IOException("no data");
		}
		else {
			result = new ByteArrayInputStream(bytes);
		}
		return result;
	}

	public int size() {
		return bytes.length;
	}

	/**
	 * @return the content type of the uploaded file as determined from the
	 *         request, or the filename extension.
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @return the name of the uploaded file as specified in the request.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the contents of the uploaded file as specified in the request.
	 */
	public byte[] getBytes() {
		return bytes;
	}

	public OutputStream getOutputStream() throws IOException {
		throw new IOException("FilePart does not support output.");
	}
}
