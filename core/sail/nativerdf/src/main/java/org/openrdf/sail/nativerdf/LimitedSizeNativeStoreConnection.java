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
package org.openrdf.sail.nativerdf;

import java.io.IOException;

import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.limited.LimitedSizeEvaluationStrategy;

/**
 * @author Jerven Bolleman, SIB Swiss Institute of Bioinformatics
 */
public class LimitedSizeNativeStoreConnection extends NativeStoreConnection {

	private int maxCollectionsSize = Integer.MAX_VALUE;

	/**
	 * @param nativeStore
	 * @throws IOException
	 */
	protected LimitedSizeNativeStoreConnection(NativeStore nativeStore)
		throws IOException
	{
		super(nativeStore);
	}

	public int getMaxCollectionsSize() {
		return maxCollectionsSize;
	}

	public void setMaxCollectionsSize(int maxCollectionsSize) {
		this.maxCollectionsSize = maxCollectionsSize;
	}

	@Override
	protected EvaluationStrategy getEvaluationStrategy(Dataset dataset, NativeTripleSource tripleSource) {
		return new LimitedSizeEvaluationStrategy(tripleSource, dataset, maxCollectionsSize);
	}
}
