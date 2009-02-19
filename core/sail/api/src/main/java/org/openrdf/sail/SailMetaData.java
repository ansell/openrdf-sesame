/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

import java.net.URL;

/**
 * @author James Leigh
 */
public interface SailMetaData {

	boolean isReadOnly();

	URL getLocation();

	boolean isEmbedded();

	String getStoreName();

	String getStoreVersion();

	int getStoreMajorVersion();

	int getStoreMinorVersion();

	String getSesameVersion();

	int getSesameMajorVersion();

	int getSesameMinorVersion();

	int getMaxLiteralLength();

	int getMaxURILength();

	String[] getQueryFunctions();

	boolean isRemoteDatasetSupported();

	boolean isContextSupported();

	boolean isContextBNodesSupported();

	boolean isBNodeIDPreserved();

	boolean isLiteralDatatypePreserved();

	boolean isLiteralLabelPreserved();

	boolean isMatchingOnlySameTerm();

	boolean isInferencing();

	boolean isHierarchicalInferencing();

	boolean isRDFSInferencing();

	boolean isOWLInferencing();

	String[] getReasoners();

	String[] getInferenceRules();
}
