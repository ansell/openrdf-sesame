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
package org.openrdf.query.resultio;

import java.nio.charset.Charset;
import java.util.Collection;

import info.aduna.lang.FileFormat;

/**
 * The base class of all file formats that represent the results of queries.
 * Currently this includes tuple and boolean queries.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * @since 2.7.0
 */
public class QueryResultFormat extends FileFormat {

	/**
	 * @param name
	 * @param mimeType
	 * @param charset
	 * @param fileExtension
	 */
	public QueryResultFormat(String name, String mimeType, Charset charset, String fileExtension) {
		super(name, mimeType, charset, fileExtension);
	}

	/**
	 * @param name
	 * @param mimeType
	 * @param charset
	 * @param fileExtensions
	 */
	public QueryResultFormat(String name, String mimeType, Charset charset, Collection<String> fileExtensions)
	{
		super(name, mimeType, charset, fileExtensions);
	}

	/**
	 * @param name
	 * @param mimeTypes
	 * @param charset
	 * @param fileExtensions
	 */
	public QueryResultFormat(String name, Collection<String> mimeTypes, Charset charset,
			Collection<String> fileExtensions)
	{
		super(name, mimeTypes, charset, fileExtensions);
	}

}