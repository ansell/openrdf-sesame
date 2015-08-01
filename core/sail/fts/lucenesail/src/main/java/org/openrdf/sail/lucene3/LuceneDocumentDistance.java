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
package org.openrdf.sail.lucene3;

import java.util.Set;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.spatial.tier.DistanceFilter;
import org.openrdf.model.URI;
import org.openrdf.sail.lucene.DocumentDistance;
import org.openrdf.sail.lucene.SearchFields;
import org.openrdf.sail.lucene.util.GeoUnits;

import com.google.common.collect.Sets;

public class LuceneDocumentDistance extends LuceneDocumentResult implements DocumentDistance {

	private final URI units;

	private final DistanceFilter distanceFilter;

	private static Set<String> requiredFields(String geoProperty, boolean includeContext) {
		Set<String> fields = Sets.newHashSet(SearchFields.URI_FIELD_NAME, geoProperty);
		if(includeContext) {
			fields.add(SearchFields.CONTEXT_FIELD_NAME);
		}
		return fields;
	}

	public LuceneDocumentDistance(ScoreDoc doc, String geoProperty, URI units, DistanceFilter df,
			boolean includeContext, LuceneIndex index)
	{
		super(doc, index, requiredFields(geoProperty, includeContext));
		this.units = units;
		this.distanceFilter = df;
	}

	@Override
	public double getDistance() {
		double miles = distanceFilter.getDistance(scoreDoc.doc);
		return GeoUnits.fromMiles(miles, units);
	}
}
