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
