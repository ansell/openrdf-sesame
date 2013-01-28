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
 
package info.aduna.xml;

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
