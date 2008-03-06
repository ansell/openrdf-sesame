/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.openrdf.sail.SailInitializationException;
import org.openrdf.sail.nativerdf.btree.BTree;
import org.openrdf.sail.nativerdf.btree.BTreeIterator;
import org.openrdf.sail.nativerdf.btree.BTreeValueComparator;
import org.openrdf.util.ByteArrayUtil;
import org.openrdf.util.log.ThreadLog;


/**
 * File-based indexed storage and retrieval of RDF statements. TripleStore
 * stores statements in the form of four integer IDs. Each ID represent an RDF
 * value that is stored in a {@link ValueStore}. The four IDs refer to the
 * statement's subject, predicate, object and context. The ID <tt>0</tt> is used
 * to represent the null context and doesn't map to an actual RDF value. 
 */
class TripleStore {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * The file name for the properties file.
	 */
	private static final String PROPERTIES_FILE = "triples.prop";
	
	/**
	 * The key used to store the triple store version in the properties file.
	 */
	private static final String VERSION_KEY = "version";
	
	/**
	 * The key used to store the triple indexes specification that specifies
	 * which triple indexes exist.
	 */
	private static final String INDEXES_KEY = "triple-indexes";

	/**
	 * The version number for the current triple store.
	 * version 0: The first version which used a single spo-index. This version
	 * did not have a properties file yet.
	 * version 1: Introduces configurable triple indexes and the properties
	 * file.
	 * version 10: Introduces a context field, essentially making this a quad
	 * store.
	 */
	private static final int SCHEME_VERSION = 10;

	/* 17 bytes are used to represent a triple:
     *  byte  0-3 : subject
     *  byte  4-7 : predicate
     *  byte  8-11: object
     *  byte 12-15: context
     *  byte    16: additional flag(s)
	 */
	static final int RECORD_LENGTH = 17;
	static final int SUBJ_IDX = 0;
	static final int PRED_IDX = 4;
	static final int OBJ_IDX  = 8;
	static final int CONTEXT_IDX = 12;
	static final int FLAG_IDX = 16;

	/**
	 * Bit field indicating that a statement has been explicitly added (instead
	 * of being inferred).
	 */
	static final byte EXPLICIT_FLAG = (byte)0x1; // 0000 0001

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The directory that is used to store the index files.
	 */
	private File _dir;

	/**
	 * Object containing meta-data for the triple store. This includes 
	 */
	private Properties _properties;

	/**
	 * The array of triple indexes that are used to store and retrieve triples.
	 */
	private TripleIndex[] _indexes;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public TripleStore(File dir, String indexSpecStr)
		throws IOException, SailInitializationException
	{
		_dir = dir;
		_properties = new Properties();

		// Read triple properties file, restore indexes, reindex
		File propFile = new File(dir, PROPERTIES_FILE);

		if (propFile.exists()) {
			_loadProperties(propFile);

			// Check version number
			String versionStr = _properties.getProperty(VERSION_KEY);
			if (versionStr == null) {
				ThreadLog.warning("version missing in TripleStore's properties file");
			}
			else {
				try {
					int version = Integer.parseInt(versionStr);
					if (version < 10) {
						throw new SailInitializationException(
								"Directory contains incompatible triple data");
					}
				}
				catch (NumberFormatException e) {
					ThreadLog.warning("Malformed version number in TripleStore's properties file");
				}
			}
		}

		Set<String> indexSpecs = _parseIndexSpecList(indexSpecStr);

		if (indexSpecs.isEmpty()) {
			// Create default spoc index
			ThreadLog.log("No indexes specified, defaulting to single spoc index");
			indexSpecs.add("spoc");
			indexSpecStr = "spoc";
		}

		// Initialize added indexes and delete removed ones:
		_reindex(indexSpecs);

		if (!String.valueOf(SCHEME_VERSION).equals(_properties.getProperty(VERSION_KEY)) ||
			!indexSpecStr.equals(_properties.getProperty(INDEXES_KEY)))
		{
			// Store up-to-date properties
			_properties.setProperty(VERSION_KEY, String.valueOf(SCHEME_VERSION));
			_properties.setProperty(INDEXES_KEY, indexSpecStr);
			_storeProperties(propFile);
		}

		// Create specified indexes
		_indexes = new TripleIndex[indexSpecs.size()];
		int i = 0;
		for (String fieldSeq : indexSpecs) {
			ThreadLog.trace("Activating index '" + fieldSeq + "'...");
			_indexes[i++] = new TripleIndex(fieldSeq);
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Parses a comma/whitespace-separated list of index specifications. Index
	 * specifications are required to consists of 4 characters: 's', 'p', 'o'
	 * and 'c'.
	 *
	 * @param indexSpecStr A string like "spoc, pocs, cosp".
	 * @return A Set containing the parsed index specifications.
	 */
	private Set<String> _parseIndexSpecList(String indexSpecStr)
		throws SailInitializationException
	{
		Set<String> indexes = new HashSet<String>();

		if (indexSpecStr != null) {
			StringTokenizer tok = new StringTokenizer(indexSpecStr, ", \t");
			while (tok.hasMoreTokens()) {
				String index = tok.nextToken().toLowerCase();

				// sanity checks
				if (index.length() != 4 ||
					index.indexOf('s') == -1 ||
					index.indexOf('p') == -1 ||
					index.indexOf('o') == -1 ||
					index.indexOf('c') == -1)
				{
					throw new SailInitializationException(
							"invalid value '" + index + "' in index specification: " + indexSpecStr);
				}

				indexes.add(index);
			}
		}

		return indexes;
	}

	private void _reindex(Set<String> newIndexSpecs)
		throws IOException, SailInitializationException
	{
		// Check if the index specification has changed and update indexes if necessary
		String currentIndexSpecStr = _properties.getProperty(INDEXES_KEY);
		if (currentIndexSpecStr == null) {
			return;
		}

		Set<String> currentIndexSpecs = _parseIndexSpecList(currentIndexSpecStr);

		if (currentIndexSpecs.isEmpty()) {
			throw new SailInitializationException("Invalid index specification found in index properties");
		}

		// Determine the set of newly added indexes
		Set<String> addedIndexSpecs = new HashSet<String>(newIndexSpecs);
		addedIndexSpecs.removeAll(currentIndexSpecs);

		if (!addedIndexSpecs.isEmpty()) {
			// Initialize new indexes using an existing index as source
			String sourceIndexSpec = currentIndexSpecs.iterator().next();
			TripleIndex sourceIndex = new TripleIndex(sourceIndexSpec);

			try {
				for (String fieldSeq : addedIndexSpecs) {
					ThreadLog.trace("Initializing new index '" + fieldSeq + "'...");

					TripleIndex addedIndex = new TripleIndex(fieldSeq);
					BTree addedBTree = addedIndex.getBTree();

					BTreeIterator sourceIter = sourceIndex.getBTree().iterateAll();
					try {
						byte[] value = null;
						while ( (value = sourceIter.next()) != null) {
							addedBTree.insert(value);
						}
					}
					finally {
						sourceIter.close();
					}

					addedBTree.sync();
					addedBTree.close();
				}

				ThreadLog.trace("New index(es) initialized");
			}
			finally {
				sourceIndex.getBTree().close();
			}
		}

		// Determine the set of removed indexes
		Set<String> removedIndexSpecs = new HashSet<String>(currentIndexSpecs);
		removedIndexSpecs.removeAll(newIndexSpecs);

		// Delete files for removed indexes
		for (String fieldSeq : removedIndexSpecs) {
			boolean deleted = _getIndexFile(fieldSeq).delete();

			if (deleted) {
				ThreadLog.trace("Deleted file for removed " + fieldSeq + " index");
			}
			else {
				ThreadLog.warning("Unable to delete file for removed " + fieldSeq + " index");
			}
		}
	}

	public void close()
		throws IOException
	{
		for (int i = 0; i < _indexes.length; i++) {
			_indexes[i].getBTree().close();
		}
		_indexes = null;
	}

	public BTreeIterator getTriples(int subj, int pred, int obj, int context)
		throws IOException
	{
		// Get best matching index
		int bestScore = -1;
		TripleIndex bestIndex = null;
		for (int i = 0; i < _indexes.length; i++) {
			int score = _indexes[i].getPatternScore(subj, pred, obj, context);
			if (score > bestScore) {
				bestScore = score;
				bestIndex = _indexes[i];
			}
		}

		byte[] searchKey = _getSearchKey(subj, pred, obj, context);
		byte[] searchMask = _getSearchMask(subj, pred, obj, context);

		if (bestScore > 0) {
			// Use ranged search
			byte[] minValue = _getMinValue(subj, pred, obj, context);
			byte[] maxValue = _getMaxValue(subj, pred, obj, context);

			//ThreadLog.trace("using " + bestIndex.getFieldSeq() + " index with a score of " +
			//		bestScore + " for pattern (" + subj + ", " + pred + ", " + obj + ")");

			return bestIndex.getBTree().iterateValues(searchKey, searchMask, minValue, maxValue);
		}
		else {
			//ThreadLog.trace("using sequential search for pattern (" + subj + ", " + pred + ", " + obj + ")");
			// Use sequential scan
			return bestIndex.getBTree().iterateValues(searchKey, searchMask);
		}
	}

	public void sync()
		throws IOException
	{
		for (int i = 0; i < _indexes.length; i++) {
			_indexes[i].getBTree().sync();
		}
	}

	public void clear()
		throws IOException
	{
		for (int i = 0; i < _indexes.length; i++) {
			_indexes[i].getBTree().clear();
		}
	}

	public byte[] storeTriple(int subj, int pred, int obj, int context)
		throws IOException
	{
		byte[] data = _getData(subj, pred, obj, context);

		byte[] oldData = _indexes[0].getBTree().insert(data);

		if (oldData == null || !Arrays.equals(data, oldData)) {
			// new or changed data was inserted into the first index,
			// also insert it into the other indexes
			for (int i = 1; i < _indexes.length; i++) {
				_indexes[i].getBTree().insert(data);
			}
		}

		return oldData;
	}

	public int removeTriples(int subj, int pred, int obj, int context)
		throws IOException
	{
		// FIXME: naive implementation below
		ArrayList<byte[]> removeList = new ArrayList<byte[]>();

		BTreeIterator iter = getTriples(subj, pred, obj, context);
		byte[] value = null;
		while ( (value = iter.next()) != null) {
			removeList.add(value);
		}

		for (int i = 0; i < _indexes.length; i++) {
			BTree btree = _indexes[i].getBTree();

			for (int j = 0; j < removeList.size(); j++) {
				btree.remove( (byte[])removeList.get(j) );
			}
		}

		return removeList.size();
	}

	private byte[] _getData(int subj, int pred, int obj, int context) {
		byte[] data = new byte[RECORD_LENGTH];

		ByteArrayUtil.putInt(subj, data, SUBJ_IDX);
		ByteArrayUtil.putInt(pred, data, PRED_IDX);
		ByteArrayUtil.putInt(obj, data, OBJ_IDX);
		ByteArrayUtil.putInt(context, data, CONTEXT_IDX);
		data[FLAG_IDX] = EXPLICIT_FLAG;

		return data;
	}

	private byte[] _getSearchKey(int subj, int pred, int obj, int context) {
		byte[] searchKey = _getData(subj, pred, obj, context);
		searchKey[FLAG_IDX] = 0;
		return searchKey;
	}

	private byte[] _getSearchMask(int subj, int pred, int obj, int context) {
		byte[] mask = new byte[RECORD_LENGTH];

		if (subj != -1) {
			ByteArrayUtil.putInt(0xffffffff, mask, SUBJ_IDX);
		}
		if (pred != -1) {
			ByteArrayUtil.putInt(0xffffffff, mask, PRED_IDX);
		}
		if (obj != -1) {
			ByteArrayUtil.putInt(0xffffffff, mask, OBJ_IDX);
		}
		if (context != -1) {
			ByteArrayUtil.putInt(0xffffffff, mask, CONTEXT_IDX);
		}

		return mask;
	}

	private byte[] _getMinValue(int subj, int pred, int obj, int context) {
		byte[] minValue = new byte[RECORD_LENGTH];

		ByteArrayUtil.putInt( (   subj == -1 ? 0x00000000 : subj)   , minValue, SUBJ_IDX);
		ByteArrayUtil.putInt( (   pred == -1 ? 0x00000000 : pred)   , minValue, PRED_IDX);
		ByteArrayUtil.putInt( (    obj == -1 ? 0x00000000 : obj)    , minValue, OBJ_IDX);
		ByteArrayUtil.putInt( (context == -1 ? 0x00000000 : context), minValue, CONTEXT_IDX);

		return minValue;
	}

	private byte[] _getMaxValue(int subj, int pred, int obj, int context) {
		byte[] maxValue = new byte[RECORD_LENGTH];

		ByteArrayUtil.putInt( (   subj == -1 ? 0xffffffff : subj)   , maxValue, SUBJ_IDX);
		ByteArrayUtil.putInt( (   pred == -1 ? 0xffffffff : pred)   , maxValue, PRED_IDX);
		ByteArrayUtil.putInt( (    obj == -1 ? 0xffffffff : obj)    , maxValue, OBJ_IDX);
		ByteArrayUtil.putInt( (context == -1 ? 0xffffffff : context), maxValue, CONTEXT_IDX);
		
		return maxValue;
	}

	private File _getIndexFile(String fieldSeq) {
		return new File(_dir, "triples-" + fieldSeq + ".dat");
	}

	private void _loadProperties(File propFile)
		throws IOException
	{
		InputStream in = new FileInputStream(propFile);
		try {
			_properties.clear();
			_properties.load(in);
		}
		finally {
			in.close();
		}
	}

	private void _storeProperties(File propFile)
		throws IOException
	{
		OutputStream out = new FileOutputStream(propFile);
		try {
			_properties.store(out, "triple indexes meta-data, DO NOT EDIT!");
		}
		finally {
			out.close();
		}
	}

	/*-------------------------*
	 * Inner class TripleIndex *
	 *-------------------------*/

	private class TripleIndex {

		private TripleComparator _tripleComparator;
		private BTree _btree;

		public TripleIndex(String fieldSeq)
			throws IOException
		{
			_tripleComparator = new TripleComparator(fieldSeq);
			File btreeFile = _getIndexFile(fieldSeq);
			_btree = new BTree(btreeFile, 2048, RECORD_LENGTH, _tripleComparator);
		}

		public char[] getFieldSeq() {
			return _tripleComparator.getFieldSeq();
		}

		public File getFile() {
			return _btree.getFile();
		}

		public BTree getBTree() {
			return _btree;
		}

		/**
		 * Determines the 'score' of this index on the supplied pattern of
		 * subject, predicate, object and context IDs. The higher the score, the
		 * better the index is suited for matching the pattern. Lowest score is
		 * 0, which means that the index will perform a sequential scan.
		 */
		public int getPatternScore(int subj, int pred, int obj, int context) {
			int score = 0;

			for (char field : _tripleComparator.getFieldSeq()) {
				switch (field) {
					case 's':
						if (subj >= 0) {
							score++;
						}
						else {
							return score;
						}
						break;
					case 'p':
						if (pred >= 0) {
							score++;
						}
						else {
							return score;
						}
						break;
					case 'o':
						if (obj >= 0) {
							score++;
						}
						else {
							return score;
						}
					case 'c':
						if (context >= 0) {
							score++;
						}
						else {
							return score;
						}
				}
			}

			return score;
		}
	}

	/*------------------------------*
	 * Inner class TripleComparator *
	 *------------------------------*/

	/**
	 * A BTreeValueComparator that can be used to create indexes with a
	 * configurable order of the subject, predicate, object and context fields.
	 */
	private static class TripleComparator implements BTreeValueComparator {

		private char[] _fieldSeq;

		public TripleComparator(String fieldSeq) {
			_fieldSeq = fieldSeq.toCharArray();
		}
		
		public char[] getFieldSeq() {
			return _fieldSeq;
		}

		// implements BTreeValueComparator.compareBTreeValues()
		public final int compareBTreeValues(byte[] key, byte[] data, int offset, int length) {
			for (char field : _fieldSeq) {
				int fieldIdx = 0;

				switch (field) {
					case 's': fieldIdx = SUBJ_IDX; break;
					case 'p': fieldIdx = PRED_IDX; break;
					case 'o': fieldIdx = OBJ_IDX; break;
					case 'c': fieldIdx = CONTEXT_IDX; break;
					default: throw new IllegalArgumentException("invalid character '" + field
							+ "' in field sequence: " + new String(_fieldSeq));
				}
				
				int diff = ByteArrayUtil.compareRegion(key, fieldIdx, data, offset + fieldIdx, 4);

				if (diff != 0) {
					return diff;
				}
			}

			return 0;
		}
	}
}
