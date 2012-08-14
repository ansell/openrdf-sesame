/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.lang.service;

import info.aduna.lang.FileFormat;

/**
 * A special {@link ServiceRegistry} for {@link FileFormat} related services.
 * This FileFormat-specific subclass offers some utility methods for matching
 * MIME types and file extensions to the file formats of registered services.
 * 
 * @author Arjohn Kampman
 */
public abstract class FileFormatServiceRegistry<FF extends FileFormat, S> extends ServiceRegistry<FF, S> {

	protected FileFormatServiceRegistry(Class<S> serviceClass) {
		super(serviceClass);
	}

	/**
	 * Tries to match a MIME type against the list of registered file formats.
	 * 
	 * @param mimeType
	 *        A MIME type, e.g. "text/plain".
	 * @return The matching {@link FileFormat}, or <tt>null</tt> if no match
	 *         was found.
	 * @see #getFileFormatForMIMEType(String, FileFormat)
	 */
	public FF getFileFormatForMIMEType(String mimeType) {
		return getFileFormatForMIMEType(mimeType, null);
	}

	/**
	 * Tries to match a MIME type against the list of registred file formats.
	 * This method calls
	 * {@link FileFormat#matchMIMEType(String, Iterable, FileFormat)} with the
	 * specified MIME type, the {@link #getKeys() keys} of this registry and the
	 * fallback format as parameters.
	 * 
	 * @param mimeType
	 *        A MIME type, e.g. "text/plain".
	 * @param fallback
	 *        The format that will be returned if no match was found.
	 * @return The matching {@link FileFormat}, or <tt>fallback</tt> if no
	 *         match was found.
	 */
	public FF getFileFormatForMIMEType(String mimeType, FF fallback) {
		return FileFormat.matchMIMEType(mimeType, this.getKeys(), fallback);
	}

	/**
	 * Tries to match the extension of a file name against the list of registred
	 * file formats.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return The matching {@link FileFormat}, or <tt>null</tt> if no match
	 *         was found.
	 * @see #getFileFormatForFileName(String, FileFormat)
	 */
	public FF getFileFormatForFileName(String fileName) {
		return getFileFormatForFileName(fileName, null);
	}

	/**
	 * Tries to match the extension of a file name against the list of registred
	 * file formats. This method calls
	 * {@link FileFormat#matchFileName(String, Iterable, FileFormat)} with the
	 * specified MIME type, the {@link #getKeys() keys} of this registry and the
	 * fallback format as parameters.
	 * 
	 * @param fileName
	 *        A file name.
	 * @param fallback
	 *        The format that will be returned if no match was found.
	 * @return The matching {@link FileFormat}, or <tt>fallback</tt> if no
	 *         match was found.
	 */
	public FF getFileFormatForFileName(String fileName, FF fallback) {
		return FileFormat.matchFileName(fileName, this.getKeys(), fallback);
	}
}
