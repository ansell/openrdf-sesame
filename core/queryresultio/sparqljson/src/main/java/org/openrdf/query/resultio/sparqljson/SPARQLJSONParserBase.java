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
package org.openrdf.query.resultio.sparqljson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.w3c.dom.stylesheets.LinkStyle;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.impl.BindingImpl;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.QueryResultParserBase;
import org.openrdf.rio.RDFParseException;

/**
 * Abstract base class for SPARQL Results JSON Parsers. Provides a common
 * implementation of both boolean and tuple parsing.
 * 
 * @author Peter Ansell
 */
public abstract class SPARQLJSONParserBase extends QueryResultParserBase {

	private static final JsonFactory JSON_FACTORY = new JsonFactory();

	static {
		// Disable features that may work for most JSON where the field names are
		// in limited supply,
		// but does not work for RDF/JSON where a wide range of URIs are used for
		// subjects and
		// predicates
		JSON_FACTORY.disable(JsonFactory.Feature.INTERN_FIELD_NAMES);
		JSON_FACTORY.disable(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES);
		JSON_FACTORY.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
	}

	public static final String HEAD = "head";

	public static final String LINK = "link";

	public static final String VARS = "vars";

	public static final String BOOLEAN = "boolean";

	public static final String RESULTS = "results";

	public static final String BINDINGS = "bindings";

	public static final String TYPE = "type";

	public static final String VALUE = "value";

	public static final String XMLLANG = "xml:lang";

	public static final String DATATYPE = "datatype";

	public static final String LITERAL = "literal";

	public static final String TYPED_LITERAL = "typed-literal";

	public static final String BNODE = "bnode";

	public static final String URI = "uri";

	/**
	 * 
	 */
	public SPARQLJSONParserBase() {
		super();
	}

	/**
	 * 
	 */
	public SPARQLJSONParserBase(ValueFactory valueFactory) {
		super(valueFactory);
	}

	@Override
	public void parseQueryResult(InputStream in)
		throws IOException, QueryResultParseException, QueryResultHandlerException
	{
		parseQueryResultInternal(in);
	}

	protected boolean parseQueryResultInternal(InputStream in)
		throws IOException, QueryResultParseException, QueryResultHandlerException
	{
		JsonParser jp = JSON_FACTORY.createJsonParser(in);
		boolean result = false;

		if (jp.nextToken() != JsonToken.START_OBJECT) {
			throw new QueryResultParseException("Expected SPARQL Results JSON document to start with an Object",
					jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());
		}

		List<String> varsList = new ArrayList<String>();
		boolean varsFound = false;

		while (jp.nextToken() != JsonToken.END_OBJECT) {

			final String subjStr = jp.getCurrentName();

			// try {
			// String json = IOUtil.readString(in);

			// "This object has a "head" member and either a "results" member or a "boolean" member, depending on the query form"
			// - http://www.w3.org/TR/sparql11-results-json/#json-result-object
			// JSONObject jsonObject = new JSONObject(json);

			// if (!jsonObject.has(HEAD)) {
			// throw new QueryResultParseException("Did not find head");
			// }
			// Both head and results should be objects
			if (subjStr.equals(HEAD)) {
				if (jp.nextToken() != JsonToken.START_OBJECT) {
					throw new QueryResultParseException("Did not find object under " + subjStr + " field",
							jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());
				}

				while (jp.nextToken() != JsonToken.END_OBJECT) {
					final String headStr = jp.getCurrentName();

					if (headStr.equals(VARS)) {
						if (jp.nextToken() != JsonToken.START_ARRAY) {
							throw new QueryResultParseException("Expected variable labels to be an array",
									jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());
						}

						while (jp.nextToken() != JsonToken.END_ARRAY) {
							varsList.add(jp.getCurrentName());
						}

						if (this.handler != null) {
							handler.startQueryResult(varsList);
						}

						varsFound = true;
					}
					else if (headStr.equals(LINK)) {
						List<String> linksList = new ArrayList<String>();
						if (jp.nextToken() != JsonToken.START_ARRAY) {
							throw new QueryResultParseException("Expected links to be an array",
									jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());
						}

						while (jp.nextToken() != JsonToken.END_ARRAY) {
							linksList.add(jp.getCurrentName());
						}

						if (this.handler != null) {
							handler.handleLinks(linksList);
						}

					}
					else {
						throw new QueryResultParseException("Found unexpected object in head field: " + headStr,
								jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());
					}
				}
			}
			else if (subjStr.equals(RESULTS)) {
				if (jp.nextToken() != JsonToken.START_OBJECT) {
					throw new QueryResultParseException("Found unexpected token in results object: "
							+ jp.getCurrentName(), jp.getCurrentLocation().getLineNr(),
							jp.getCurrentLocation().getColumnNr());
				}

				if (jp.nextToken() != JsonToken.FIELD_NAME) {
					throw new QueryResultParseException("Found unexpected token in results object: "
							+ jp.getCurrentName(), jp.getCurrentLocation().getLineNr(),
							jp.getCurrentLocation().getColumnNr());
				}

				if (jp.getCurrentName().equals(BINDINGS)) {
					if (jp.nextToken() != JsonToken.START_ARRAY) {
						throw new QueryResultParseException("Found unexpected token in bindings object: "
								+ jp.getCurrentName(), jp.getCurrentLocation().getLineNr(),
								jp.getCurrentLocation().getColumnNr());
					}

					MapBindingSet nextBindingSet = new MapBindingSet();

					while (jp.nextToken() != JsonToken.END_ARRAY) {
						// TODO: Parse each binding

						if (!varsFound) {
							// TODO: Buffer the bindings to fit with the
							// QueryResultHandler contract so that startQueryResults is
							// always called before handleSolution
						}

						if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
							throw new QueryResultParseException("Did not find object in bindings array: "
									+ jp.getCurrentName(), jp.getCurrentLocation().getLineNr(),
									jp.getCurrentLocation().getColumnNr());
						}

						while (jp.nextToken() != JsonToken.END_OBJECT) {

							if (jp.getCurrentToken() != JsonToken.FIELD_NAME) {
								throw new QueryResultParseException("Did not find binding name",
										jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());
							}

							final String bindingStr = jp.getCurrentName();

							if (jp.nextToken() != JsonToken.START_OBJECT) {
								throw new QueryResultParseException("Did not find object for binding value",
										jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());
							}

							String lang = null;
							String type = null;
							String datatype = null;
							String value = null;

							while (jp.nextToken() != JsonToken.END_OBJECT) {

								if (jp.getCurrentToken() != JsonToken.FIELD_NAME) {
									throw new QueryResultParseException("Did not find value attribute under "
											+ bindingStr + " field", jp.getCurrentLocation().getLineNr(),
											jp.getCurrentLocation().getColumnNr());
								}
								String fieldName = jp.getCurrentName();
								
								// move to the value token
								jp.nextToken();

								// set the appropriate state variable
								if (TYPE.equals(fieldName)) {
									type = jp.getText();
								}
								else if (XMLLANG.equals(fieldName)) {
									lang = jp.getText();
								}
								else if (DATATYPE.equals(fieldName)) {
									datatype = jp.getText();
								}
								else if (VALUE.equals(fieldName)) {
									value = jp.getText();
								}
								else {
									throw new QueryResultParseException("Unexpected field name: " + fieldName,
											jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());

								}
							}

							nextBindingSet.addBinding(bindingStr, parseValue(type, value, lang, datatype));
						}
						// parsing of solution finished, report result return to
						// bindings state

						if (handler != null) {
							handler.handleSolution(nextBindingSet);
						}
					}

				}
				else {
					throw new QueryResultParseException("Found unexpected field in results: "
							+ jp.getCurrentName(), jp.getCurrentLocation().getLineNr(),
							jp.getCurrentLocation().getColumnNr());
				}
			}
			else if (subjStr.equals(BOOLEAN)) {
				JsonToken nextToken = jp.nextToken();

				result = Boolean.parseBoolean(jp.getText());
				handler.handleBoolean(result);
			}
			else {
				throw new QueryResultParseException("Found unexpected object in top level " + subjStr + " field",
						jp.getCurrentLocation().getLineNr(), jp.getCurrentLocation().getColumnNr());
			}
		}

		return result;
		// JSONObject head = jsonObject.getJSONObject(HEAD);

		// Both Boolean and Tuple results can have headers with link elements.
		// if (head.has(LINK)) {
		// FIXME: Extend QueryResultHandler interface to support link's
		// }

		// check if we are handling a boolean first
		// if (jsonObject.has(BOOLEAN)) {
		// result = jsonObject.getBoolean(BOOLEAN);
		//
		// if (this.handler != null) {
		// handler.handleBoolean(result);
		// }
		// }
		// // we must be handling tuple solutions if it was not a boolean
		// else {
		// List<String> varsList = new ArrayList<String>();
		//
		// if (!head.has(VARS)) {
		// throw new
		// QueryResultParseException("Head object did not contain vars");
		// }
		//
		// JSONArray vars = head.getJSONArray(VARS);
		//
		// if (vars.length() == 0) {
		// throw new QueryResultParseException("Vars array was empty");
		// }
		//
		// for (int i = 0; i < vars.length(); i++) {
		// varsList.add(vars.getString(i));
		// }
		//
		// if (this.handler != null) {
		// handler.startQueryResult(varsList);
		// }
		//
		// if (!jsonObject.has(RESULTS)) {
		// throw new QueryResultParseException("Did not find results");
		// }
		//
		// JSONObject resultsObject = jsonObject.getJSONObject(RESULTS);
		//
		// if (!resultsObject.has(BINDINGS)) {
		// throw new
		// QueryResultParseException("Results object did not contain a bindings object");
		// }
		//
		// JSONArray bindings = resultsObject.getJSONArray(BINDINGS);
		//
		// for (int i = 0; i < bindings.length(); i++) {
		//
		// JSONObject nextBindingObject = bindings.getJSONObject(i);
		//
		// MapBindingSet nextBindingSet = new MapBindingSet();
		//
		// for (String nextVar : varsList) {
		// if (nextBindingObject.has(nextVar)) {
		// JSONObject nextVarBinding = nextBindingObject.getJSONObject(nextVar);
		//
		// if (!nextVarBinding.has(TYPE)) {
		// throw new QueryResultParseException("Binding did not contain a type: "
		// + nextVar);
		// }
		//
		// String type = nextVarBinding.getString(TYPE);
		//
		// if (!nextVarBinding.has(VALUE)) {
		// throw new QueryResultParseException("Binding did not contain a value: "
		// + nextVar);
		// }
		//
		// String value = nextVarBinding.getString(VALUE);
		//
		// String language = null;
		// String datatype = null;
		//
		// if (type.equals(LITERAL)) {
		// // only check this if the type is literal
		// if (nextVarBinding.has(XMLLANG)) {
		// language = nextVarBinding.getString(XMLLANG);
		// }
		// }
		//
		// // provide some backwards compatibility with 2007 SPARQL
		// // Query Results in JSON W3C Working Group Note by
		// // supporting typed-literal here as well as literal
		// // http://www.w3.org/TR/2007/NOTE-rdf-sparql-json-res-20070618/
		// if (type.equals(LITERAL) || type.equals(TYPED_LITERAL)) {
		// if (nextVarBinding.has(DATATYPE)) {
		// datatype = nextVarBinding.getString(DATATYPE);
		// }
		// }
		//
		// Value nextValue = parseValue(type, value, language, datatype);
		//
		// nextBindingSet.addBinding(new BindingImpl(nextVar, nextValue));
		// }
		// }
		//
		// if (nextBindingSet.size() == 0) {
		// throw new
		// QueryResultParseException("Binding did not contain any variables");
		// }
		//
		// if (this.handler != null) {
		// handler.handleSolution(nextBindingSet);
		// }
		// }
		// if (this.handler != null) {
		// handler.endQueryResult();
		// }
		// }
		//
		// return result;
		// }
		// catch (JSONException e) {
		// throw new QueryResultParseException("Failed to parse JSON object", e);
		// }
		// finally {
		// in.close();
		// }
	}

	/**
	 * Parse a value out of the elements for a binding.
	 * 
	 * @param type
	 *        {@link #LITERAL}, {@link #TYPED_LITERAL}, {@link #BNODE} or
	 *        {@link #URI}
	 * @param value
	 *        actual value text
	 * @param language
	 *        language tag, if applicable
	 * @param datatype
	 *        datatype tag, if applicable
	 * @return the value corresponding to the given parameters
	 */
	private Value parseValue(String type, String value, String language, String datatype) {
		Value result = null;

		if (type.equals(LITERAL) || type.equals(TYPED_LITERAL)) {
			if (language != null) {
				result = valueFactory.createLiteral(value, language);
			}
			else if (datatype != null) {
				result = valueFactory.createLiteral(value, valueFactory.createURI(datatype));
			}
			else {
				result = valueFactory.createLiteral(value);
			}
		}
		else if (type.equals(BNODE)) {
			result = valueFactory.createBNode(value);
		}
		else if (type.equals(URI)) {
			result = valueFactory.createURI(value);
		}

		return result;
	}
}