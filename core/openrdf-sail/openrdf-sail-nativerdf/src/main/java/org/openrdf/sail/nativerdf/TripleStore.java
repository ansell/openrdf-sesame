/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.io.ByteArrayUtil;

import org.openrdf.sail.SailException;
import org.openrdf.sail.nativerdf.btree.BTree;
import org.openrdf.sail.nativerdf.btree.BTreeIterator;
import org.openrdf.sail.nativerdf.btree.BTreeValueComparator;

/**
 * File-based indexed storage and retrieval of RDF statements. TripleStore
 * stores statements in the form of four integer IDs. Each ID represent an RDF
 * value that is stored in a {@link ValueStore}. The four IDs refer to the
 * statement's subject, predicate, object and context. The ID <tt>0</tt> is
 * used to represent the "null" context and doesn't map to an actual RDF value.
 * 
 * @author Arjohn Kampman
 */
class TripleStore {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

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
	 * The version number for the current triple store.<br>
	 * version 0: The first version which used a single spo-index. This version
	 * did not have a properties file yet.<br>
	 * version 1: Introduces configurable triple indexes and the properties file.<br>
	 * version 10: Introduces a context field, essentially making this a quad
	 * store.
	 */
	private static final int SCHEME_VERSION = 10;

	// 17 bytes are used to represent a triple:
	// byte 0-3 : subject
	// byte 4-7 : predicate
	// byte 8-11: object
	// byte 12-15: context
	// byte 16: additional flag(s)
	static final int RECORD_LENGTH = 17;

	static final int SUBJ_IDX = 0;

	static final int PRED_IDX = 4;

	static final int OBJ_IDX = 8;

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
	private File dir;

	/**
	 * Object containing meta-data for the triple store. This includes
	 */
	private Properties properties;

	/**
	 * The array of triple indexes that are used to store and retrieve triples.
	 */
	private TripleIndex[] indexes;

	private boolean forceSync;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public TripleStore(File dir, String indexSpecStr)
		throws IOException, SailException
	{
		this(dir, indexSpecStr, false);
	}

	public TripleStore(File dir, String indexSpecStr, boolean forceSync)
		throws IOException, SailException
	{
		this.dir = dir;
		this.forceSync = forceSync;
		properties = new Properties();

		// Read triple properties file, restore indexes, reindex
		File propFile = new File(dir, PROPERTIES_FILE);

		if (propFile.exists()) {
			loadProperties(propFile);

			// Check version number
			String versionStr = properties.getProperty(VERSION_KEY);
			if (versionStr == null) {
				logger.warn("version missing in TripleStore's properties file");
			}
			else {
				try {
					int version = Integer.parseInt(versionStr);
					if (version < 10) {
						throw new SailException("Directory contains incompatible triple data");
					}
				}
				catch (NumberFormatException e) {
					logger.warn("Malformed version number in TripleStore's properties file");
				}
			}
		}

		Set<String> indexSpecs = parseIndexSpecList(indexSpecStr);

		if (indexSpecs.isEmpty()) {
			// Create default spoc and posc indexes
			logger.info("No indexes specified, defaulting to indexes: spoc, posc");
			indexSpecs.add("spoc");
			indexSpecs.add("posc");
			indexSpecStr = "spoc,posc";
		}

		// Initialize added indexes and delete removed ones:
		reindex(indexSpecs);

		if (!String.valueOf(SCHEME_VERSION).equals(properties.getProperty(VERSION_KEY))
				|| !indexSpecStr.equals(properties.getProperty(INDEXES_KEY)))
		{
			// Store up-to-date properties
			properties.setProperty(VERSION_KEY, String.valueOf(SCHEME_VERSION));
			properties.setProperty(INDEXES_KEY, indexSpecStr);
			storeProperties(propFile);
		}

		// Create specified indexes
		indexes = new TripleIndex[indexSpecs.size()];
		int i = 0;
		for (String fieldSeq : indexSpecs) {
			logger.debug("Activating index '" + fieldSeq + "'...");
			indexes[i++] = new TripleIndex(fieldSeq);
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Parses a comma/whitespace-separated list of index specifications. Index
	 * specifications are required to consists of 4 characters: 's', 'p', 'o' and
	 * 'c'.
	 * 
	 * @param indexSpecStr
	 *        A string like "spoc, pocs, cosp".
	 * @return A Set containing the parsed index specifications.
	 */
	private Set<String> parseIndexSpecList(String indexSpecStr)
		throws SailException
	{
		Set<String> indexes = new HashSet<String>();

		if (indexSpecStr != null) {
			StringTokenizer tok = new StringTokenizer(indexSpecStr, ", \t");
			while (tok.hasMoreTokens()) {
				String index = tok.nextToken().toLowerCase();

				// sanity checks
				if (index.length() != 4 || index.indexOf('s') == -1 || index.indexOf('p') == -1
						|| index.indexOf('o') == -1 || index.indexOf('c') == -1)
				{
					throw new SailException("invalid value '" + index + "' in index specification: "
							+ indexSpecStr);
				}

				indexes.add(index);
			}
		}

		return indexes;
	}

	private void reindex(Set<String> newIndexSpecs)
		throws IOException, SailException
	{
		// Check if the index specification has changed and update indexes if
		// necessary
		String currentIndexSpecStr = properties.getProperty(INDEXES_KEY);
		if (currentIndexSpecStr == null) {
			return;
		}

		Set<String> currentIndexSpecs = parseIndexSpecList(currentIndexSpecStr);

		if (currentIndexSpecs.isEmpty()) {
			throw new SailException("Invalid index specification found in index properties");
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
					logger.debug("Initializing new index '" + fieldSeq + "'...");

					TripleIndex addedIndex = new TripleIndex(fieldSeq);
					BTree addedBTree = addedIndex.getBTree();

					BTreeIterator sourceIter = sourceIndex.getBTree().iterateAll();
					try {
						byte[] value = null;
						while ((value = sourceIter.next()) != null) {
							addedBTree.insert(value);
						}
					}
					finally {
						sourceIter.close();
					}

					addedBTree.close();
				}

				logger.debug("New index(es) initialized");
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
			boolean deleted = getIndexFile(fieldSeq).delete();

			if (deleted) {
				logger.debug("Deleted file for removed " + fieldSeq + " index");
			}
			else {
				logger.warn("Unable to delete file for removed " + fieldSeq + " index");
			}
		}
	}

	public void close()
		throws IOException
	{
		for (int i = 0; i < indexes.length; i++) {
			indexes[i].getBTree().close();
		}
		indexes = null;
	}

	public BTreeIterator getTriples(int subj, int pred, int obj, int context)
		throws IOException
	{
		return getTriples(subj, pred, obj, context, true, false);
	}

	public BTreeIterator getTriples(int subj, int pred, int obj, int context, boolean explicit)
		throws IOException
	{
		return getTriples(subj, pred, obj, context, explicit, true);
	}

	private BTreeIterator getTriples(int subj, int pred, int obj, int context, boolean explicit,
			boolean matchExplicitFlag)
		throws IOException
	{
		// Get best matching index
		int bestScore = -1;
		TripleIndex bestIndex = null;
		for (int i = 0; i < indexes.length; i++) {
			int score = indexes[i].getPatternScore(subj, pred, obj, context);
			if (score > bestScore) {
				bestScore = score;
				bestIndex = indexes[i];
			}
		}

		byte[] searchKey = getSearchKey(subj, pred, obj, context, explicit);
		byte[] searchMask = getSearchMask(subj, pred, obj, context, matchExplicitFlag);

		if (bestScore > 0) {
			// Use ranged search
			byte[] minValue = getMinValue(subj, pred, obj, context);
			byte[] maxValue = getMaxValue(subj, pred, obj, context);

			// logger.debug("using " + bestIndex.getFieldSeq() + " index with a
			// score of " +
			// bestScore + " for pattern (" + subj + ", " + pred + ", " + obj +
			// ")");

			return bestIndex.getBTree().iterateValues(searchKey, searchMask, minValue, maxValue);
		}
		else {
			// logger.debug("using sequential search for pattern (" + subj + ", " +
			// pred + ", " + obj + ")");
			// Use sequential scan
			return bestIndex.getBTree().iterateValues(searchKey, searchMask);
		}
	}

	public long countTriples(int subj, int pred, int obj, int context)
		throws IOException
	{
		return count(getTriples(subj, pred, obj, context));
	}

	public long countTriples(int subj, int pred, int obj, int context, boolean explicit)
		throws IOException
	{
		return count(getTriples(subj, pred, obj, context, explicit));
	}

	private long count(BTreeIterator iter)
		throws IOException
	{
		long count = 0L;

		try {
			while (iter.next() != null) {
				count++;
			}
		}
		finally {
			iter.close();
		}

		return count;
	}

	public void sync()
		throws IOException
	{
		for (int i = 0; i < indexes.length; i++) {
			indexes[i].getBTree().sync();
		}
	}

	public void clear()
		throws IOException
	{
		for (int i = 0; i < indexes.length; i++) {
			indexes[i].getBTree().clear();
		}
	}

	public byte[] storeTriple(int subj, int pred, int obj, int context)
		throws IOException
	{
		return storeTriple(subj, pred, obj, context, true);
	}

	public byte[] storeTriple(int subj, int pred, int obj, int context, boolean explicit)
		throws IOException
	{
		byte[] data = getData(subj, pred, obj, context, explicit);

		byte[] oldData = indexes[0].getBTree().insert(data);

		if (oldData == null || !Arrays.equals(data, oldData)) {
			// new or changed data was inserted into the first index,
			// also insert it into the other indexes
			for (int i = 1; i < indexes.length; i++) {
				indexes[i].getBTree().insert(data);
			}
		}

		return oldData;
	}

	public int removeTriples(int subj, int pred, int obj, int context)
		throws IOException
	{
		BTreeIterator iter = getTriples(subj, pred, obj, context);
		try {
			return removeTriples(iter);
		}
		finally {
			iter.close();
		}
	}

	/**
	 * @param subj
	 *        The subject for the pattern, or <tt>-1</tt> for a wildcard.
	 * @param pred
	 *        The predicate for the pattern, or <tt>-1</tt> for a wildcard.
	 * @param obj
	 *        The object for the pattern, or <tt>-1</tt> for a wildcard.
	 * @param context
	 *        The context for the pattern, or <tt>-1</tt> for a wildcard.
	 * @param explicit
	 *        Flag indicating whether explicit or inferred statements should be
	 *        removed; <tt>true</tt> removes explicit statements that match the
	 *        pattern, <tt>false</tt> removes inferred statements that match
	 *        the pattern.
	 * @return The number of triples that were removed.
	 * @throws IOException
	 */
	public int removeTriples(int subj, int pred, int obj, int context, boolean explicit)
		throws IOException
	{
		BTreeIterator iter = getTriples(subj, pred, obj, context, explicit);
		try {
			return removeTriples(iter);
		}
		finally {
			iter.close();
		}
	}

	private int removeTriples(BTreeIterator iter)
		throws IOException
	{
		// FIXME: naive implementation below
		ArrayList<byte[]> removeList = new ArrayList<byte[]>();

		byte[] value = null;
		while ((value = iter.next()) != null) {
			removeList.add(value);
		}

		for (TripleIndex index : indexes) {
			BTree btree = index.getBTree();

			for (byte[] v : removeList) {
				btree.remove(v);
			}
		}

		return removeList.size();
	}

	private byte[] getData(int subj, int pred, int obj, int context, boolean explicit) {
		byte[] data = new byte[RECORD_LENGTH];

		ByteArrayUtil.putInt(subj, data, SUBJ_IDX);
		ByteArrayUtil.putInt(pred, data, PRED_IDX);
		ByteArrayUtil.putInt(obj, data, OBJ_IDX);
		ByteArrayUtil.putInt(context, data, CONTEXT_IDX);
		if (explicit) {
			data[FLAG_IDX] |= EXPLICIT_FLAG;
		}

		return data;
	}

	private byte[] getSearchKey(int subj, int pred, int obj, int context, boolean explicit) {
		return getData(subj, pred, obj, context, explicit);
	}

	private byte[] getSearchMask(int subj, int pred, int obj, int context, boolean checkExplicitFlag) {
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
		if (checkExplicitFlag) {
			mask[FLAG_IDX] |= EXPLICIT_FLAG;
		}

		return mask;
	}

	private byte[] getMinValue(int subj, int pred, int obj, int context) {
		byte[] minValue = new byte[RECORD_LENGTH];

		ByteArrayUtil.putInt((subj == -1 ? 0x00000000 : subj), minValue, SUBJ_IDX);
		ByteArrayUtil.putInt((pred == -1 ? 0x00000000 : pred), minValue, PRED_IDX);
		ByteArrayUtil.putInt((obj == -1 ? 0x00000000 : obj), minValue, OBJ_IDX);
		ByteArrayUtil.putInt((context == -1 ? 0x00000000 : context), minValue, CONTEXT_IDX);
		minValue[FLAG_IDX] = (byte)0;

		return minValue;
	}

	private byte[] getMaxValue(int subj, int pred, int obj, int context) {
		byte[] maxValue = new byte[RECORD_LENGTH];

		ByteArrayUtil.putInt((subj == -1 ? 0xffffffff : subj), maxValue, SUBJ_IDX);
		ByteArrayUtil.putInt((pred == -1 ? 0xffffffff : pred), maxValue, PRED_IDX);
		ByteArrayUtil.putInt((obj == -1 ? 0xffffffff : obj), maxValue, OBJ_IDX);
		ByteArrayUtil.putInt((context == -1 ? 0xffffffff : context), maxValue, CONTEXT_IDX);
		maxValue[FLAG_IDX] = (byte)0xff;

		return maxValue;
	}

	private File getIndexFile(String fieldSeq) {
		return new File(dir, "triples-" + fieldSeq + ".dat");
	}

	private void loadProperties(File propFile)
		throws IOException
	{
		InputStream in = new FileInputStream(propFile);
		try {
			properties.clear();
			properties.load(in);
		}
		finally {
			in.close();
		}
	}

	private void storeProperties(File propFile)
		throws IOException
	{
		OutputStream out = new FileOutputStream(propFile);
		try {
			properties.store(out, "triple indexes meta-data, DO NOT EDIT!");
		}
		finally {
			out.close();
		}
	}

	/*-------------------------*
	 * Inner class TripleIndex *
	 *-------------------------*/

	private class TripleIndex {

		private TripleComparator tripleComparator;

		private BTree btree;

		public TripleIndex(String fieldSeq)
			throws IOException
		{
			tripleComparator = new TripleComparator(fieldSeq);
			File btreeFile = getIndexFile(fieldSeq);
			btree = new BTree(btreeFile, 2048, RECORD_LENGTH, tripleComparator, forceSync);
		}

		public char[] getFieldSeq() {
			return tripleComparator.getFieldSeq();
		}

		public File getFile() {
			return btree.getFile();
		}

		public BTree getBTree() {
			return btree;
		}

		/**
		 * Determines the 'score' of this index on the supplied pattern of
		 * subject, predicate, object and context IDs. The higher the score, the
		 * better the index is suited for matching the pattern. Lowest score is 0,
		 * which means that the index will perform a sequential scan.
		 */
		public int getPatternScore(int subj, int pred, int obj, int context) {
			int score = 0;

			for (char field : tripleComparator.getFieldSeq()) {
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

		private char[] fieldSeq;

		public TripleComparator(String fieldSeq) {
			this.fieldSeq = fieldSeq.toCharArray();
		}

		public char[] getFieldSeq() {
			return fieldSeq;
		}

		// implements BTreeValueComparator.compareBTreeValues()
		public final int compareBTreeValues(byte[] key, byte[] data, int offset, int length) {
			for (char field : fieldSeq) {
				int fieldIdx = 0;

				switch (field) {
					case 's':
						fieldIdx = SUBJ_IDX;
						break;
					case 'p':
						fieldIdx = PRED_IDX;
						break;
					case 'o':
						fieldIdx = OBJ_IDX;
						break;
					case 'c':
						fieldIdx = CONTEXT_IDX;
						break;
					default:
						throw new IllegalArgumentException("invalid character '" + field + "' in field sequence: "
								+ new String(fieldSeq));
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
