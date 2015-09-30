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
package org.openrdf.rio.jsonld;

import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.RDFDataset;

/**
 * A package private internal implementation class.
 *
 * @author Peter Ansell
 */
class JSONLDInternalRDFParser implements com.github.jsonldjava.core.RDFParser {

	public void setPrefix(RDFDataset result, String fullUri, String prefix) {
		result.setNamespace(fullUri, prefix);
	}

	public void handleStatement(RDFDataset result, Statement nextStatement) {
		// TODO: from a basic look at the code it seems some of these could be
		// null
		// null values for IRIs will probably break things further down the line
		// and i'm not sure yet if this should be something handled later on, or
		// something that should be checked here
		final String subject = getResourceValue(nextStatement.getSubject());
		final String predicate = getResourceValue(nextStatement.getPredicate());
		final Value object = nextStatement.getObject();
		final String graphName = getResourceValue(nextStatement.getContext());

		if (object instanceof Literal) {
			final Literal literal = (Literal)object;
			final String value = literal.getLabel();

			String datatype = getResourceValue(literal.getDatatype());

			// In RDF-1.1, Language Literals internally have the datatype
			// rdf:langString
			if (literal.getLanguage().isPresent() && datatype == null) {
				datatype = RDF.LANGSTRING.stringValue();
			}

			// In RDF-1.1, RDF-1.0 Plain Literals are now Typed Literals with
			// type xsd:String
			if (!literal.getLanguage().isPresent() && datatype == null) {
				datatype = XMLSchema.STRING.stringValue();
			}

			result.addQuad(subject, predicate, value, datatype, literal.getLanguage().orElse(null), graphName);
		}
		else {
			result.addQuad(subject, predicate, getResourceValue((Resource)object), graphName);
		}
	}

	private String getResourceValue(Resource subject) {
		if (subject == null) {
			return null;
		}
		else if (subject instanceof IRI) {
			return subject.stringValue();
		}
		else if (subject instanceof BNode) {
			return "_:" + subject.stringValue();
		}

		throw new IllegalStateException("Did not recognise resource type: " + subject.getClass().getName());
	}

	@Override
	public RDFDataset parse(Object input)
		throws JsonLdError
	{
		final RDFDataset result = new RDFDataset();
		if (input instanceof Statement) {
			handleStatement(result, (Statement)input);
		}
		else if (input instanceof Graph) {
			if (input instanceof Model) {
				final Set<Namespace> namespaces = ((Model)input).getNamespaces();
				for (final Namespace nextNs : namespaces) {
					result.setNamespace(nextNs.getName(), nextNs.getPrefix());
				}
			}

			for (final Statement nextStatement : (Graph)input) {
				handleStatement(result, nextStatement);
			}
		}
		return result;
	}

}
