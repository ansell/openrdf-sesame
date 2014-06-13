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
package org.openrdf.rio.jsonld;

import java.util.Optional;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
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
			final Optional<String> language = literal.getLanguage();

			String datatype = getResourceValue(literal.getDatatype());

			// In RDF-1.1, Language Literals internally have the datatype
			// rdf:langString
			if (language.isPresent() && datatype == null) {
				datatype = RDF.LANGSTRING.stringValue();
			}

			// In RDF-1.1, RDF-1.0 Plain Literals are now Typed Literals with
			// type xsd:String
			if (!language.isPresent() && datatype == null) {
				datatype = XMLSchema.STRING.stringValue();
			}

			if (language.isPresent()) {
				result.addQuad(subject, predicate, value, datatype, language.get(), graphName);
			}
			else {
				result.addQuad(subject, predicate, value, datatype, null, graphName);
			}
		}
		else {
			result.addQuad(subject, predicate, getResourceValue((Resource)object), graphName);
		}
	}

	private String getResourceValue(Resource subject) {
		if (subject == null) {
			return null;
		}
		else if (subject instanceof URI) {
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
