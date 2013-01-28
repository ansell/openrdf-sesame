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
package org.openrdf.rio.nquads;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.model.Statement;

import java.io.OutputStream;
import java.io.Writer;
import java.io.IOException;

/**
 * RDFWriter implementation for the {@link RDFFormat#NQUADS N-Quads} RDF format.
 * 
 * @since 2.7.0
 * @author Joshua Shinavier
 */
public class NQuadsWriter extends NTriplesWriter {
	public NQuadsWriter(OutputStream outputStream) {
		super(outputStream);
	}

	public NQuadsWriter(Writer writer) {
		super(writer);
	}

	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {
		if (!writingStarted) {
			throw new RuntimeException(
					"Document writing has not yet been started");
		}

		try {
			// SUBJECT
			NTriplesUtil.append(st.getSubject(), writer);
			writer.write(" ");

			// PREDICATE
			NTriplesUtil.append(st.getPredicate(), writer);
			writer.write(" ");

			// OBJECT
			NTriplesUtil.append(st.getObject(), writer);

			if (null != st.getContext()) {
				writer.write(" ");
				NTriplesUtil.append(st.getContext(), writer);
			}

			writer.write(" .\n");
		} catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}
}
