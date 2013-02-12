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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import info.aduna.io.IOUtil;
import info.aduna.io.UncloseableInputStream;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.impl.BindingImpl;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.QueryResultParserBase;

/**
 * Abstract base class for SPARQL Results JSON Parsers.
 * 
 * @author Peter Ansell
 */
public abstract class SPARQLJSONParserBase extends QueryResultParserBase {

	public static final String HEAD = "head";

	public static final String LINK = "link";

	public static final String VARS = "vars";

	public static final String BOOLEAN = "boolean";

	public static final String RESULTS = "results";

	public static final String BINDINGS = "bindings";

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
		BufferedInputStream buff = new BufferedInputStream(in);

		try {
			String json = IOUtil.readString(in);

			boolean result = false;

			// "This object has a "head" member and either a "results" member or a "boolean" member, depending on the query form"
			// - http://www.w3.org/TR/sparql11-results-json/#json-result-object
			JSONObject jsonObject = new JSONObject(json);

			if (!jsonObject.has(HEAD)) {
				throw new QueryResultParseException("Did not find head");
			}

			JSONObject head = jsonObject.getJSONObject(HEAD);

			// Both Boolean and Tuple results can have headers with link elements.
			if (head.has(LINK)) {
				// FIXME: Extend QueryResultHandler interface to support link's
			}

			// check if we are handling a boolean first
			if (jsonObject.has(BOOLEAN)) {
				result = jsonObject.getBoolean(BOOLEAN);

				handler.handleBoolean(result);
			}
			// we must be handling tuple solutions if it was not a boolean
			else {
				List<String> varsList = new ArrayList<String>();

				if (!head.has(VARS)) {
					throw new QueryResultParseException("Head object did not contain vars");
				}

				JSONArray vars = head.getJSONArray(VARS);

				if (vars.length() == 0) {
					throw new QueryResultParseException("Vars array was empty");
				}

				for (int i = 0; i < vars.length(); i++) {
					varsList.add(vars.getString(i));
				}

				handler.startQueryResult(varsList);

				if (!jsonObject.has(RESULTS)) {
					throw new QueryResultParseException("Did not find results");
				}

				JSONObject resultsObject = jsonObject.getJSONObject(RESULTS);

				if (!resultsObject.has(BINDINGS)) {
					throw new QueryResultParseException("Results object did not contain a bindings object");
				}

				JSONArray bindings = resultsObject.getJSONArray(BINDINGS);

				for (int i = 0; i < bindings.length(); i++) {

					JSONObject nextBindingObject = bindings.getJSONObject(i);

					MapBindingSet nextBindingSet = new MapBindingSet();

					for (String nextVar : varsList) {

						JSONObject nextVarBinding = nextBindingObject.getJSONObject(nextVar);

						Value nextValue = null;

						nextBindingSet.addBinding(new BindingImpl(nextVar, nextValue));
					}

					handler.handleSolution(nextBindingSet);
				}
			}

			return result;
		}
		catch (JSONException e) {
			throw new QueryResultParseException("Failed to parse JSON object", e);
		}
		finally {
			in.close();
		}
	}

}