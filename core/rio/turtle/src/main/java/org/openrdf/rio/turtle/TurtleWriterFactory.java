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
package org.openrdf.rio.turtle;

import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

/**
 * An {@link RDFWriterFactory} for Turtle writers.
 * 
 * @author Arjohn Kampman
 */
public class TurtleWriterFactory implements RDFWriterFactory {

	/**
	 * Returns {@link RDFFormat#TURTLE}.
	 */
	@Override
	public RDFFormat getRDFFormat() {
		return RDFFormat.TURTLE;
	}

	@Override
	public RDFWriter getWriter(OutputStream out) {
		return new TurtleWriter(out);
	}

	@Override
	public RDFWriter getWriter(OutputStream out, URI baseURI) {
		return new TurtleWriter(out, baseURI);
	}

	@Override
	public RDFWriter getWriter(Writer writer) {
		return new TurtleWriter(writer);
	}

	@Override
	public RDFWriter getWriter(Writer writer, URI baseURI) {
		return new TurtleWriter(writer, baseURI);
	}
}
