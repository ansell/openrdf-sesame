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
package org.openrdf.sail.lucene4;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import org.openrdf.sail.lucene.LuceneSail;

/**
 * LuceneIndex which uses a NIOFSDirectory instead of MMapDirectory to avoid the
 * JVM crash (see <a href=
 * "http://stackoverflow.com/questions/8224843/jvm-crashes-on-lucene-datainput-readvint"
 * >http://stackoverflow.com/questions/8224843/jvm-crashes-on-lucene-datainput-
 * readvint</a>).
 * 
 * @author andriy.nikolov
 */
public class LuceneIndexNIOFS extends LuceneIndex {

	@Override
	protected Directory createDirectory(Properties parameters)
		throws IOException
	{
		if (parameters.containsKey(LuceneSail.LUCENE_DIR_KEY)) {
			return new NIOFSDirectory(new File(parameters.getProperty(LuceneSail.LUCENE_DIR_KEY)));
		}
		else
			return super.createDirectory(parameters);
	}
}