package org.openrdf.query.algebra.evaluation.function.geosparql;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.ShapeCollection;
import com.spatial4j.core.shape.impl.BufferedLineString;

/**
 * This class is responsible for creating the
 * {@link com.spatial4j.core.context.SpatialContext},
 * {@link SpatialAlegbra}
 * and {@link WktWriter} that will be used.
 * It will first try to load a subclass of itself called "org.openrdf.query.algebra.evaluation.function.geosparql.SpatialSupportInitializer".
 * This is not provided, and is primarily intended as a way to inject JTS support.
 * If this fails then the following fall-backs are used:
 * <ul>
 * <li>a SpatialContext created by passing system properties with the prefix "spatialContext."
 * to {@link com.spatial4j.core.context.SpatialContextFactory}.
 * The prefix is stripped from the system property name to form the SpatialContextFactory argument name.</li>
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
			Class<?> cls = Class.forName("org.openrdf.query.algebra.evaluation.function.geosparql.SpatialSupportInitializer", true, Thread.currentThread().getContextClassLoader());
			support = (SpatialSupport) cls.newInstance();
		} catch (Exception e) {
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
		private static final String SYSTEM_PROPERTY_PREFIX = "spatialContext.";

		@Override
		protected SpatialContext createSpatialContext() {
			Map<String,String> args = new HashMap<String,String>();
			for(String key : System.getProperties().stringPropertyNames()) {
				if(key.startsWith(SYSTEM_PROPERTY_PREFIX)) {
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


	private static final class DefaultSpatialAlgebra implements SpatialAlgebra {

		private <T> T notSupported() {
			throw new UnsupportedOperationException("Not supported due to licensing issues. Feel free to provide your own implementation by using something like JTS.");
		}

		private Shape createEmptyPoint() {
			return getSpatialContext().makePoint(Double.NaN, Double.NaN);
		}

		private Shape createEmptyGeometry() {
			return new ShapeCollection<Shape>(Collections.<Shape>emptyList(), getSpatialContext());
		}

		@Override
		public Shape convexHull(Shape s) {
			if(s instanceof Point) {
				return s;
			} else if(s instanceof ShapeCollection<?>) {
				return new BufferedLineString((ShapeCollection<Point>)s, 0.0, getSpatialContext());
			}
			return notSupported();
		}

		@Override
		public Shape boundary(Shape s) {
			if(s instanceof Point) {
				// points have no boundary so return empty shape
				return createEmptyGeometry();
			} else if(s instanceof ShapeCollection<?>) {
				ShapeCollection<?> col = (ShapeCollection<?>) s;
				if(col.isEmpty()) {
					return createEmptyGeometry();
				}
				for(Shape p : col) {
					if(!(p instanceof Point)) {
						return notSupported();
					}
				}
				return createEmptyGeometry();
			}
			return notSupported();
		}

		@Override
		public Shape envelope(Shape s) {
			if(s instanceof Point) {
				return s;
			}
			return notSupported();
		}

		@Override
		public Shape union(Shape s1, Shape s2) {
			if(s1 instanceof Point && s2 instanceof Point) {
				Point p1 = (Point) s1;
				Point p2 = (Point) s2;
				int diff = compare(p2, p1);
				if(diff == 0) {
					return s1;
				} else if(diff < 0) {
					p1 = p2;
					p2 = (Point) s1;
				}
				return new ShapeCollection<Point>(Arrays.asList(p1, p2), getSpatialContext());
			}
			return notSupported();
		}

		private int compare(Point p1, Point p2) {
			int diff = Double.compare(p1.getX(), p2.getX());
			if(diff == 0) {
				diff = Double.compare(p1.getY(), p2.getY());
			}
			return diff;
		}

		@Override
		public Shape intersection(Shape s1, Shape s2) {
			if(s1 instanceof Point && s2 instanceof Point) {
				Point p1 = (Point) s1;
				Point p2 = (Point) s2;
				int diff = compare(p2, p1);
				if(diff == 0) {
					return s1;
				} else {
					return createEmptyPoint();
				}
			}
			return notSupported();
		}

		@Override
		public Shape symDifference(Shape s1, Shape s2) {
			if(s1 instanceof Point && s2 instanceof Point) {
				Point p1 = (Point) s1;
				Point p2 = (Point) s2;
				int diff = compare(p2, p1);
				if(diff == 0) {
					return createEmptyPoint();
				} else if(diff < 0) {
					p1 = p2;
					p2 = (Point) s1;
				}
				return new ShapeCollection<Point>(Arrays.asList(p1, p2), getSpatialContext());
			}
			return notSupported();
		}

		@Override
		public Shape difference(Shape s1, Shape s2) {
			if(s1 instanceof Point && s2 instanceof Point) {
				Point p1 = (Point) s1;
				Point p2 = (Point) s2;
				int diff = compare(p2, p1);
				if(diff == 0) {
					return createEmptyPoint();
				}
				return s1;
			}
			return notSupported();
		}

		@Override
		public boolean relate(Shape s1, Shape s2, String intersectionPattern) {
			return notSupported();
		}

		@Override
		public boolean equals(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean sfDisjoint(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean sfIntersects(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean sfTouches(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean sfCrosses(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean sfWithin(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean sfContains(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean sfOverlaps(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean ehDisjoint(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean ehMeet(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean ehOverlap(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean ehCovers(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean ehCoveredBy(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean ehInside(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean ehContains(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean rcc8dc(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean rcc8ec(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean rcc8po(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean rcc8tppi(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean rcc8tpp(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean rcc8ntpp(Shape s1, Shape s2) {
			return notSupported();
		}

		@Override
		public boolean rcc8ntppi(Shape s1, Shape s2) {
			return notSupported();
		}
	}


	private static final class DefaultWktWriter implements WktWriter {

		private String notSupported(Shape s) {
			throw new UnsupportedOperationException("This shape is not supported due to licensing issues. Feel free to provide your own implementation by using something like JTS: "+s.getClass().getName());
		}

		@Override
		public String toWkt(Shape shape) throws IOException {
			if(shape instanceof Point) {
				Point p = (Point) shape;
				return "POINT "+toCoords(p);
			} else if(shape instanceof ShapeCollection<?>) {
				ShapeCollection<?> col = (ShapeCollection<?>) shape;
				if(col.isEmpty()) {
					return "GEOMETRYCOLLECTION EMPTY";
				}
				Class<?> elementType = null;
				StringBuilder buf = new StringBuilder(" (");
				String sep = "";
				for(Shape s : col) {
					if(elementType == null) {
						elementType = s.getClass();
					} else if(!elementType.equals(s.getClass())) {
						elementType = Shape.class;
					}
					buf.append(sep).append(toCoords(s));
					sep = ", ";
				}
				buf.append(")");
				if(Point.class.isAssignableFrom(elementType)) {
					buf.insert(0, "MULTIPOINT");
				} else if(elementType == Shape.class) {
					buf.insert(0,  "GEOMETRYCOLLECTION");
				} else {
					return notSupported(shape);
				}
				return buf.toString();
			} else if(shape instanceof BufferedLineString) {
				BufferedLineString ls = (BufferedLineString) shape;
				return "LINESTRING "+toCoords(ls);
			}
			return notSupported(shape);
		}

		private String toCoords(Shape shape) throws IOException {
			if(shape instanceof Point) {
				Point p = (Point) shape;
				return toCoords(p);
			} else if(shape instanceof BufferedLineString) {
				BufferedLineString ls = (BufferedLineString) shape;
				return toCoords(ls);
			}
			return notSupported(shape);
		}

		private String toCoords(Point p) throws IOException {
			if(p.isEmpty()) {
				return "EMPTY";
			} else {
				return "("+p.getX()+" "+p.getY()+")";
			}
		}

		private String toCoords(BufferedLineString shape) throws IOException {
			double buffer = shape.getBuf();
			if(buffer != 0.0) {
				return notSupported(shape);
			}
			StringBuilder buf = new StringBuilder("(");
			String sep = "";
			for(Point p : shape.getPoints()) {
				buf.append(sep);
				buf.append(p.getX()).append(" ").append(p.getY());
				sep = ", ";
			}
			buf.append(")");
			return buf.toString();
		}
	}
}
