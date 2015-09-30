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
 
package info.aduna.lang;

/**
 * Generic utility methods related to objects.
 * 
 * @author Arjohn Kampman
 */
public class ObjectUtil {

	/**
	 * Compares two objects or null references.
	 * 
	 * @param o1
	 *        The first object.
	 * @param o2
	 *        The second object
	 * @return <tt>true</tt> if both objects are <tt>null</tt>, if the
	 *         object references are identical, or if the objects are equal
	 *         according to the {@link Object#equals} method of the first object;
	 *         <tt>false</tt> in all other situations.
	 */
	public static boolean nullEquals(Object o1, Object o2) {
		return o1 == o2 || o1 != null && o2 != null && o1.equals(o2);
	}

	/**
	 * Returns the hash code of the supplied object, or <tt>0</tt> if a null
	 * reference is supplied.
	 * 
	 * @param o
	 *        An object or null reference.
	 * @return The object's hash code, or <tt>0</tt> if the parameter is
	 *         <tt>null</tt>.
	 */
	public static int nullHashCode(Object o) {
		return o == null ? 0 : o.hashCode();
	}
}
