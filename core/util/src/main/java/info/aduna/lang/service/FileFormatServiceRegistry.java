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
package info.aduna.lang.service;

import java.util.Optional;

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
	 * @return The matching {@link FileFormat}, or {@link Optional#empty()} if no
	 *         match was found.
	 * @see #getFileFormatForMIMEType(String, FileFormat)
	 */
	public Optional<FF> getFileFormatForMIMEType(String mimeType) {
		return FileFormat.matchMIMEType(mimeType, this.getKeys());
	}

	/**
	 * Tries to match the extension of a file name against the list of registred
	 * file formats.
	 * 
	 * @param fileName
	 *        A file name.
	 * @return The matching {@link FileFormat}, or {@link Optional#empty()} if no
	 *         match was found.
	 * @see #getFileFormatForFileName(String, FileFormat)
	 */
	public Optional<FF> getFileFormatForFileName(String fileName) {
		return FileFormat.matchFileName(fileName, this.getKeys());
	}
}
