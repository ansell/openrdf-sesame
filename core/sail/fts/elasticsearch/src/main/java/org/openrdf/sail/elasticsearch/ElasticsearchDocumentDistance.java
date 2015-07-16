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
package org.openrdf.sail.elasticsearch;

import org.elasticsearch.common.geo.GeoDistance.FixedSourceDistance;
import org.elasticsearch.common.geo.GeoHashUtils;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.search.SearchHit;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.GEOF;
import org.openrdf.sail.lucene.DocumentDistance;
import org.openrdf.sail.lucene.SearchDocument;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;

public class ElasticsearchDocumentDistance implements DocumentDistance {

	private final SearchHit hit;

	private final SpatialContext geoContext;

	private final String geoPointField;

	private final URI units;

	private final FixedSourceDistance srcDistance;

	private final DistanceUnit unit;

	private ElasticsearchDocument fullDoc;

	public ElasticsearchDocumentDistance(SearchHit hit, SpatialContext geoContext, String geoPointField,
			URI units, FixedSourceDistance srcDistance, DistanceUnit unit)
	{
		this.hit = hit;
		this.geoContext = geoContext;
		this.geoPointField = geoPointField;
		this.units = units;
		this.srcDistance = srcDistance;
		this.unit = unit;
	}

	@Override
	public SearchDocument getDocument() {
		if (fullDoc == null) {
			fullDoc = new ElasticsearchDocument(hit, geoContext);
		}
		return fullDoc;
	}

	@Override
	public double getDistance() {
		String geohash = (String)((ElasticsearchDocument)getDocument()).getSource().get(geoPointField);
		GeoPoint point = GeoHashUtils.decode(geohash);
		double unitDist = srcDistance.calculate(point.getLat(), point.getLon());
		double distance;
		if (GEOF.UOM_METRE.equals(units)) {
			distance = unit.toMeters(unitDist);
		}
		else if (GEOF.UOM_DEGREE.equals(units)) {
			distance = unitDist / unit.getDistancePerDegree();
		}
		else if (GEOF.UOM_RADIAN.equals(units)) {
			distance = DistanceUtils.dist2Radians(unit.convert(unitDist, DistanceUnit.KILOMETERS),
					DistanceUtils.EARTH_MEAN_RADIUS_KM);
		}
		else if (GEOF.UOM_UNITY.equals(units)) {
			distance = unit.convert(unitDist, DistanceUnit.KILOMETERS)
					/ (Math.PI * DistanceUtils.EARTH_MEAN_RADIUS_KM);
		}
		else {
			throw new UnsupportedOperationException("Unsupported units: " + units);
		}
		return distance;
	}
}
