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
package org.openrdf.query.algebra.evaluation.function.geosparql;

import java.util.HashMap;
import java.util.Map;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.SpatialContextFactory;

/**
 * This class is responsible for creating the
 * {@link com.spatial4j.core.context.SpatialContext}, {@link SpatialAlegbra} and
 * {@link WktWriter} that will be used. It will first try to load a subclass of
 * itself called
 * "org.openrdf.query.algebra.evaluation.function.geosparql.SpatialSupportInitializer"
 * . This is not provided, and is primarily intended as a way to inject JTS
 * support. If this fails then the following fall-backs are used:
 * <ul>
 * <li>a SpatialContext created by passing system properties with the prefix
 * "spatialSupport." to {@link com.spatial4j.core.context.SpatialContextFactory}
 * . The prefix is stripped from the system property name to form the
 * SpatialContextFactory argument name.</li>
 * <li>a SpatialAlgebra that does not support any operation.</li>
 * <li>a WktWriter that only supports points</li>.
 * </ul>
 */
abstract class SpatialSupport {

	private static final SpatialContext spatialContext;

	private static final SpatialAlgebra spatialAlgebra;

	private static final WktWriter wktWriter;

	static {
		SpatialSupport support;
		try {
			Class<?> cls = Class.forName(
					"org.openrdf.query.algebra.evaluation.function.geosparql.SpatialSupportInitializer", true,
					Thread.currentThread().getContextClassLoader());
			support = (SpatialSupport)cls.newInstance();
		}
		catch (Exception e) {
			support = new DefaultSpatialSupport();
		}
		spatialContext = support.createSpatialContext();
		spatialAlgebra = support.createSpatialAlgebra();
		wktWriter = support.createWktWriter();
	}

	static SpatialContext getSpatialContext() {
		return spatialContext;
	}

	static SpatialAlgebra getSpatialAlgebra() {
		return spatialAlgebra;
	}

	static WktWriter getWktWriter() {
		return wktWriter;
	}

	protected abstract SpatialContext createSpatialContext();

	protected abstract SpatialAlgebra createSpatialAlgebra();

	protected abstract WktWriter createWktWriter();

	private static final class DefaultSpatialSupport extends SpatialSupport {

		private static final String SYSTEM_PROPERTY_PREFIX = "spatialSupport.";

		@Override
		protected SpatialContext createSpatialContext() {
			Map<String, String> args = new HashMap<String, String>();
			for (String key : System.getProperties().stringPropertyNames()) {
				if (key.startsWith(SYSTEM_PROPERTY_PREFIX)) {
					args.put(key.substring(SYSTEM_PROPERTY_PREFIX.length()), System.getProperty(key));
				}
			}
			return SpatialContextFactory.makeSpatialContext(args, Thread.currentThread().getContextClassLoader());
		}

		@Override
		protected SpatialAlgebra createSpatialAlgebra() {
			return new DefaultSpatialAlgebra();
		}

		@Override
		protected WktWriter createWktWriter() {
			return new DefaultWktWriter();
		}
	}
}
