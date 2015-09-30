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
package org.eclipse.rdf4j.query.impl;

import java.io.Serializable;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.Dataset;


/**
 *
 * @author james
 */
public class FallbackDataset implements Dataset, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5866540736738270376L;

	public static Dataset fallback(Dataset primary, Dataset fallback) {
		if (primary == null)
			return fallback;
		if (fallback == null)
			return primary;
		return new FallbackDataset(primary, fallback);
	}

	private Dataset primary;
	private Dataset fallback;

	private FallbackDataset(Dataset primary, Dataset secondary) {
		assert primary != null;
		assert secondary != null;
		this.primary = primary;
		this.fallback = secondary;
	}

	public Set<IRI> getDefaultGraphs() {
		Set<IRI> set = primary.getDefaultGraphs();
		if (set == null || set.isEmpty())
			return fallback.getDefaultGraphs();
		return set;
	}

	public Set<IRI> getNamedGraphs() {
		Set<IRI> set = primary.getNamedGraphs();
		if (set == null || set.isEmpty())
			return fallback.getNamedGraphs();
		return set;
	}

	public IRI getDefaultInsertGraph() {
		IRI graph = primary.getDefaultInsertGraph();
		if (graph == null)
			return fallback.getDefaultInsertGraph();
		return graph;
	}

	public Set<IRI> getDefaultRemoveGraphs() {
		Set<IRI> set = primary.getDefaultRemoveGraphs();
		if (set == null || set.isEmpty())
			return fallback.getDefaultRemoveGraphs();
		return set;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (IRI uri : getDefaultRemoveGraphs()) {
			sb.append("DELETE FROM ");
			appendURI(sb, uri);
		}
		sb.append("INSERT INTO ");
		appendURI(sb, getDefaultInsertGraph());
		for (IRI uri : getDefaultGraphs()) {
			sb.append("USING ");
			appendURI(sb, uri);
		}
		for (IRI uri : getNamedGraphs()) {
			sb.append("USING NAMED ");
			appendURI(sb, uri);
		}
		return sb.toString();
	}

	private void appendURI(StringBuilder sb, IRI uri) {
		String str = uri.toString();
		if (str.length() > 50) {
			sb.append("<").append(str, 0, 19).append("..");
			sb.append(str, str.length() - 29, str.length()).append(">\n");
		}
		else {
			sb.append("<").append(uri).append(">\n");
		}
	}

}
