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
 * An implementation of <tt>SimpleSAXListener</tt> providing dummy
 * implementations for all its methods.
 */
public class SimpleSAXAdapter implements SimpleSAXListener {

	// implements SimpleSAXListener.startDocument()
	public void startDocument()
		throws SAXException
	{
	}

	// implements SimpleSAXListener.endDocument()
	public void endDocument()
		throws SAXException
	{
	}

	// implements SimpleSAXListener.startTag()
	public void startTag(String tagName, Map<String, String> atts, String text)
		throws SAXException
	{
	}

	// implements SimpleSAXListener.endTag()
	public void endTag(String tagName)
		throws SAXException
	{
	}
}
