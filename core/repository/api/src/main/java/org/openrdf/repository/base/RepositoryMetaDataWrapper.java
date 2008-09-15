/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.base;

import java.net.URL;

import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryMetaData;
import org.openrdf.rio.RDFFormat;


/**
 *
 * @author James Leigh
 */
public class RepositoryMetaDataWrapper implements RepositoryMetaData {
	private RepositoryMetaData delegate;

	public RepositoryMetaDataWrapper(RepositoryMetaData delegate) {
		this.delegate = delegate;
	}

	public RDFFormat[] getAddRDFFormats() {
		return delegate.getAddRDFFormats();
	}

	public RDFFormat[] getExportRDFFormats() {
		return delegate.getExportRDFFormats();
	}

	public QueryLanguage[] getQueryLanguages() {
		return delegate.getQueryLanguages();
	}

	public boolean isRemoteDatasetSupported() {
		return delegate.isRemoteDatasetSupported();
	}

	public String[] getInferenceRules() {
		return delegate.getInferenceRules();
	}

	public URL getLocation() {
		return delegate.getLocation();
	}

	public int getMaxLiteralLength() {
		return delegate.getMaxLiteralLength();
	}

	public int getMaxURILengeth() {
		return delegate.getMaxURILengeth();
	}

	public String[] getQueryFunctions() {
		return delegate.getQueryFunctions();
	}

	public String[] getReasoners() {
		return delegate.getReasoners();
	}

	public int getSesameMajorVersion() {
		return delegate.getSesameMajorVersion();
	}

	public int getSesameMinorVersion() {
		return delegate.getSesameMinorVersion();
	}

	public String getSesameVersion() {
		return delegate.getSesameVersion();
	}

	public int getStoreMajorVersion() {
		return delegate.getStoreMajorVersion();
	}

	public int getStoreMinorVersion() {
		return delegate.getStoreMinorVersion();
	}

	public String getStoreName() {
		return delegate.getStoreName();
	}

	public String getStoreVersion() {
		return delegate.getStoreVersion();
	}

	public boolean isContextBNodesSupported() {
		return delegate.isContextBNodesSupported();
	}

	public boolean isContextSupported() {
		return delegate.isContextSupported();
	}

	public boolean isHierarchicalInferencing() {
		return delegate.isHierarchicalInferencing();
	}

	public boolean isInferencing() {
		return delegate.isInferencing();
	}

	public boolean isLiteralDatatypePreserved() {
		return delegate.isLiteralDatatypePreserved();
	}

	public boolean isLiteralLabelPreserved() {
		return delegate.isLiteralLabelPreserved();
	}

	public boolean isBNodeIDPreserved() {
		return delegate.isBNodeIDPreserved();
	}

	public boolean isLocalStore() {
		return delegate.isLocalStore();
	}

	public boolean isMatchingOnlySameTerm() {
		return delegate.isMatchingOnlySameTerm();
	}

	public boolean isOWLInferencing() {
		return delegate.isOWLInferencing();
	}

	public boolean isRDFSInferencing() {
		return delegate.isRDFSInferencing();
	}

	public boolean isReadOnly() {
		return delegate.isReadOnly();
	}
}
