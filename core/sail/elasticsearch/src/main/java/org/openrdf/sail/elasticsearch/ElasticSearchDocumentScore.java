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
package org.openrdf.sail.elasticsearch;

import org.openrdf.sail.lucene.DocumentScore;
import org.openrdf.sail.lucene.SearchDocument;

public class ElasticSearchDocumentScore implements DocumentScore {

	@Override
	public SearchDocument getDocument() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getScore() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isHighlighted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterable<String> getSnippets(String field) {
		// TODO Auto-generated method stub
		return null;
	}

}
