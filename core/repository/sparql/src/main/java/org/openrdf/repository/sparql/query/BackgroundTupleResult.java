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
package org.openrdf.repository.sparql.query;

import java.io.InputStream;

import org.apache.commons.httpclient.HttpMethod;

import org.openrdf.http.client.QueueCursor;
import org.openrdf.query.BindingSet;
import org.openrdf.query.resultio.TupleQueryResultParser;

/**
 * Provides concurrent access to tuple results as they are being parsed.
 * 
 * @author James Leigh
 * @deprecated use {@link org.openrdf.http.client.BackgroundTupleResult} instead
 * @see org.openrdf.http.client.BackgroundTupleResult
 */
public class BackgroundTupleResult extends org.openrdf.http.client.BackgroundTupleResult {


	public BackgroundTupleResult(QueueCursor<BindingSet> queue, TupleQueryResultParser parser, InputStream in,
			HttpMethod connection)
	{
		super(queue, parser, in, connection);
	}

	public BackgroundTupleResult(TupleQueryResultParser parser, InputStream in, HttpMethod connection) {
		super(parser, in, connection);
	}	
}
