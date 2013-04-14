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
package org.openrdf.rio;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.rio.RDFParser.DatatypeHandling;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.NTriplesParserSettings;
import org.openrdf.rio.helpers.RDFaParserSettings;
import org.openrdf.rio.helpers.TriXParserSettings;

/**
 * A container object for easy setting and passing of {@link RDFParser}
 * configuration options.
 * 
 * @author Jeen Broekstra
 * @author Peter Ansell
 */
public class ParserConfig extends RioConfig implements Serializable {

	/**
	 * @since 2.7.0
	 */
	private static final long serialVersionUID = 270L;

	private Set<RioSetting<?>> nonFatalErrors = Collections.emptySet();

	/**
	 * Creates a ParserConfig object starting with default settings.
	 */
	public ParserConfig() {
		super();
	}

	/**
	 * Creates a ParserConfig object with the supplied config settings.
	 * 
	 * @deprecated Use {@link ParserConfig#ParserConfig()} instead and set
	 *             preserveBNodeIDs using {@link #set(RioSetting, Object)} with
	 *             {@link BasicParserSettings#PRESERVE_BNODE_IDS}.
	 *             <p>
	 *             The other parameters are all deprecated and this constructor
	 *             may be removed in a future release.
	 *             <p>
	 *             This constructor calls #setNonFatalErrors using a best-effort
	 *             algorithm that may not match the exact semantics of the
	 *             pre-2.7 constructor.
	 */
	@Deprecated
	public ParserConfig(boolean verifyData, boolean stopAtFirstError, boolean preserveBNodeIDs,
			DatatypeHandling datatypeHandling)
	{
		this();

		this.set(BasicParserSettings.PRESERVE_BNODE_IDS, preserveBNodeIDs);

		// If they wanted to stop at the first error, then all optional errors are
		// fatal, which is the default.
		// We only attempt to map the parameters for cases where they wanted the
		// parser to attempt to recover.
		if (!stopAtFirstError) {
			Set<RioSetting<?>> nonFatalErrors = new HashSet<RioSetting<?>>();
			nonFatalErrors.add(TriXParserSettings.IGNORE_TRIX_INVALID_STATEMENT);
			nonFatalErrors.add(TriXParserSettings.IGNORE_TRIX_MISSING_DATATYPE);
			nonFatalErrors.add(NTriplesParserSettings.IGNORE_NTRIPLES_INVALID_LINES);
			nonFatalErrors.add(RDFaParserSettings.ALLOW_RDFA_UNDEFINED_PREFIXES);
			if (verifyData) {
				nonFatalErrors.add(BasicParserSettings.VERIFY_RELATIVE_URIS);
				if (datatypeHandling == DatatypeHandling.IGNORE) {
					nonFatalErrors.add(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
					nonFatalErrors.add(BasicParserSettings.VERIFY_DATATYPE_VALUES);
					nonFatalErrors.add(BasicParserSettings.NORMALIZE_DATATYPE_VALUES);
				}
				else if (datatypeHandling == DatatypeHandling.VERIFY) {
					nonFatalErrors.add(BasicParserSettings.NORMALIZE_DATATYPE_VALUES);
				}
				else {
					// For DatatypeHandling.NORMALIZE, all three datatype settings
					// are fatal.
				}
			}
			setNonFatalErrors(nonFatalErrors);
		}
	}

	/**
	 * This method indicates a list of optional errors that the parser should
	 * attempt to recover from.
	 * <p>
	 * If recovery is not possible, then the parser will still abort with an
	 * exception.
	 * <p>
	 * Calls to this method will override previous calls, including the
	 * backwards-compatibility settings setup in the deprecated
	 * {@link ParserConfig#ParserConfig(boolean, boolean, boolean, DatatypeHandling)}
	 * constructor.
	 * <p>
	 * Non-Fatal errors that are detected MUST be reported to the error listener.
	 * 
	 * @param nonFatalErrors
	 *        The set of parser errors that are relevant to
	 * @since 2.7.0
	 */
	public void setNonFatalErrors(Set<RioSetting<?>> nonFatalErrors) {
		this.nonFatalErrors = new HashSet<RioSetting<?>>(nonFatalErrors);
	}

	/**
	 * This method is used by the parser to check whether they should throw an
	 * exception or attempt to recover from a non-fatal error.
	 * <p>
	 * If this method returns false, then the given non-fatal error will cause
	 * the parser to throw an exception.
	 * <p>
	 * If this method returns true, then the parser will do its best to recover
	 * from the error, potentially by dropping triples or creating triples that
	 * do not exactly match the source.
	 * <p>
	 * By default this method will always return false until
	 * {@link #setNonFatalErrors(Set)} is called to specify the set of errors
	 * that are non-fatal in the given context.
	 * <p>
	 * Non-Fatal errors that are detected MUST be reported to the error listener.
	 * 
	 * @param errorToCheck
	 * @return True if the user has setup the parser configuration to indicate
	 *         that this error is not necessarily fatal and false if the user has
	 *         not indicated that this error is fatal.
	 * @since 2.7.0
	 */
	public boolean isNonFatalError(RioSetting<?> errorToCheck) {
		return this.nonFatalErrors.contains(errorToCheck);
	}

	/**
	 * @deprecated All non-fatal errors must be specified using
	 *             {@link #setNonFatalErrors(Set)} and checked using
	 *             {@link #isNonFatalError(RioSetting)}.
	 */
	@Deprecated
	public boolean verifyData() {
		return true;
	}

	/**
	 * @deprecated All non-fatal errors must be specified using
	 *             {@link #setNonFatalErrors(Set)} and checked using
	 *             {@link #isNonFatalError(RioSetting)}.
	 */
	@Deprecated
	public boolean stopAtFirstError() {
		return true;
	}

	/**
	 * This method is preserved for backwards compatibility.
	 * <p>
	 * Code should be gradually migrated to use
	 * {@link BasicParserSettings#PRESERVE_BNODE_IDS}.
	 * 
	 * @return Returns the {@link BasicParserSettings#PRESERVE_BNODE_IDS}
	 *         setting.
	 */
	public boolean isPreserveBNodeIDs() {
		return this.get(BasicParserSettings.PRESERVE_BNODE_IDS);
	}

	/**
	 * @deprecated Datatype handling is now split across
	 *             {@link BasicParserSettings#VERIFY_DATATYPE_VALUES},
	 *             {@link BasicParserSettings#FAIL_ON_UNKNOWN_DATATYPES} and
	 *             {@link BasicParserSettings#NORMALIZE_DATATYPE_VALUES}.
	 *             <p>
	 *             This method will be removed in a future release.
	 */
	@Deprecated
	public DatatypeHandling datatypeHandling() {
		throw new RuntimeException("This method is not used anymore");
	}
}
