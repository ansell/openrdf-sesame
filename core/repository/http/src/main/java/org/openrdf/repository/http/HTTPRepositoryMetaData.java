/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.net.URL;

import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryMetaData;
import org.openrdf.rio.RDFFormat;


public class HTTPRepositoryMetaData implements RepositoryMetaData {

	public HTTPRepositoryMetaData(HTTPRepository repository) {
		// TODO Auto-generated constructor stub
	}

	public RDFFormat[] getAddRDFFormats() {
		// TODO Auto-generated method stub
		return null;
	}

	public RDFFormat[] getExportRDFFormats() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getInferenceRules() {
		// TODO Auto-generated method stub
		return null;
	}

	public URL getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getMaxLiteralLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxURILengeth() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String[] getQueryFunctions() {
		// TODO Auto-generated method stub
		return null;
	}

	public QueryLanguage[] getQueryLanguages() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getReasoners() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSesameMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getSesameMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getSesameVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getStoreMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getStoreMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getStoreName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getStoreVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isBNodeIDPreserved() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isContextBNodesSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isContextSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isHierarchicalInferencing() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isInferencing() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isLiteralDatatypePreserved() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isLiteralLabelPreserved() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isLocalStore() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isMatchingOnlySameTerm() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isOWLInferencing() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRDFSInferencing() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRemoteDatasetSupported() {
		// TODO Auto-generated method stub
		return false;
	}

}
