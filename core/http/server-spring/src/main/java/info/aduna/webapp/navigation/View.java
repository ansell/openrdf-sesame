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
package info.aduna.webapp.navigation;

/**
 * View represents a "leaf" in the navigation model, for example a page in a
 * website.
 * 
 * @author Herko ter Horst
 */
public class View extends AbstractNavigationNode {

	/**
	 * Construct a new view with the specified ID.
	 * 
	 * @param id
	 *            the ID of the view
	 */
	public View(String id) {
		super(id);
	}
	
	public String getPath() {
		if(path == null) {
			StringBuilder result = new StringBuilder();
			result.append(super.getPath());
			result.append(getViewSuffix());
			setPath(result.toString());
		}
		return path;
	}
	
	public Object clone() {
		View result = new View(getId());
		copyCommonAttributes(result);
		return result;
	}
}
