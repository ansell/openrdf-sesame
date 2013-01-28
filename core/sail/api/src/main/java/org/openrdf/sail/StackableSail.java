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
package org.openrdf.sail;

/**
 * An interface for Sails that can be stacked on top of other Sails.
 */
public interface StackableSail extends Sail {

	/**
	 * Sets the base Sail that this Sail will work on top of. This method
	 * will be called before the initialize() method is called.
	 */
	public void setBaseSail(Sail baseSail);

	/**
	 * Gets the base Sail that this Sail works on top of.
	 */
	public Sail getBaseSail();
}
