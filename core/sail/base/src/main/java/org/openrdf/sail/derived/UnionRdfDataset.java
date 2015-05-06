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
package org.openrdf.sail.derived;

import java.util.Arrays;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailException;

/**
 * @author James Leigh
 */
class UnionRdfDataset implements RdfDataset {

	private final RdfDataset[] datasets;

	public UnionRdfDataset(RdfDataset... datasets) {
		this.datasets = datasets;
	}

	public String toString() {
		return Arrays.asList(datasets).toString();
	}

	@Override
	public void close()
		throws SailException
	{
		for (RdfDataset dataset : datasets) {
			dataset.close();
		}
	}

	@Override
	public RdfIteration<? extends Namespace> getNamespaces()
		throws SailException
	{
		RdfIteration<? extends Namespace>[] result = new RdfIteration[datasets.length];
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
	public RdfIteration<? extends Resource> getContextIDs()
		throws SailException
	{
		RdfIteration<? extends Resource>[] result = new RdfIteration[datasets.length];
		for (int i = 0; i < datasets.length; i++) {
			result[i] = datasets[i].getContextIDs();
		}
		return union(result);
	}

	@Override
	public RdfIteration<? extends Statement> get(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		RdfIteration<? extends Statement>[] result = new RdfIteration[datasets.length];
		for (int i = 0; i < datasets.length; i++) {
			result[i] = datasets[i].get(subj, pred, obj, contexts);
		}
		return union(result);
	}

	private <T> RdfIteration<? extends T> union(RdfIteration<? extends T>[] items) {
		return new UnionRdfIteration<T>(items);
	}

}
