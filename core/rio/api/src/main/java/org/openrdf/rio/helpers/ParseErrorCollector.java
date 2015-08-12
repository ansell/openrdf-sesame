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
package org.openrdf.rio.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openrdf.rio.ParseErrorListener;

/**
 * A ParseErrorListener that collects Rio parse errors in the sequence they were
 * collected in.
 * 
 * @author Peter Ansell
 */
public class ParseErrorCollector implements ParseErrorListener {

	private List<String> warnings = new ArrayList<String>();

	private List<String> errors = new ArrayList<String>();

	private List<String> fatalErrors = new ArrayList<String>();

	@Override
	public void warning(String msg, long lineNo, long colNo) {
		warnings.add(msg + " (" + lineNo + ", " + colNo + ")");
	}

	@Override
	public void error(String msg, long lineNo, long colNo) {
		errors.add("[Rio error] " + msg + " (" + lineNo + ", " + colNo + ")");
	}

	@Override
	public void fatalError(String msg, long lineNo, long colNo) {
		fatalErrors.add("[Rio fatal] " + msg + " (" + lineNo + ", " + colNo + ")");
	}

	/**
	 * @return An unmodifiable list of strings representing warnings that were
	 *         received using the
	 *         {@link ParseErrorListener#warning(String, int, int)} interface.
	 */
	public List<String> getWarnings() {
		return Collections.unmodifiableList(warnings);
	}

	/**
	 * @return An unmodifiable list of strings representing potential errors
	 *         that were received using the
	 *         {@link ParseErrorListener#error(String, int, int)} interface.
	 */
	public List<String> getErrors() {
		return Collections.unmodifiableList(errors);
	}

	/**
	 * @return An unmodifiable list of strings representing fatal errors that
	 *         were received using the
	 *         {@link ParseErrorListener#fatalError(String, int, int)}
	 *         interface.
	 */
	public List<String> getFatalErrors() {
		return Collections.unmodifiableList(fatalErrors);
	}

	/**
	 * Resets the lists of warnings, errors and fatal errors.
	 */
	public void reset() {
		warnings.clear();
		errors.clear();
		fatalErrors.clear();
	}
}
