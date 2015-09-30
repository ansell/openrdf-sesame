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
package org.openrdf.rio.rdfxml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A collection of XML attributes.
 */
class Atts {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * List containing Att objects.
	 */
	private List<Att> attributes;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new <tt>Atts</tt> object.
	 */
	public Atts() {
		this(4);
	}

	/**
	 * Creates a new <tt>Atts</tt> object.
	 * 
	 * @param size
	 *        The initial size of the array for storing attributes.
	 */
	public Atts(int size) {
		attributes = new ArrayList<Att>(size);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Adds an attribute.
	 */
	public void addAtt(Att att) {
		attributes.add(att);
	}

	/**
	 * Get an iterator on the attributes.
	 * 
	 * @return an Iterator over Att objects.
	 */
	public Iterator<Att> iterator() {
		return attributes.iterator();
	}

	/**
	 * Gets the attribute with the specified QName.
	 * 
	 * @param qName
	 *        The QName of an attribute.
	 * @return The attribute with the specified QName, or <tt>null</tt> if no
	 *         such attribute could be found.
	 */
	public Att getAtt(String qName) {
		for (int i = 0; i < attributes.size(); i++) {
			Att att = attributes.get(i);

			if (att.getQName().equals(qName)) {
				return att;
			}
		}

		return null;
	}

	/**
	 * Gets the attribute with the specified namespace and local name.
	 * 
	 * @param namespace
	 *        The namespace of an attribute.
	 * @param localName
	 *        The local name of an attribute.
	 * @return The attribute with the specified namespace and local name, or
	 *         <tt>null</tt> if no such attribute could be found.
	 */
	public Att getAtt(String namespace, String localName) {
		for (int i = 0; i < attributes.size(); i++) {
			Att att = attributes.get(i);

			if (att.getLocalName().equals(localName) && att.getNamespace().equals(namespace)) {
				return att;
			}
		}

		return null;
	}

	/**
	 * Removes the attribute with the specified QName and returns it.
	 * 
	 * @param qName
	 *        The QName of an attribute.
	 * @return The removed attribute, or <tt>null</tt> if no attribute with the
	 *         specified QName could be found.
	 */
	public Att removeAtt(String qName) {
		for (int i = 0; i < attributes.size(); i++) {
			Att att = attributes.get(i);

			if (att.getQName().equals(qName)) {
				attributes.remove(i);
				return att;
			}
		}

		return null;
	}

	/**
	 * Removes the attribute with the specified namespace and local name and
	 * returns it.
	 * 
	 * @param namespace
	 *        The namespace of an attribute.
	 * @param localName
	 *        The local name of an attribute.
	 * @return The removed attribute, or <tt>null</tt> if no attribute with the
	 *         specified namespace and local name could be found.
	 */
	public Att removeAtt(String namespace, String localName) {
		for (int i = 0; i < attributes.size(); i++) {
			Att att = attributes.get(i);

			if (att.getLocalName().equals(localName) && att.getNamespace().equals(namespace)) {
				attributes.remove(i);
				return att;
			}
		}

		return null;
	}

	/**
	 * Returns the number of attributes contained in this object.
	 */
	public int size() {
		return attributes.size();
	}

	/**
	 * Produces a String-representation of this object.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Atts[");
		for (int i = 0; i < attributes.size(); i++) {
			Att att = attributes.get(i);
			sb.append(att.getQName());
			sb.append("=");
			sb.append(att.getValue());
			sb.append("; ");
		}
		sb.append("]");
		return sb.toString();
	}
}
