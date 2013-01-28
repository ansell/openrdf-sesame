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
