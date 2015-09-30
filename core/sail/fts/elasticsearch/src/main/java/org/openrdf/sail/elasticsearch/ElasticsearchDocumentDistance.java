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
package org.openrdf.sail.elasticsearch;

import org.elasticsearch.common.geo.GeoDistance.FixedSourceDistance;
import org.elasticsearch.common.geo.GeoHashUtils;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.search.SearchHit;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.GEOF;
import org.openrdf.sail.lucene.DocumentDistance;

import com.google.common.base.Function;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;

public class ElasticsearchDocumentDistance extends ElasticsearchDocumentResult implements DocumentDistance {

	private final String geoPointField;

	private final URI units;

	private final FixedSourceDistance srcDistance;

	private final DistanceUnit unit;

	public ElasticsearchDocumentDistance(SearchHit hit, Function<? super String,? extends SpatialContext> geoContextMapper, String geoPointField,
			URI units, FixedSourceDistance srcDistance, DistanceUnit unit)
	{
		super(hit, geoContextMapper);
		this.geoPointField = geoPointField;
		this.units = units;
		this.srcDistance = srcDistance;
		this.unit = unit;
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
