/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * A configuration object containing the configuration parameters for a
 * repository. Configuration options for repositories include read-write
 * permissions and the configuration of the Sail stack. Read- and write
 * permissions can be configured separately as publicly accessible, or to be
 * restricted to specific accounts. A repository that is both publicly readable
 * and publicly writeable is called a 'public repository'. A repository that is
 * neither publicly readable nor publicly writeable is called a 'private
 * repository'.
 */
public class RepositoryConfig {

	/*-----------*
	 * Variables *
	 *-----------*/

	/** The repository's ID. */
	private String _id;

	/** The repository's title. */
	private String _title;

	private String _className;

	/** Flag indicating whether the repository should be publicly readable. */
	private boolean _worldReadable = false;

	/** Flag indicating whether the repository should be publicly writeable. */
	private boolean _worldWriteable = false;

	private HashSet<String> _readACL = new HashSet<String>();

	private HashSet<String> _writeACL = new HashSet<String>();;

	/**
	 * Stack of SailConfig objects, representing the Sail stack.
	 */
	private Stack<SailConfig> _sailStack = new Stack<SailConfig>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new repository configuration object for a private repository.
	 */
	public RepositoryConfig() {
	}

	/**
	 * Creates a new repository configuration object for a private repository
	 * with the specified ID.
	 * 
	 * @param id
	 *        The repository id.
	 */
	public RepositoryConfig(String id) {
		setID(id);
	}

	/**
	 * Creates a new repository configuration object for a private repository
	 * with the specified ID and title.
	 * 
	 * @param id
	 *        The repository id.
	 * @param title
	 *        The repository title.
	 */
	public RepositoryConfig(String id, String title) {
		setID(id);
		setTitle(title);
	}

	/**
	 * Creates a new repository configuration object for a repository with the
	 * specified ID and title. Whether or not the repository is publicly readable
	 * and/or writeable is controlled by the <tt>worldReadable</tt> and
	 * <tt>worldWriteable</tt> parameters.
	 * 
	 * @param id
	 *        The repository id.
	 * @param worldReadable
	 *        Flag indicating wether the repository should be publicly readable.
	 * @param worldWriteable
	 *        Flag indicating wether the repository should be publicly writeable.
	 */
	public RepositoryConfig(String id, boolean worldReadable, boolean worldWriteable) {
		setID(id);
		setWorldReadable(worldReadable);
		setWorldWriteable(worldWriteable);
	}

	/**
	 * Creates a new repository configuration object for a repository with the
	 * specified ID and title. Whether or not the repository is publicly readable
	 * and/or writeable is controlled by the <tt>worldReadable</tt> and
	 * <tt>worldWriteable</tt> parameters.
	 * 
	 * @param id
	 *        The repository id.
	 * @param title
	 *        The repository title.
	 * @param worldReadable
	 *        Flag indicating wether the repository should be publicly readable.
	 * @param worldWriteable
	 *        Flag indicating wether the repository should be publicly writeable.
	 */
	public RepositoryConfig(String id, String title, boolean worldReadable, boolean worldWriteable) {
		setID(id);
		setTitle(title);
		setWorldReadable(worldReadable);
		setWorldWriteable(worldWriteable);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the ID of this repository.
	 * 
	 * @return The repository id.
	 */
	public String getID() {
		return _id;
	}

	/**
	 * Sets the ID of this repository.
	 * 
	 * @param The
	 *        repository id.
	 */
	public void setID(String id) {
		_id = id;
	}

	/**
	 * Gets the title of the repository.
	 * 
	 * @return the repository title
	 */
	public String getTitle() {
		return _title;
	}

	/**
	 * Sets the title of the repository.
	 * 
	 * @param title
	 *        the repository title
	 */
	public void setTitle(String title) {
		_title = title;
	}

	/**
	 * Returns the name of the configured Repository class.
	 */
	public String getClassName() {
		return _className;
	}
	
	/**
	 * Assigns the name of the Repository class to be used.
	 */
	public void setClassName(String className) {
		_className = className;
	}

	/**
	 * Checks whether this repository is publicly readable.
	 */
	public boolean isWorldReadable() {
		return _worldReadable;
	}

	/**
	 * Sets the world-readable flag of this repository.
	 */
	public void setWorldReadable(boolean worldReadable) {
		_worldReadable = worldReadable;
	}

	/**
	 * Checks whether this repository is publicly writeable.
	 */
	public boolean isWorldWriteable() {
		return _worldWriteable;
	}

	/**
	 * Sets the world-writeable flag of this repository.
	 */
	public void setWorldWriteable(boolean worldWriteable) {
		_worldWriteable = worldWriteable;
	}

	/**
	 * Makes this repository private, meaning that it will not be publicly
	 * readable or writeable.
	 */
	public void makePrivate() {
		setWorldReadable(false);
		setWorldWriteable(false);
	}

	/**
	 * Makes this repository public, meaning that it will be publicly readable
	 * and writeable.
	 */
	public void makePublic() {
		setWorldReadable(true);
		setWorldWriteable(true);
	}

	public void grantReadAccess(String login) {
		_readACL.add(login);
	}

	public void revokeReadAccess(String login) {
		_readACL.remove(login);
	}

	public boolean hasReadAccess(String login) {
		return _worldReadable || _readACL.contains(login);
	}

	public Set<String> getReadACL() {
		return Collections.unmodifiableSet(_readACL);
	}

	public void grantWriteAccess(String login) {
		_writeACL.add(login);
	}

	public void revokeWriteAccess(String login) {
		_writeACL.remove(login);
	}

	public boolean hasWriteAccess(String login) {
		return _worldWriteable || _writeACL.contains(login);
	}

	public Set<String> getWriteACL() {
		return Collections.unmodifiableSet(_writeACL);
	}

	/**
	 * Pushes the supplied Sail configuration on top of the current stack of Sail
	 * configurations.
	 * 
	 * @param sailConfig
	 *        A Sail configuration.
	 */
	public void pushSailConfig(SailConfig sailConfig) {
		_sailStack.push(sailConfig);
	}

	/**
	 * Adds the supplied Sail configuration to the bottom of the current stack of
	 * Sail configurations.
	 * 
	 * @param sailConfig
	 *        A Sail configuration.
	 */
	public void addSailConfig(SailConfig sailConfig) {
		_sailStack.add(0, sailConfig);
	}

	/**
	 * Removes the supplied Sail configuration from the current stack of Sail
	 * configurations.
	 * 
	 * @param sailConfig
	 *        The Sail configuration to remove.
	 */
	public boolean removeSailConfig(SailConfig sailConfig) {
		return _sailStack.remove(sailConfig);
	}

	/**
	 * Removes the Sail configuration that is at the specified index from the
	 * current stack of Sail configurations.
	 * 
	 * @param index
	 *        The index of the Sail configuration to remove, 0 for the top-most
	 *        Sail configuration.
	 */
	public SailConfig removeSailConfig(int index) {
		return _sailStack.remove(index);
	}

	/**
	 * Retrieves the current stack of Sail configurations.
	 * 
	 * @return A Stack of SailConfig objects.
	 * @see SailConfig
	 */
	public Stack<SailConfig> getSailConfigStack() {
		return _sailStack;
	}

	/**
	 * Sets the Sail configuration stack.
	 * 
	 * @param sailStack
	 *        A stack of SailConfig objects.
	 */
	public void setSailConfigStack(Stack<SailConfig> sailStack) {
		_sailStack = sailStack;
	}
}
