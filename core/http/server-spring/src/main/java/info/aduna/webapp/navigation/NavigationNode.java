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

/**
 * NavigationNode represents a node in a NavigationModel.
 * 
 * @author Herko ter Horst
 */
public interface NavigationNode extends Cloneable {

	/**
	 * Get the ID of the node.
	 * 
	 * @return the ID of the node
	 */
	public String getId();

	/**
	 * Is the node hidden?
	 * 
	 * @return true if the node is hidden, false otherwise
	 */
	public boolean isHidden();

	/**
	 * Set the hidden status of the node.
	 * 
	 * @param hidden
	 *            the new hidden status of the node
	 */
	public void setHidden(boolean hidden);

	/**
	 * Is the node enabled/active?
	 * 
	 * @return true if the node is enabled, false otherwise
	 */
	public boolean isEnabled();

	/**
	 * Set the enabled status of the node.
	 * 
	 * @param enabled
	 *            the new enabled status of the node
	 */
	public void setEnabled(boolean enabled);

	/**
	 * Get the parent node of this node.
	 * 
	 * @return the parent node of this node, or null if this node is the root
	 *         NavigationModel
	 */
	public NavigationNode getParent();

	/**
	 * Set the parent of this node.
	 * 
	 * @param parent
	 *            the new parent of this node
	 */
	public void setParent(NavigationNode parent);

	/**
	 * Is this node a parent of the specified node?
	 * 
	 * @param node
	 *            the node to check
	 * @return true if this node is a direct or indirect parent of the specified
	 *         node, false otherwise
	 */
	public boolean isParent(NavigationNode node);

	/**
	 * Get the depth of this node in the hierarchy. The root NavigationModel has
	 * depth 0, all other nodes have a depth equal to the depth of their parent +
	 * 1.
	 * 
	 * @return the depth of the node in the hierarhcy
	 */
	public int getDepth();

	public String getPathPrefix();
	
	public String getPathSeparator();
	
	public String getPath();

	public void setPath(String path);

	public String getIconPrefix();

	public String getIconSeparator();
	
	public String getIconSuffix();

	public String getIcon();

	public void setIcon(String icon);

	public String getI18nPrefix();

	public String getI18nSeparator();
	
	public String getI18nSuffix();

	public String getI18n();

	public void setI18n(String i18n);
	
	public String getViewSuffix();
	
	public void setViewSuffix(String suffix);
}