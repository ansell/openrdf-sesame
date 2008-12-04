/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import java.net.URL;

import org.openrdf.query.QueryLanguage;
import org.openrdf.rio.RDFFormat;

/**
 * @author James Leigh
 */
public interface RepositoryMetaData {

	QueryLanguage[] getQueryLanguages();

	RDFFormat[] getAddRDFFormats();

	RDFFormat[] getExportRDFFormats();

	boolean isRemoteDatasetSupported();

	// properties shared by SailMetaData

	boolean isReadOnly();

	URL getLocation();

	boolean isLocalStore();

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
