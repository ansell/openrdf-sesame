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

import java.util.List;
import java.util.Map.Entry;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.ParseErrorLogger;
import org.openrdf.rio.helpers.RDFParserHelper;
import org.openrdf.rio.helpers.StatementCollector;

import com.github.jsonldjava.core.JsonLdTripleCallback;
import com.github.jsonldjava.core.RDFDataset;

/**
 * A package private internal implementation class
 *
 * @author Peter Ansell
 */
class JSONLDInternalTripleCallback implements JsonLdTripleCallback {

	private ValueFactory vf;

	private RDFHandler handler;

	private ParserConfig parserConfig;

	private final ParseErrorListener parseErrorListener;

	public JSONLDInternalTripleCallback() {
		this(new StatementCollector(new LinkedHashModel()));
	}

	public JSONLDInternalTripleCallback(RDFHandler nextHandler) {
		this(nextHandler, ValueFactoryImpl.getInstance());
	}

	public JSONLDInternalTripleCallback(RDFHandler nextHandler, ValueFactory vf) {
		this(nextHandler, vf, new ParserConfig(), new ParseErrorLogger());
	}

	public JSONLDInternalTripleCallback(RDFHandler nextHandler, ValueFactory vf, ParserConfig parserConfig,
			ParseErrorListener parseErrorListener)
	{
		this.handler = nextHandler;
		this.vf = vf;
		this.parserConfig = parserConfig;
		this.parseErrorListener = parseErrorListener;
	}

	private void triple(String s, String p, String o, String graph) {
		if (s == null || p == null || o == null) {
			// TODO: i don't know what to do here!!!!
			return;
		}

		Statement result;
		// This method is always called with three Resources as subject
		// predicate and
		// object
		if (graph == null) {
			result = vf.createStatement(createResource(s), vf.createURI(p), createResource(o));
		}
		else {
			result = vf.createStatement(createResource(s), vf.createURI(p), createResource(o),
					createResource(graph));
		}

		if (handler != null) {
			try {
				handler.handleStatement(result);
			}
			catch (final RDFHandlerException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private Resource createResource(String resource) {
		// Blank node without any given identifier
		if (resource.equals("_:")) {
			return vf.createBNode();
		}
		else if (resource.startsWith("_:")) {
			return vf.createBNode(resource.substring(2));
		}
		else {
			return vf.createURI(resource);
		}
	}

	private void triple(String s, String p, String value, String datatype, String language, String graph) {

		if (s == null || p == null || value == null) {
			// TODO: i don't know what to do here!!!!
			return;
		}

		final Resource subject = createResource(s);

		final URI predicate = vf.createURI(p);
		final URI datatypeURI = datatype == null ? null : vf.createURI(datatype);

		Value object;
		try {
			object = RDFParserHelper.createLiteral(value, language, datatypeURI, getParserConfig(),
					getParserErrorListener(), getValueFactory());
		}
		catch (final RDFParseException e) {
			throw new RuntimeException(e);
		}

		Statement result;
		if (graph == null) {
			result = vf.createStatement(subject, predicate, object);
		}
		else {
			result = vf.createStatement(subject, predicate, object, createResource(graph));
		}

		if (handler != null) {
			try {
				handler.handleStatement(result);
			}
			catch (final RDFHandlerException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public ParseErrorListener getParserErrorListener() {
		return this.parseErrorListener;
	}

	/**
	 * @return the handler
	 */
	public RDFHandler getHandler() {
		return handler;
	}

	/**
	 * @param handler
	 *        the handler to set
	 */
	public void setHandler(RDFHandler handler) {
		this.handler = handler;
	}

	/**
	 * @return the parserConfig
	 */
	public ParserConfig getParserConfig() {
		return parserConfig;
	}

	/**
	 * @param parserConfig
	 *        the parserConfig to set
	 */
	public void setParserConfig(ParserConfig parserConfig) {
		this.parserConfig = parserConfig;
	}

	/**
	 * @return the vf
	 */
	public ValueFactory getValueFactory() {
		return vf;
	}

	/**
	 * @param vf
	 *        the vf to set
	 */
	public void setValueFactory(ValueFactory vf) {
		this.vf = vf;
	}

	@Override
	public Object call(final RDFDataset dataset) {
		if (handler != null) {
			try {
				handler.startRDF();
				for (final Entry<String, String> nextNamespace : dataset.getNamespaces().entrySet()) {
					handler.handleNamespace(nextNamespace.getKey(), nextNamespace.getValue());
				}
			}
			catch (final RDFHandlerException e) {
				throw new RuntimeException("Could not handle start of RDF", e);
			}
		}
		for (String graphName : dataset.keySet()) {
			final List<RDFDataset.Quad> quads = dataset.getQuads(graphName);
			if ("@default".equals(graphName)) {
				graphName = null;
			}
			for (final RDFDataset.Quad quad : quads) {
				if (quad.getObject().isLiteral()) {
					triple(quad.getSubject().getValue(), quad.getPredicate().getValue(),
							quad.getObject().getValue(), quad.getObject().getDatatype(),
							quad.getObject().getLanguage(), graphName);
				}
				else {
					triple(quad.getSubject().getValue(), quad.getPredicate().getValue(),
							quad.getObject().getValue(), graphName);
				}
			}
		}
		if (handler != null) {
			try {
				handler.endRDF();
			}
			catch (final RDFHandlerException e) {
				throw new RuntimeException("Could not handle end of RDF", e);
			}
		}

		return getHandler();
	}

}
