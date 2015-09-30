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
package org.openrdf.rio.nquads;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.BasicWriterSettings;
import org.openrdf.rio.helpers.NTriplesWriterSettings;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.model.Statement;

import java.io.OutputStream;
import java.io.Writer;
import java.io.IOException;

/**
 * RDFWriter implementation for the {@link org.openrdf.rio.RDFFormat#NQUADS
 * N-Quads} RDF format.
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
	public RDFFormat getRDFFormat() {
		return RDFFormat.NQUADS;
	}

	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		if (!writingStarted) {
			throw new RuntimeException("Document writing has not yet been started");
		}

		try {
			// SUBJECT
			NTriplesUtil.append(st.getSubject(), writer);
			writer.write(" ");

			// PREDICATE
			NTriplesUtil.append(st.getPredicate(), writer);
			writer.write(" ");

			// OBJECT
			NTriplesUtil.append(st.getObject(), writer,
					getWriterConfig().get(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL),
					getWriterConfig().get(NTriplesWriterSettings.ESCAPE_UNICODE));

			if (null != st.getContext()) {
				writer.write(" ");
				NTriplesUtil.append(st.getContext(), writer);
			}

			writer.write(" .\n");
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}
}
