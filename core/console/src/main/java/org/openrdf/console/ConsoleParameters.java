/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.console;

/**
 * @author dale
 */
public interface ConsoleParameters {

	int getWidth();

	void setWidth(int width);

	boolean isShowPrefix();

	void setShowPrefix(boolean value);

	boolean isQueryPrefix();

	void setQueryPrefix(boolean value);
}