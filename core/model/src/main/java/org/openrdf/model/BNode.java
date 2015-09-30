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
package org.openrdf.model;

/**
 * A blank node (aka <em>bnode</em>, aka <em>anonymous node</em>). A blank node
 * has an identifier to be able to compare it to other blank nodes internally.
 * Please note that, conceptually, blank node equality can only be determined by
 * examining the statements that refer to them.
 */
public interface BNode extends Resource {

	/**
	 * retrieves this blank node's identifier.
	 *
	 * @return A blank node identifier.
	 */
	public String getID();
	
	/**
	 * Compares a blank node object to another object.
	 *
	 * @param o The object to compare this blank node to.
	 * @return <tt>true</tt> if the other object is an instance of {@link BNode}
	 * and their IDs are equal, <tt>false</tt> otherwise.
	 */
	public boolean equals(Object o);
	
	/**
	 * The hash code of a blank node is defined as the hash code of its
	 * identifier: <tt>id.hashCode()</tt>.
	 * 
	 * @return A hash code for the blank node.
	 */
	public int hashCode();
}
