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


/**
 * Makes working with a queue easier by adding the methods {@link #done()} and
 * {@link #toss(Exception)} and automatically converting the exception into a
 * QueryEvaluationException with an appropriate stack trace.
 * 
 * @author James Leigh
 * @deprecated use {@link org.openrdf.http.client.QueueCursor} instead
 * @see org.openrdf.http.client.QueueCursor
 */
public class QueueCursor<E> extends org.openrdf.http.client.QueueCursor<E> {

	public QueueCursor(int capacity, boolean fair) {
		super(capacity, fair);
	}

	public QueueCursor(int capacity) {
		super(capacity);
	}	
}
