/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;

import org.apache.commons.httpclient.methods.RequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.io.IOUtil;


/**
 * Based on InputStreamRequestEntity
 * 
 * @see org.apache.commons.httpclient.method.InputRequestEntity
 * @author Herko ter Horst
 */
public class ReaderRequestEntity implements RequestEntity {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * The content length will be calculated automatically. This implies
	 * buffering of the content.
	 */
	public static final int CONTENT_LENGTH_AUTO = -2;

	private long contentLength;

	private Reader content;

	/** The buffered request body, if any. */
	private byte[] buffer = null;

	/** The content type */
	private String contentType;

	/**
	 * Creates a new InputStreamRequestEntity with the given content and a
	 * content type of {@link #CONTENT_LENGTH_AUTO}.
	 * 
	 * @param content
	 *        The content to set.
	 */
	public ReaderRequestEntity(Reader content) {
		this(content, null);
	}

	/**
	 * Creates a new InputStreamRequestEntity with the given content, content
	 * type, and a content length of {@link #CONTENT_LENGTH_AUTO}.
	 * 
	 * @param content
	 *        The content to set.
	 * @param contentType
	 *        The type of the content, or <code>null</code>.
	 */
	public ReaderRequestEntity(Reader content, String contentType) {
		this(content, CONTENT_LENGTH_AUTO, contentType);
	}

	/**
	 * Creates a new InputStreamRequestEntity with the given content and content
	 * length.
	 * 
	 * @param content
	 *        The content to set.
	 * @param contentLength
	 *        The content size in bytes or a negative number if not known. If
	 *        {@link #CONTENT_LENGTH_AUTO} is given the content will be buffered
	 *        in order to determine its size when {@link #getContentLength()} is
	 *        called.
	 */
	public ReaderRequestEntity(Reader content, long contentLength) {
		this(content, contentLength, null);
	}

	/**
	 * Creates a new InputStreamRequestEntity with the given content, content
	 * length, and content type.
	 * 
	 * @param content
	 *        The content to set.
	 * @param contentLength
	 *        The content size in bytes or a negative number if not known. If
	 *        {@link #CONTENT_LENGTH_AUTO} is given the content will be buffered
	 *        in order to determine its size when {@link #getContentLength()} is
	 *        called.
	 * @param contentType
	 *        The type of the content, or <code>null</code>.
	 */
	public ReaderRequestEntity(Reader content, long contentLength, String contentType) {
		if (content == null) {
			throw new IllegalArgumentException("The content cannot be null");
		}
		this.content = content;
		this.contentLength = contentLength;
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

	/**
	 * Buffers request body input stream.
	 */
	private void bufferContent() {

		if (this.buffer != null) {
			// Already been buffered
			return;
		}
		if (this.content != null) {
			try {
				ByteArrayOutputStream tmp = new ByteArrayOutputStream();
				OutputStreamWriter out = new OutputStreamWriter(tmp, "UTF-8");
				IOUtil.transfer(this.content, out);
				this.buffer = tmp.toByteArray();
				this.content = null;
				this.contentLength = buffer.length;
			}
			catch (IOException e) {
				logger.error("Unabled to buffer content", e);
				this.buffer = null;
				this.content = null;
				this.contentLength = 0;
			}
		}
	}

	/**
	 * Tests if this method is repeatable. Only <code>true</code> if the
	 * content has been buffered.
	 * 
	 * @see #getContentLength()
	 */
	public boolean isRepeatable() {
		return buffer != null;
	}

	public void writeRequest(OutputStream out)
		throws IOException
	{

		if (content != null) {
			OutputStreamWriter wrapper = new OutputStreamWriter(out, "UTF-8");
			IOUtil.transfer(this.content, wrapper);
		}
		else if (buffer != null) {
			out.write(buffer);
		}
		else {
			throw new IllegalStateException("Content must be set before entity is written");
		}
	}

	/**
	 * Gets the content length. If the content length has not been set, the
	 * content will be buffered to determine the actual content length.
	 */
	public long getContentLength() {
		if (contentLength == CONTENT_LENGTH_AUTO && buffer == null) {
			bufferContent();
		}
		return contentLength;
	}

	/**
	 * @return Returns the content.
	 */
	public Reader getContent() {
		return content;
	}
}
