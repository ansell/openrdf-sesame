/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.webapp.navigation;

/**
 * View represents a "leaf" in the navigation model, for example a page in a
 * website.
 * 
 * @author Herko ter Horst
 */
public class View extends NavigationNodeBase {

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
