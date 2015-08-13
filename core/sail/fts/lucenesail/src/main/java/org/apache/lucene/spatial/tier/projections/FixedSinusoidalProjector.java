package org.apache.lucene.spatial.tier.projections;

import org.apache.lucene.spatial.DistanceUtils;

public class FixedSinusoidalProjector implements IProjector {

	@Override
	public String coordsAsString(double latitude, double longitude) {
		double[] coords = coords(latitude, longitude);
		return coords[0] + "," + coords[1];
	}

	@Override
	public double[] coords(double latitude, double longitude) {
		double rlat = latitude * DistanceUtils.DEGREES_TO_RADIANS;
		double rlong = longitude * DistanceUtils.DEGREES_TO_RADIANS;
		double x = rlong * Math.cos(rlat);
		return new double[] { x, rlat };

	}

}
