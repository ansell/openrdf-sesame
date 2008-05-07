/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.util;

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.manager.RepositoryInfo;

public class RepositoryUtil {

	/**
	 * Derives a ID from a specified base name. The base name may for example be
	 * a Repository name entered by the user. The derived ID will contain a
	 * version of this name that does not occur as a Repository ID in the
	 * specified System Repository and is suitable for use as a file name (e.g.
	 * for its data dir).
	 * 
	 * @param baseName
	 *        The String on which the returned ID should be based. This may be
	 *        null or an empty string.
	 * @param repository
	 *        The system Repository holding all Repository configurations,
	 *        including their IDs.
	 * @return A ID for a new Repository that is not used in the set of
	 *         Repositories maintained by the specified repository at the moment
	 *         of invocation.
	 * @throws RepositoryException
	 *         when queries to the system repository fails.
	 */
	public static String createID(String baseName, Repository repository)
		throws RepositoryException
	{
		// derive a base name that can safely be used as file name, XML ID, etc.
		if (baseName != null) {
			baseName = baseName.trim();

			// derive a base name that's safe to use as a file name
			int length = baseName.length();
			StringBuilder buffer = new StringBuilder(length);
			for (int i = 0; i < length; i++) {
				char c = baseName.charAt(i);
				if (Character.isLetter(c) || Character.isDigit(c) || c == '-' || c == '_' || c == '.') {
					buffer.append(Character.toLowerCase(c));
				}
				else if (c != '"' && c != '\'') {
					buffer.append('-');
				}
			}

			baseName = buffer.toString();
		}

		// when the base name is null or empty, generate one
		int index = 0;
		String result = baseName;

		if (baseName == null || baseName.length() == 0) {
			baseName = "source-";
			result = baseName + index;
			index++;
		}

		// keep appending numbers at the end of the name until we find an ID
		// that's not in use yet
		RepositoryConnection con = repository.getConnection();
		try {
			while (con.hasStatement(null, REPOSITORYID, new LiteralImpl(result), false)) {
				if (baseName.endsWith("-")) {
					result = baseName + index;
				}
				else {
					result = baseName + '-' + index;
				}
				index++;
			}

			return result;
		}
		finally {
			con.close();
		}
	}

	/**
	 * @return
	 * @throws RepositoryException 
	 */
	public static List<RepositoryInfo> getRemoteRepositoryInfos(String serverURL, String username, String password, boolean skipSystemRepo) throws RepositoryException {
		List<RepositoryInfo> result = new ArrayList<RepositoryInfo>();
	
		RemoteRepositoryManager manager = new RemoteRepositoryManager(serverURL);
		manager.setUsernameAndPassword(username, password);
		manager.initialize();
	
		result.addAll(manager.getAllRepositoryInfos(skipSystemRepo));
		Collections.sort(result);
		
		return result;
	}

	public static RepositoryInfo getRemoteRepositoryInfo(String serverURL, String username, String password, String id) throws RepositoryException {
		RepositoryInfo result = null;
		
		RemoteRepositoryManager manager = new RemoteRepositoryManager(serverURL);
		manager.setUsernameAndPassword(username, password);
		manager.initialize();
		
		result = manager.getRepositoryInfo(id);
		
		return result;
	}

	public static Repository getRemoteRepository(String serverURL, String username, String password, String id) throws RepositoryException, RepositoryConfigException {
		Repository result = null;
		
		RemoteRepositoryManager manager = new RemoteRepositoryManager(serverURL);
		manager.setUsernameAndPassword(username, password);
		manager.initialize();
		
		result = manager.getRepository(id);
		
		return result;
	}
}
