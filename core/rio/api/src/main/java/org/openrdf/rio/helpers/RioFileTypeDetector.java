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
package org.openrdf.rio.helpers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;

/**
 * Implementation of {@link FileTypeDetector} using
 * {@link Rio#getParserFormatForFileName(String)} and
 * {@link Rio#getWriterFormatForFileName(String)} if a parser is not found.
 * 
 * @author Peter Ansell
 */
public class RioFileTypeDetector extends FileTypeDetector {

	@Override
	public String probeContentType(Path path)
		throws IOException
	{
		RDFFormat format = Rio.getParserFormatForFileName(path.getFileName().toString());

		if (format == null) {
			format = Rio.getWriterFormatForFileName(path.getFileName().toString());
		}

		if (format == null) {
			return null;
		}

		return format.getDefaultMIMEType();
	}

}
