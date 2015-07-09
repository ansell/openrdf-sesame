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
package org.openrdf.sail.lucene.util;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.GEOF;

import com.spatial4j.core.distance.DistanceUtils;

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
}
