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
import org.openrdf.query.parser.ParsedUpdate;

/**
 * @author Jeen Broekstra
 */
public abstract class AbstractParserUpdate extends AbstractUpdate {

	private final ParsedUpdate parsedUpdate;

	protected AbstractParserUpdate(ParsedUpdate parsedUpdate) {
		this.parsedUpdate = parsedUpdate;
	}

	public ParsedUpdate getParsedUpdate() {
		return parsedUpdate;
	}

	@Override
	public String toString() {
		return parsedUpdate.toString();
	}

	protected Dataset getMergedDataset(Dataset sparqlDefinedDataset) {
		if (sparqlDefinedDataset == null) {
			return dataset;
		}
		else if (dataset == null) {
			return sparqlDefinedDataset;
		}
		else {
			DatasetImpl mergedDataset = new DatasetImpl();

			boolean merge = false;

			Set<URI> dgs = sparqlDefinedDataset.getDefaultGraphs();
			if (dgs != null && dgs.size() > 0) {
				merge = true;
				// one or more USING-clauses in the update itself, we need to define
				// the default graphs by means of the update itself
				for (URI graphURI : dgs) {
					mergedDataset.addDefaultGraph(graphURI);
				}
			}

			Set<URI> ngs = sparqlDefinedDataset.getNamedGraphs();
			if (ngs != null && ngs.size() > 0) {
				merge = true;
				// one or more USING NAMED-claused in the update, we need to define
				// the named graphs by means of the update itself.
				for (URI graphURI : ngs) {
					mergedDataset.addNamedGraph(graphURI);
				}
			}

			if (merge) {
				mergedDataset.setDefaultInsertGraph(dataset.getDefaultInsertGraph());

				for (URI graphURI : dataset.getDefaultRemoveGraphs()) {
					mergedDataset.addDefaultRemoveGraph(graphURI);
				}

				return mergedDataset;
			}
			else {
				return sparqlDefinedDataset;
			}

		}
	}
}
