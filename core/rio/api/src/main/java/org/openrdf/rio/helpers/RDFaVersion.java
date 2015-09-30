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
package org.openrdf.rio.helpers;

/**
 * Enumeration for tracking versions of the RDFa specification to specify
 * processing capabilities of RDFa modules.
 * 
 * @author Peter Ansell
 */
public enum RDFaVersion {

	/**
	 * The initial RDFa 1.0 version (2008)
	 * 
	 * @see <a href="http://www.w3.org/TR/2008/REC-rdfa-syntax-20081014/">RDFa in
	 *      XHTML: Syntax and Processing</a>
	 */
	RDFA_1_0("RDFa 1.0", "http://www.w3.org/TR/2008/REC-rdfa-syntax-20081014/"),

	/**
	 * The modified RDFa 1.1 version (2012)
	 * 
	 * @see <a href="http://www.w3.org/TR/2012/REC-rdfa-core-20120607/">RDFa Core
	 *      1.1</a>
	 */
	RDFA_1_1("RDFa 1.1", "http://www.w3.org/TR/2012/REC-rdfa-core-20120607/"),

	;

	private final String label;

	private final String reference;

	RDFaVersion(String nextLabel, String nextRef) {
		label = nextLabel;
		reference = nextRef;
	}

	/**
	 * @return Returns the reference URL for the given version.
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}
}
