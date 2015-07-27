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
package org.openrdf.sail.lucene4;

import java.text.ParseException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.openrdf.model.URI;
import org.openrdf.sail.lucene.DocumentDistance;
import org.openrdf.sail.lucene.SearchDocument;
import org.openrdf.sail.lucene.SearchFields;
import org.openrdf.sail.lucene.util.GeoUnits;

import com.google.common.collect.Sets;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;

public class LuceneDocumentDistance implements DocumentDistance {

	private final ScoreDoc scoreDoc;

	private final String geoProperty;

	private final URI units;

	private final Point origin;

	private final LuceneIndex index;

	private LuceneDocument fullDoc;

	public LuceneDocumentDistance(ScoreDoc doc, String geoProperty, URI units, Point origin, LuceneIndex index)
	{
		this.scoreDoc = doc;
		this.geoProperty = geoProperty;
		this.units = units;
		this.origin = origin;
		this.index = index;
	}

	@Override
	public SearchDocument getDocument() {
		if (fullDoc == null) {
			Document doc = index.getDocument(scoreDoc.doc,
					Sets.newHashSet(SearchFields.URI_FIELD_NAME, geoProperty));
			fullDoc = new LuceneDocument(doc, index.getSpatialContext(), index.getSpatialPrefixTree());
		}
		return fullDoc;
	}

	@Override
	public double getDistance() {
		List<String> wkts = getDocument().getProperty(geoProperty);
		double min = Double.POSITIVE_INFINITY;
		for (String wkt : wkts) {
			Shape shape;
			try {
				shape = index.getSpatialContext().readShapeFromWkt(wkt);
				double dist = index.getSpatialContext().calcDistance(shape.getCenter(), origin);
				min = Math.min(dist, min);
			}
			catch (ParseException e) {
				// ignore
			}
		}
		return GeoUnits.fromDegrees(min, units);
	}
}
