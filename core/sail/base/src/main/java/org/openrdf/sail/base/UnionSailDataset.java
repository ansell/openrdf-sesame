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
package org.openrdf.sail.base;

import java.util.Arrays;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailException;

/**
 * Combines multiple {@link SailDataset} into a single view.
 * 
 * @author James Leigh
 */
class UnionSailDataset implements SailDataset {

	/**
	 * Set of {@link SailDataset}s to combine.
	 */
	private final SailDataset[] datasets;

	/**
	 * Creates a new {@link SailDataset} that includes all the given
	 * {@link SailDataset}s.
	 * 
	 * @param datasets
	 */
	public UnionSailDataset(SailDataset... datasets) {
		this.datasets = datasets;
	}

	public String toString() {
		return Arrays.asList(datasets).toString();
	}

	@Override
	public void close()
		throws SailException
	{
		for (SailDataset dataset : datasets) {
			dataset.close();
		}
	}

	@Override
	public SailIteration<? extends Namespace> getNamespaces()
		throws SailException
	{
		SailIteration<? extends Namespace>[] result = new SailIteration[datasets.length];
		for (int i = 0; i < datasets.length; i++) {
			result[i] = datasets[i].getNamespaces();
		}
		return union(result);
	}

	@Override
	public String getNamespace(String prefix)
		throws SailException
	{
		for (int i = 0; i < datasets.length; i++) {
			String result = datasets[i].getNamespace(prefix);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	@Override
	public SailIteration<? extends Resource> getContextIDs()
		throws SailException
	{
		SailIteration<? extends Resource>[] result = new SailIteration[datasets.length];
		for (int i = 0; i < datasets.length; i++) {
			result[i] = datasets[i].getContextIDs();
		}
		return union(result);
	}

	@Override
	public SailIteration<? extends Statement> get(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		SailIteration<? extends Statement>[] result = new SailIteration[datasets.length];
		for (int i = 0; i < datasets.length; i++) {
			result[i] = datasets[i].get(subj, pred, obj, contexts);
		}
		return union(result);
	}

	private <T> SailIteration<? extends T> union(SailIteration<? extends T>[] items) {
		return new UnionSailIteration<T>(items);
	}

}
