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
package info.aduna.webapp.navigation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Group represents a logical group of views in a NavigationModel.
 * 
 * @author Herko ter Horst
 * 
 */
public class Group extends AbstractNavigationNode {

	protected Map<String, Group> groups;

	protected Map<String, View> views;
	
	protected Map<String, View> viewNames;

	/**
	 * Construct a new group with the specified ID.
	 * 
	 * @param id
	 *            the ID of the group
	 */
	public Group(String id) {
		super(id);
		groups = new LinkedHashMap<String, Group>();
		views = new LinkedHashMap<String, View>();
		viewNames = new LinkedHashMap<String, View>();
	}

	/**
	 * Add a group to this group. The group becomes a sub-group of this group.
	 * 
	 * @param group
	 *            the group to add
	 */
	public void addGroup(Group group) {
		group.setParent(this);
		groups.put(group.getId(), group);
	}

	/**
	 * Get the sub-group with the specified ID.
	 * 
	 * @param id
	 *            the ID of the sub-group
	 * @return the sub-group with the specified ID, or null if this group
	 *         doesn't contain a sub-group with that ID
	 */
	public Group getGroup(String id) {
		return groups.get(id);
	}

	/**
	 * Get the list of sub-groups
	 * 
	 * @return the list of sub-groups
	 */
	public List<Group> getGroups() {
		return new ArrayList<Group>(groups.values());
	}

	/**
	 * Add a view to this group.
	 * 
	 * @param view
	 *            the view to add
	 */
	public void addView(View view) {
		view.setParent(this);
		views.put(view.getId(), view);
		viewNames.put(view.getId()+view.getViewSuffix(), view);
	}

	public View getView(String viewId) {
		return views.get(viewId);
	}

	/**
	 * Get the view with the specified name.
	 * 
	 * @param viewName
	 *            the name of the view (ID+suffix)
	 * @return the view with the specified name, or null if this group doesn't
	 *         contain a view with that name
	 */
	public View getViewByName(String viewName) {
		return viewNames.get(viewName);
	}

	protected View findViewInternal(String viewName) {
		View result = null;

		int indexOfSeparator = viewName.indexOf(getPathSeparator());
		if (indexOfSeparator > 0) {
			String groupId = viewName.substring(0, indexOfSeparator);
			Group subGroup = getGroup(groupId);
			result = subGroup.findViewInternal(viewName
					.substring(indexOfSeparator + 1));
		} else {
			result = getViewByName(viewName);
		}

		return result;
	}

	/**
	 * Get the list of views.
	 * 
	 * @return the list of views
	 */
	public List<View> getViews() {
		return new ArrayList<View>(views.values());
	}

	public Object clone() {
		Group result = new Group(getId());
		copyCommonAttributes(result);
		copyGroupsAndViews(result);
		return result;
	}

	protected void copyGroupsAndViews(Group group) {
		for (Group subGroup : getGroups()) {
			Group clonedGroup = (Group) subGroup.clone();
			group.addGroup(clonedGroup);
		}
		for (View view : getViews()) {
			View clonedView = (View) view.clone();
			group.addView(clonedView);
		}
	}
}
