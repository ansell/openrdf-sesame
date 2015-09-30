/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.sail.base;

import java.util.Arrays;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.UnionIteration;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
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
	public CloseableIteration<? extends Namespace, SailException> getNamespaces()
		throws SailException
	{
		CloseableIteration<? extends Namespace, SailException>[] result;
		result = new CloseableIteration[datasets.length];
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
	public CloseableIteration<? extends Resource, SailException> getContextIDs()
		throws SailException
	{
		CloseableIteration<? extends Resource, SailException>[] result;
		result = new CloseableIteration[datasets.length];
		for (int i = 0; i < datasets.length; i++) {
			result[i] = datasets[i].getContextIDs();
		}
		return union(result);
	}

	@Override
	public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, IRI pred,
			Value obj, Resource... contexts)
		throws SailException
	{
		CloseableIteration<? extends Statement, SailException>[] result;
		result = new CloseableIteration[datasets.length];
		for (int i = 0; i < datasets.length; i++) {
			result[i] = datasets[i].getStatements(subj, pred, obj, contexts);
		}
		return union(result);
	}

	private <T> CloseableIteration<? extends T, SailException> union(
			CloseableIteration<? extends T, SailException>[] items)
	{
		return new UnionIteration<T, SailException>(items);
	}

}
