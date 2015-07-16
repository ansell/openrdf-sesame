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

import org.apache.lucene.spatial.tier.projections.CartesianTierPlotter;
import org.apache.lucene.spatial.tier.projections.FixedSinusoidalProjector;
import org.apache.lucene.spatial.tier.projections.IProjector;

public class CartesianTiers {

	public static final int DEFAULT_MIN_TIER = 2;

	public static final int DEFAULT_MAX_TIER = 15;

	private static final CartesianTierPlotter utils = new CartesianTierPlotter(0, null, null);

	private final IProjector projector = new FixedSinusoidalProjector();

	private final String fieldPrefix;

	private final int minTier;

	private final int maxTier;

	private final CartesianTierPlotter[] plotters;

	public static int getTier(double miles) {
		return utils.bestFit(miles);
	}

	public CartesianTiers(String field) {
		this(field, DEFAULT_MIN_TIER, DEFAULT_MAX_TIER);
	}

	public CartesianTiers(String field, int minTier, int maxTier) {
		this.fieldPrefix = CartesianTierPlotter.DEFALT_FIELD_PREFIX + field + "_";
		this.minTier = minTier;
		this.maxTier = maxTier;
		plotters = new CartesianTierPlotter[maxTier - minTier + 1];
		for (int tier = minTier; tier <= maxTier; tier++) {
			plotters[tier - minTier] = new CartesianTierPlotter(tier, projector, fieldPrefix);
		}
	}

	public String getFieldPrefix() {
		return fieldPrefix;
	}

	public int getMinTier() {
		return minTier;
	}

	public int getMaxTier() {
		return maxTier;
	}

	public CartesianTierPlotter[] getPlotters() {
		return plotters;
	}
}
