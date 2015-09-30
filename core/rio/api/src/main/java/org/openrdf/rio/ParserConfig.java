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

	private Set<RioSetting<?>> nonFatalErrors = new HashSet<RioSetting<?>>();

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
			nonFatalErrors.add(TriXParserSettings.FAIL_ON_TRIX_INVALID_STATEMENT);
			nonFatalErrors.add(TriXParserSettings.FAIL_ON_TRIX_MISSING_DATATYPE);
			nonFatalErrors.add(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
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
	 * backwards-compatibility settings setup in the deprecated constructor.
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
	 * Add a non-fatal error to the set used by parsers to determine whether they
	 * should attempt to recover from a particular parsing error.
	 * 
	 * @param nextNonFatalError
	 *        A non-fatal error that a parser should attempt to recover from.
	 */
	public void addNonFatalError(RioSetting<?> nextNonFatalError) {
		this.nonFatalErrors.add(nextNonFatalError);
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
		return nonFatalErrors.contains(errorToCheck);
	}

	/**
	 * Get the current set of non-fatal errors.
	 * 
	 * @return An unmodifiable set containing the current non-fatal errors.
	 */
	public Set<RioSetting<?>> getNonFatalErrors() {
		return Collections.unmodifiableSet(nonFatalErrors);
	}

	/**
	 * @deprecated All non-fatal verification errors must be specified using
	 *             {@link #addNonFatalError(RioSetting)} and checked using
	 *             {@link #isNonFatalError(RioSetting)}.
	 */
	@Deprecated
	public boolean verifyData() {
		return get(BasicParserSettings.VERIFY_RELATIVE_URIS);
	}

	/**
	 * @deprecated All non-fatal errors must be specified using
	 *             {@link #setNonFatalErrors(Set)} or
	 *             {@link #addNonFatalError(RioSetting)} and checked using
	 *             {@link #isNonFatalError(RioSetting)}.
	 */
	@Deprecated
	public boolean stopAtFirstError() {
		return getNonFatalErrors().isEmpty();
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
		throw new RuntimeException("This method is not supported anymore.");
	}

	@Override
	public void useDefaults() {
		super.useDefaults();
		this.nonFatalErrors.clear();
	}
}
