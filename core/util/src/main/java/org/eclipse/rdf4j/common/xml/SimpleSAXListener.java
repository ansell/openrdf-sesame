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
 
package org.eclipse.rdf4j.common.xml;

import java.util.Map;

import org.xml.sax.SAXException;

/**
 * A listener for events reported by <tt>SimpleSAXParser</tt>.
 */
public interface SimpleSAXListener {

	/**
	 * Notifies the listener that the parser has started parsing.
	 */
	public void startDocument()
		throws SAXException;

	/**
	 * Notifies the listener that the parser has finished parsing.
	 */
	public void endDocument()
		throws SAXException;

	/**
	 * Reports a start tag to the listener. The method call reports
	 * the tag's name, the attributes that were found in the start tag
	 * and any text that was found after the start tag.
	 *
	 * @param tagName The tag name.
	 * @param atts A map containing key-value-pairs representing the
	 * attributes that were found in the start tag.
	 * @param text The text immediately following the start tag, or an
	 * empty string if the start tag was followed by a nested start
	 * tag or if no text (other than whitespace) was found between
	 * start- and end tag.
	 */
	public void startTag(String tagName, Map<String, String> atts, String text)
		throws SAXException;

	/**
	 * Reports an end tag to the listener.
	 *
	 * @param tagName The tag name.
	 */
	public void endTag(String tagName)
		throws SAXException;
}
