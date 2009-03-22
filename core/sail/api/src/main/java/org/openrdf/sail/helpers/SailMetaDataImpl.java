/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.sail.SailMetaData;
import org.openrdf.store.Isolation;

/**
 * @author James Leigh
 */
public class SailMetaDataImpl implements SailMetaData {

	private static final String SESAME_POM_PROPERTIES = "/META-INF/maven/org.openrdf.sesame/sesame-sail-api/pom.properties";

	private Logger logger = LoggerFactory.getLogger(SailMetaDataImpl.class);

	public String[] getInferenceRules() {
		return new String[0];
	}

	public URL getLocation() {
		return null;
	}

	public int getMaxLiteralLength() {
		return 0;
	}

	public int getMaxURILength() {
		return 0;
	}

	public String[] getQueryFunctions() {
		return new String[0];
	}

	public String[] getReasoners() {
		return new String[0];
	}

	public int getSesameMajorVersion() {
		String version = getSesameVersion();
		if (version == null) {
			return 0;
		}
		int idx = version.indexOf('.');
		if (idx < 0) {
			return 0;
		}
		try {
			return Integer.parseInt(version.substring(0, idx));
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}

	public int getSesameMinorVersion() {
		String version = getSesameVersion();
		if (version == null) {
			return 0;
		}
		int idx = version.indexOf('.');
		if (idx < 0) {
			return 0;
		}
		int dot = version.indexOf('.', idx + 1);
		int dash = version.indexOf('-', idx + 1);
		if (dot < 0 && dash < 0) {
			return 0;
		}
		int end = 0 < dot && dot < dash ? dot : dash;
		try {
			return Integer.parseInt(version.substring(idx + 1, end));
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}

	public String getSesameVersion() {
		InputStream in = getClass().getResourceAsStream(SESAME_POM_PROPERTIES);
		if (in == null) {
			return null;
		}
		Properties pom = new Properties();
		try {
			pom.load(in);
		}
		catch (IOException e) {
			logger.error(e.toString(), e);
			return null;
		}
		String version = (String)pom.get("version");
		return version;
	}

	public int getStoreMajorVersion() {
		return 0;
	}

	public int getStoreMinorVersion() {
		return 0;
	}

	public String getStoreName() {
		return null;
	}

	public String getStoreVersion() {
		return null;
	}

	public boolean isRemoteDatasetSupported() {
		return false;
	}

	public boolean isContextBNodesSupported() {
		return false;
	}

	public boolean isContextSupported() {
		return false;
	}

	public boolean isHierarchicalInferencing() {
		return false;
	}

	public boolean isInferencing() {
		return false;
	}

	public boolean isLiteralDatatypePreserved() {
		return false;
	}

	public boolean isLiteralLabelPreserved() {
		return false;
	}

	public boolean isBNodeIDPreserved() {
		return false;
	}

	public boolean isEmbedded() {
		return false;
	}

	public boolean isMatchingOnlySameTerm() {
		return false;
	}

	public boolean isOWLInferencing() {
		return false;
	}

	public boolean isRDFSInferencing() {
		return false;
	}

	public boolean isReadOnly() {
		return false;
	}

	public Isolation getDefaultIsolation() {
		return Isolation.NONE;
	}

	public boolean supportsIsolation(Isolation isolation) {
		return getDefaultIsolation().isCompatibleWith(isolation);
	}

}
