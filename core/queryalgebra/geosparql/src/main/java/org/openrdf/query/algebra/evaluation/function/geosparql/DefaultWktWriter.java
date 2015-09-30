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

import java.io.IOException;

import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.ShapeCollection;
import com.spatial4j.core.shape.impl.BufferedLineString;

final class DefaultWktWriter implements WktWriter {

	private String notSupported(Shape s) {
		throw new UnsupportedOperationException(
				"This shape is not supported due to licensing issues. Feel free to provide your own implementation by using something like JTS: "
						+ s.getClass().getName());
	}

	@Override
	public String toWkt(Shape shape)
		throws IOException
	{
		if (shape instanceof Point) {
			Point p = (Point)shape;
			return "POINT " + toCoords(p);
		}
		else if (shape instanceof ShapeCollection<?>) {
			ShapeCollection<?> col = (ShapeCollection<?>)shape;
			if (col.isEmpty()) {
				return "GEOMETRYCOLLECTION EMPTY";
			}
			Class<?> elementType = null;
			StringBuilder buf = new StringBuilder(" (");
			String sep = "";
			for (Shape s : col) {
				if (elementType == null) {
					elementType = s.getClass();
				}
				else if (!elementType.equals(s.getClass())) {
					elementType = Shape.class;
				}
				buf.append(sep).append(toCoords(s));
				sep = ", ";
			}
			buf.append(")");
			if (Point.class.isAssignableFrom(elementType)) {
				buf.insert(0, "MULTIPOINT");
			}
			else if (elementType == Shape.class) {
				buf.insert(0, "GEOMETRYCOLLECTION");
			}
			else {
				return notSupported(shape);
			}
			return buf.toString();
		}
		else if (shape instanceof BufferedLineString) {
			BufferedLineString ls = (BufferedLineString)shape;
			return "LINESTRING " + toCoords(ls);
		}
		return notSupported(shape);
	}

	private String toCoords(Shape shape)
		throws IOException
	{
		if (shape instanceof Point) {
			Point p = (Point)shape;
			return toCoords(p);
		}
		else if (shape instanceof BufferedLineString) {
			BufferedLineString ls = (BufferedLineString)shape;
			return toCoords(ls);
		}
		return notSupported(shape);
	}

	private String toCoords(Point p)
		throws IOException
	{
		if (p.isEmpty()) {
			return "EMPTY";
		}
		else {
			return "(" + p.getX() + " " + p.getY() + ")";
		}
	}

	private String toCoords(BufferedLineString shape)
		throws IOException
	{
		double buffer = shape.getBuf();
		if (buffer != 0.0) {
			return notSupported(shape);
		}
		StringBuilder buf = new StringBuilder("(");
		String sep = "";
		for (Point p : shape.getPoints()) {
			buf.append(sep);
			buf.append(p.getX()).append(" ").append(p.getY());
			sep = ", ";
		}
		buf.append(")");
		return buf.toString();
	}
}