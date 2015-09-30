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
package org.eclipse.rdf4j.common.webapp.navigation;

/**
 * Base implementation of the NavigationNode interface.
 * 
 * @author Herko ter Horst
 */
public abstract class AbstractNavigationNode implements NavigationNode {

	private String id;

	private boolean hidden;

	private boolean enabled;

	private NavigationNode parent;

	protected String path;

	protected String icon;

	protected String i18n;

	protected String viewSuffix;

	public AbstractNavigationNode(String id) {
		setId(id);
		setEnabled(true);
	}

	public String getId() {
		return id;
	}

	void setId(String id) {
		this.id = id;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public NavigationNode getParent() {
		return parent;
	}

	public void setParent(NavigationNode parent) {
		this.parent = parent;
	}

	public boolean isParent(NavigationNode node) {
		boolean result = false;

		if (node != null && node != this) {
			if (node.getParent() == this) {
				result = true;
			} else if (node.getParent() != null) {
				result = isParent(node.getParent());
			}
		}

		return result;
	}

	public String getPathPrefix() {
		StringBuilder result = new StringBuilder();
		if (getParent() != null) {
			if (getParent().getPathPrefix() != null) {
				result.append(getParent().getPathPrefix());
			}
			if (getParent().getId().length() > 0) {
				result.append(getParent().getId());
				result.append(getPathSeparator());
			}
		}
		return result.toString();
	}

	public String getPathSeparator() {
		String result = null;
		if (getParent() != null) {
			result = getParent().getPathSeparator();
		}
		return result;
	}

	public String getPath() {
		if (path == null) {
			StringBuilder result = new StringBuilder();
			result.append(getPathPrefix());
			result.append(getId());
			setPath(result.toString());
		}
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getIconPrefix() {
		StringBuilder result = new StringBuilder();
		if (getParent() != null) {
			if (getParent().getIconPrefix() != null) {
				result.append(getParent().getIconPrefix());
			}
			if (getParent().getId().length() > 0) {
				result.append(getParent().getId());
				result.append(getIconSeparator());
			}
		}
		return result.toString();
	}

	public String getIconSeparator() {
		String result = null;
		if (getParent() != null) {
			result = getParent().getIconSeparator();
		}
		return result;
	}

	public String getIconSuffix() {
		String result = null;
		if (getParent() != null) {
			result = getParent().getIconSuffix();
		}
		return result;
	}

	public String getIcon() {
		if (icon == null) {
			StringBuilder result = new StringBuilder();
			result.append(getIconPrefix());
			result.append(getId());
			result.append(getIconSuffix());
			setIcon(result.toString());
		}

		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getI18nPrefix() {
		StringBuilder result = new StringBuilder();
		if (getParent() != null) {
			if (getParent().getI18nPrefix() != null) {
				result.append(getParent().getI18nPrefix());
			}
			if (getParent().getId().length() > 0) {
				result.append(getParent().getId());
				result.append(getI18nSeparator());
			}
		}
		return result.toString();
	}

	public String getI18nSeparator() {
		String result = null;
		if (getParent() != null) {
			result = getParent().getI18nSeparator();
		}
		return result;
	}

	public String getI18nSuffix() {
		String result = null;
		if (getParent() != null) {
			result = getParent().getI18nSuffix();
		}
		return result;
	}

	public String getI18n() {
		if (i18n == null) {
			StringBuilder result = new StringBuilder();
			result.append(getI18nPrefix());
			result.append(getId());
			result.append(getI18nSuffix());
			setI18n(result.toString());
		}
		return i18n;
	}

	public void setI18n(String i18n) {
		this.i18n = i18n;
	}

	public String getViewSuffix() {
		if (viewSuffix == null) {
			if (getParent() != null) {
				setViewSuffix(getParent().getViewSuffix());
			}
		}
		return viewSuffix;
	}

	public void setViewSuffix(String viewSuffix) {
		this.viewSuffix = viewSuffix;
	}

	public int getDepth() {
		int result = 0;

		if (getParent() != null) {
			result = getParent().getDepth() + 1;
		}

		return result;
	}

	public boolean equals(Object other) {
		boolean result = this == other;
		if (!result && other instanceof NavigationNode
				&& getClass().equals(other.getClass())) {
			NavigationNode otherNode = (NavigationNode) other;
			result = getId().equals(otherNode.getId());
			if (result
					&& !(getParent() == null && otherNode.getParent() == null)) {
				if (getParent() != null && otherNode.getParent() != null) {
					result = getParent().equals(otherNode.getParent());
				} else {
					result = false;
				}
			}
		}
		return result;
	}

	public int hashCode() {
		int result = getId().hashCode();
		if (getParent() != null) {
			result += 31 * getParent().hashCode();
		}
		return result;
	}

	protected void copyCommonAttributes(NavigationNode node) {
		node.setEnabled(isEnabled());
		node.setHidden(isHidden());
		node.setI18n(getI18n());
		node.setIcon(getIcon());
		node.setPath(getPath());
		node.setViewSuffix(getViewSuffix());
	}
}
