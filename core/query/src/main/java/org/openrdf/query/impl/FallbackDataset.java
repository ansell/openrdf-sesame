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
package org.openrdf.query.impl;

import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.query.Dataset;


/**
 *
 * @author james
 */
public class FallbackDataset implements Dataset {

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

	public Set<URI> getDefaultGraphs() {
		Set<URI> set = primary.getDefaultGraphs();
		if (set == null || set.isEmpty())
			return fallback.getDefaultGraphs();
		return set;
	}

	public Set<URI> getNamedGraphs() {
		Set<URI> set = primary.getNamedGraphs();
		if (set == null || set.isEmpty())
			return fallback.getNamedGraphs();
		return set;
	}

	public URI getDefaultInsertGraph() {
		URI graph = primary.getDefaultInsertGraph();
		if (graph == null)
			return fallback.getDefaultInsertGraph();
		return graph;
	}

	public Set<URI> getDefaultRemoveGraphs() {
		Set<URI> set = primary.getDefaultRemoveGraphs();
		if (set == null || set.isEmpty())
			return fallback.getDefaultRemoveGraphs();
		return set;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (URI uri : getDefaultRemoveGraphs()) {
			sb.append("DELETE FROM ");
			appendURI(sb, uri);
		}
		sb.append("INSERT INTO ");
		appendURI(sb, getDefaultInsertGraph());
		for (URI uri : getDefaultGraphs()) {
			sb.append("USING ");
			appendURI(sb, uri);
		}
		for (URI uri : getNamedGraphs()) {
			sb.append("USING NAMED ");
			appendURI(sb, uri);
		}
		return sb.toString();
	}

	private void appendURI(StringBuilder sb, URI uri) {
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
