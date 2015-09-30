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
package org.eclipse.rdf4j.sail.lucene.util;

import com.spatial4j.core.distance.DistanceUtils;

import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.vocabulary.GEOF;

public final class GeoUnits {
	private GeoUnits() {}

	public static final double toMiles(double distance, URI units) {
		final double miles;
		if(GEOF.UOM_METRE.equals(units)) {
			miles = DistanceUtils.KM_TO_MILES*distance/1000.0;
		} else if(GEOF.UOM_DEGREE.equals(units)) {
			miles = DistanceUtils.degrees2Dist(distance, DistanceUtils.EARTH_MEAN_RADIUS_MI);
		} else if(GEOF.UOM_RADIAN.equals(units)) {
			miles = DistanceUtils.radians2Dist(distance, DistanceUtils.EARTH_MEAN_RADIUS_MI);
		} else if(GEOF.UOM_UNITY.equals(units)) {
			miles = distance*Math.PI*DistanceUtils.EARTH_MEAN_RADIUS_MI;
		} else {
			throw new IllegalArgumentException("Unsupported units: "+units);
		}
		return miles;
	}

	public static final double fromMiles(double miles, URI units) {
		double dist;
		if(GEOF.UOM_METRE.equals(units)) {
			dist = DistanceUtils.MILES_TO_KM*miles*1000.0;
		} else if(GEOF.UOM_DEGREE.equals(units)) {
			dist = DistanceUtils.dist2Degrees(miles, DistanceUtils.EARTH_MEAN_RADIUS_MI);
		} else if(GEOF.UOM_RADIAN.equals(units)) {
			dist = DistanceUtils.dist2Radians(miles, DistanceUtils.EARTH_MEAN_RADIUS_MI);
		} else if(GEOF.UOM_UNITY.equals(units)) {
			dist = miles/(Math.PI*DistanceUtils.EARTH_MEAN_RADIUS_MI);
		} else {
			throw new IllegalArgumentException("Unsupported units: "+units);
		}
		return dist;
	}

	public static final double toKilometres(double distance, URI units) {
		final double kms;
		if(GEOF.UOM_METRE.equals(units)) {
			kms = distance/1000.0;
		} else if(GEOF.UOM_DEGREE.equals(units)) {
			kms = DistanceUtils.degrees2Dist(distance, DistanceUtils.EARTH_MEAN_RADIUS_KM);
		} else if(GEOF.UOM_RADIAN.equals(units)) {
			kms = DistanceUtils.radians2Dist(distance, DistanceUtils.EARTH_MEAN_RADIUS_KM);
		} else if(GEOF.UOM_UNITY.equals(units)) {
			kms = distance*Math.PI*DistanceUtils.EARTH_MEAN_RADIUS_KM;
		} else {
			throw new IllegalArgumentException("Unsupported units: "+units);
		}
		return kms;
	}

	public static final double fromKilometres(double kms, URI units) {
		double dist;
		if(GEOF.UOM_METRE.equals(units)) {
			dist = kms*1000.0;
		} else if(GEOF.UOM_DEGREE.equals(units)) {
			dist = DistanceUtils.dist2Degrees(kms, DistanceUtils.EARTH_MEAN_RADIUS_KM);
		} else if(GEOF.UOM_RADIAN.equals(units)) {
			dist = DistanceUtils.dist2Radians(kms, DistanceUtils.EARTH_MEAN_RADIUS_KM);
		} else if(GEOF.UOM_UNITY.equals(units)) {
			dist = kms/(Math.PI*DistanceUtils.EARTH_MEAN_RADIUS_KM);
		} else {
			throw new IllegalArgumentException("Unsupported units: "+units);
		}
		return dist;
	}

	public static final double toDegrees(double distance, URI units) {
		final double degs;
		if(GEOF.UOM_METRE.equals(units)) {
			degs = DistanceUtils.dist2Degrees(distance/1000.0, DistanceUtils.EARTH_MEAN_RADIUS_KM);
		} else if(GEOF.UOM_DEGREE.equals(units)) {
			degs = distance;
		} else if(GEOF.UOM_RADIAN.equals(units)) {
			degs = DistanceUtils.RADIANS_TO_DEGREES*distance;
		} else if(GEOF.UOM_UNITY.equals(units)) {
			degs = distance*180.0;
		} else {
			throw new IllegalArgumentException("Unsupported units: "+units);
		}
		return degs;
	}

	public static final double fromDegrees(double degs, URI units) {
		double dist;
		if(GEOF.UOM_METRE.equals(units)) {
			dist = DistanceUtils.degrees2Dist(degs, DistanceUtils.EARTH_MEAN_RADIUS_KM)*1000.0;
		} else if(GEOF.UOM_DEGREE.equals(units)) {
			dist = degs;
		} else if(GEOF.UOM_RADIAN.equals(units)) {
			dist = DistanceUtils.DEGREES_TO_RADIANS*degs;
		} else if(GEOF.UOM_UNITY.equals(units)) {
			dist = degs/180.0;
		} else {
			throw new IllegalArgumentException("Unsupported units: "+units);
		}
		return dist;
	}
}
