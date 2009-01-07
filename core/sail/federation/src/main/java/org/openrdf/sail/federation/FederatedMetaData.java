/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryMetaData;
import org.openrdf.sail.SailMetaData;
import org.openrdf.sail.helpers.SailMetaDataWrapper;
import org.openrdf.store.StoreException;

/**
 * Load {@link RepositoryMetaData} from the members and union them into a
 * {@link SailMetaData}.
 * 
 * @author James Leigh
 */
public class FederatedMetaData extends SailMetaDataWrapper {

	private List<RepositoryMetaData> members;

	private boolean readOnly;

	public FederatedMetaData(SailMetaData delegate, Collection<Repository> members)
		throws StoreException
	{
		super(delegate);
		this.members = new ArrayList<RepositoryMetaData>(members.size());
		for (Repository r : members) {
			this.members.add(r.getMetaData());
		}
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Override
	public int getMaxLiteralLength() {
		int max = 0;
		for (RepositoryMetaData md : members) {
			int m = md.getMaxLiteralLength();
			if (max == 0 || m != 0 && m < max) {
				max = m;
			}
		}
		return max;
	}

	@Override
	public int getMaxURILength() {
		int max = 0;
		for (RepositoryMetaData md : members) {
			int m = md.getMaxURILength();
			if (max == 0 || m != 0 && m < max) {
				max = m;
			}
		}
		return max;
	}

	@Override
	public String[] getQueryFunctions() {
		List<String> list = new ArrayList<String>();
		for (RepositoryMetaData md : members) {
			list.addAll(Arrays.asList(md.getQueryFunctions()));
		}
		return list.toArray(new String[list.size()]);
	}

	@Override
	public String[] getInferenceRules() {
		List<String> list = new ArrayList<String>();
		for (RepositoryMetaData md : members) {
			list.addAll(Arrays.asList(md.getInferenceRules()));
		}
		return list.toArray(new String[list.size()]);
	}

	@Override
	public String[] getReasoners() {
		List<String> list = new ArrayList<String>();
		for (RepositoryMetaData md : members) {
			list.addAll(Arrays.asList(md.getReasoners()));
		}
		return list.toArray(new String[list.size()]);
	}

	@Override
	public boolean isBNodeIDPreserved() {
		for (RepositoryMetaData md : members) {
			if (!md.isBNodeIDPreserved()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isContextBNodesSupported() {
		for (RepositoryMetaData md : members) {
			if (!md.isContextBNodesSupported()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isContextSupported() {
		for (RepositoryMetaData md : members) {
			if (!md.isContextSupported()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isHierarchicalInferencing() {
		for (RepositoryMetaData md : members) {
			if (!md.isHierarchicalInferencing()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isInferencing() {
		for (RepositoryMetaData md : members) {
			if (!md.isInferencing()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isLiteralDatatypePreserved() {
		for (RepositoryMetaData md : members) {
			if (!md.isLiteralDatatypePreserved()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isLiteralLabelPreserved() {
		for (RepositoryMetaData md : members) {
			if (!md.isLiteralLabelPreserved()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isLocalStore() {
		for (RepositoryMetaData md : members) {
			if (!md.isLocalStore()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isMatchingOnlySameTerm() {
		for (RepositoryMetaData md : members) {
			if (!md.isMatchingOnlySameTerm()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isOWLInferencing() {
		for (RepositoryMetaData md : members) {
			if (!md.isOWLInferencing()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isRDFSInferencing() {
		for (RepositoryMetaData md : members) {
			if (!md.isRDFSInferencing()) {
				return false;
			}
		}
		return true;
	}

}
