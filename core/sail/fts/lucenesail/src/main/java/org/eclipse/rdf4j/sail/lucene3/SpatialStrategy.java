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
package org.eclipse.rdf4j.sail.lucene3;

import org.apache.lucene.spatial.tier.projections.CartesianTierPlotter;
import org.apache.lucene.spatial.tier.projections.FixedSinusoidalProjector;
import org.apache.lucene.spatial.tier.projections.IProjector;

import com.spatial4j.core.context.SpatialContext;

public class SpatialStrategy {

	public static final int DEFAULT_MIN_TIER = 2;

	public static final int DEFAULT_MAX_TIER = 15;

	private static final CartesianTierPlotter utils = new CartesianTierPlotter(0, null, null);

	private final SpatialContext context;

	private final IProjector projector = new FixedSinusoidalProjector();

	private final String fieldPrefix;

	private final int minTier;

	private final int maxTier;

	private final CartesianTierPlotter[] plotters;

	public static int getTier(double miles) {
		return utils.bestFit(miles);
	}

	public SpatialStrategy(String field) {
		this(field, DEFAULT_MIN_TIER, DEFAULT_MAX_TIER, SpatialContext.GEO);
	}

	public SpatialStrategy(String field, int minTier, int maxTier, SpatialContext context) {
		this.context = context;
		this.fieldPrefix = CartesianTierPlotter.DEFALT_FIELD_PREFIX + field + "_";
		this.minTier = minTier;
		this.maxTier = maxTier;
		plotters = new CartesianTierPlotter[maxTier - minTier + 1];
		for (int tier = minTier; tier <= maxTier; tier++) {
			plotters[tier - minTier] = new CartesianTierPlotter(tier, projector, fieldPrefix);
		}
	}

	public SpatialContext getSpatialContext() {
		return context;
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
