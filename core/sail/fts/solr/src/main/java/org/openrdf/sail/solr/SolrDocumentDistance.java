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
package org.openrdf.sail.solr;

import org.openrdf.model.URI;
import org.openrdf.sail.lucene.DocumentDistance;
import org.openrdf.sail.lucene.util.GeoUnits;

public class SolrDocumentDistance extends SolrDocumentResult implements DocumentDistance {

	private final URI units;

	public SolrDocumentDistance(SolrSearchDocument doc, URI units) {
		super(doc);
		this.units = units;
	}

	@Override
	public double getDistance() {
		Number s = ((Number)doc.getDocument().get(SolrIndex.DISTANCE_FIELD));
		return (s != null) ? GeoUnits.fromKilometres(s.doubleValue(), units) : Double.NaN;
	}
}
