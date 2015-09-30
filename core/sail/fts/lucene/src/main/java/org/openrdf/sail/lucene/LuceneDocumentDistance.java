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
package org.openrdf.sail.lucene;

import java.text.ParseException;
import java.util.List;
import java.util.Set;

import org.apache.lucene.search.ScoreDoc;
import org.openrdf.model.URI;
import org.openrdf.sail.lucene.util.GeoUnits;

import com.google.common.collect.Sets;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;

public class LuceneDocumentDistance extends LuceneDocumentResult implements DocumentDistance {

	private final String geoProperty;

	private final URI units;

	private final Point origin;

	private static Set<String> requiredFields(String geoProperty, boolean includeContext) {
		Set<String> fields = Sets.newHashSet(SearchFields.URI_FIELD_NAME, geoProperty);
		if(includeContext) {
			fields.add(SearchFields.CONTEXT_FIELD_NAME);
		}
		return fields;
	}

	public LuceneDocumentDistance(ScoreDoc doc, String geoProperty, URI units, Point origin, boolean includeContext, LuceneIndex index)
	{
		super(doc, index, requiredFields(geoProperty, includeContext));
		this.geoProperty = geoProperty;
		this.units = units;
		this.origin = origin;
	}

	@Override
	public double getDistance() {
		List<String> wkts = getDocument().getProperty(geoProperty);
		double min = Double.POSITIVE_INFINITY;
		for (String wkt : wkts) {
			Shape shape;
			try {
				shape = index.getSpatialContext(geoProperty).readShapeFromWkt(wkt);
				double dist = index.getSpatialContext(geoProperty).calcDistance(shape.getCenter(), origin);
				min = Math.min(dist, min);
			}
			catch (ParseException e) {
				// ignore
			}
		}
		return GeoUnits.fromDegrees(min, units);
	}
}
