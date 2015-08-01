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

import java.util.Map;

import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.spatial4j.core.shape.Shape;

/**
 * This class will try to load a subclass of itself called
 * "org.openrdf.sail.elasticsearch.ElasticsearchSpatialSupportInitializer".
 * This is not provided, and is primarily intended as a way to inject JTS
 * support. If this fails a fall-back is used that doesn't support any shapes.
 */
abstract class ElasticsearchSpatialSupport {
	private static final ElasticsearchSpatialSupport support;

	static {
		ElasticsearchSpatialSupport spatialSupport;
		try {
			Class<?> cls = Class.forName(
					"org.openrdf.sail.elasticsearch.ElasticsearchSpatialSupportInitializer", true,
					Thread.currentThread().getContextClassLoader());
			spatialSupport = (ElasticsearchSpatialSupport)cls.newInstance();
		}
		catch (Exception e) {
			spatialSupport = new DefaultElasticsearchSpatialSupport();
		}
		support = spatialSupport;
	}

	static ElasticsearchSpatialSupport getSpatialSupport() {
		return support;
	}

	protected abstract ShapeBuilder toShapeBuilder(Shape s);
	protected abstract Map<String,Object> toGeoJSON(Shape s);

	private static final class DefaultElasticsearchSpatialSupport extends ElasticsearchSpatialSupport {
		@Override
		protected ShapeBuilder toShapeBuilder(Shape s) {
			throw new UnsupportedOperationException(
					"This shape is not supported due to licensing issues. Feel free to provide your own implementation by using something like JTS: "
							+ s.getClass().getName());
		}

		@Override
		protected XContentBuilder toGeoJSON(Shape s) {
			throw new UnsupportedOperationException(
					"This shape is not supported due to licensing issues. Feel free to provide your own implementation by using something like JTS: "
							+ s.getClass().getName());
		}
	}
}
