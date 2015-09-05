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
package org.openrdf.sail.inferencer.fc;

import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailWrapper;
import org.openrdf.sail.inferencer.InferencerConnection;

/**
 * An inferencer may infer the same statement from two different statements.
 * This leads to that single inferred statement undergoing inferencing
 * multiple times, and all the statements inferred from it too, etc.
 * This is mainly due to the use of SailConnectionListeners which don't
 * distinguish between adding a new statement and one that already exists.
 * Adding this inferencer to a Sail stack prevents this problem
 * and gives a significant performance increase.
 */
public class DedupingInferencer extends NotifyingSailWrapper {

	public DedupingInferencer()
	{}

	public DedupingInferencer(NotifyingSail baseSail) {
		super(baseSail);
	}

	@Override
	public InferencerConnection getConnection()
		throws SailException
	{
		try {
			InferencerConnection con = (InferencerConnection)super.getConnection();
			return new DedupingInferencerConnection(con, getValueFactory());
		}
		catch (ClassCastException e) {
			throw new SailException(e.getMessage(), e);
		}
	}
}
