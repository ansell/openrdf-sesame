/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import info.aduna.io.IOUtil;
import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.model.MemResource;
import org.openrdf.sail.memory.model.MemStatement;
import org.openrdf.sail.memory.model.MemURI;
import org.openrdf.sail.memory.model.MemValue;
import org.openrdf.sail.memory.model.ReadMode;
import org.openrdf.sail.memory.model.TxnStatus;

/**
 * Functionality to read and write MemoryStore to/from a file.
 * 
 * @author Arjohn Kampman
 */
class FileIO {

	/** Magic number for Binary Memory Store Files */
	private static final byte[] MAGIC_NUMBER = new byte[] { 'B', 'M', 'S', 'F' };

	/** The version number of the current format. */
	private static final int BMSF_VERSION = 1;

	/* RECORD TYPES */
	public static final int NAMESPACE_MARKER = 1;

	public static final int EXPL_TRIPLE_MARKER = 2;

	public static final int EXPL_QUAD_MARKER = 3;

	public static final int INF_TRIPLE_MARKER = 4;

	public static final int INF_QUAD_MARKER = 5;

	public static final int URI_MARKER = 6;

	public static final int BNODE_MARKER = 7;

	public static final int PLAIN_LITERAL_MARKER = 8;

	public static final int LANG_LITERAL_MARKER = 9;

	public static final int DATATYPE_LITERAL_MARKER = 10;

	public static final int EOF_MARKER = 127;

	public static void write(MemoryStore store, File dataFile)
		throws IOException, SailException
	{
		OutputStream out = new FileOutputStream(dataFile);
		try {
			// Write header
			out.write(MAGIC_NUMBER);
			out.write(BMSF_VERSION);

			// The rest of the data is GZIP-compressed
			DataOutputStream dataOut = new DataOutputStream(new GZIPOutputStream(out));
			out = dataOut;

			writeNamespaces(store, dataOut);

			writeStatements(store, dataOut);

			dataOut.writeByte(EOF_MARKER);
		}
		finally {
			out.close();
		}
	}

	public static void read(MemoryStore store, File dataFile)
		throws IOException
	{
		InputStream in = new FileInputStream(dataFile);
		try {
			byte[] magicNumber = IOUtil.readBytes(in, MAGIC_NUMBER.length);
			if (!Arrays.equals(magicNumber, MAGIC_NUMBER)) {
				throw new IOException("File is not a binary MemoryStore file");
			}

			int version = in.read();
			if (version != BMSF_VERSION) {
				throw new IOException("Incompatible format version: " + version);
			}

			// The rest of the data is GZIP-compressed
			DataInputStream dataIn = new DataInputStream(new GZIPInputStream(in));
			in = dataIn;

			int recordTypeMarker;
			while ((recordTypeMarker = dataIn.readByte()) != EOF_MARKER) {
				switch (recordTypeMarker) {
					case NAMESPACE_MARKER:
						readNamespace(store, dataIn);
						break;
					case EXPL_TRIPLE_MARKER:
						readStatement(store, false, true, dataIn);
						break;
					case EXPL_QUAD_MARKER:
						readStatement(store, true, true, dataIn);
						break;
					case INF_TRIPLE_MARKER:
						readStatement(store, false, false, dataIn);
						break;
					case INF_QUAD_MARKER:
						readStatement(store, true, false, dataIn);
						break;
					default:
						throw new IOException("Invalid record type marker: " + recordTypeMarker);
				}
			}
		}
		finally {
			in.close();
		}
	}

	private static void writeNamespaces(MemoryStore store, DataOutputStream dataOut)
		throws IOException
	{
		for (Namespace ns : store.getNamespaceStore()) {
			dataOut.writeByte(NAMESPACE_MARKER);
			dataOut.writeUTF(ns.getPrefix());
			dataOut.writeUTF(ns.getName());

			// FIXME dummy boolean to be compatible with older version:
			// the up-to-date status is no longer relevant
			dataOut.writeBoolean(true);
		}
	}

	private static void readNamespace(MemoryStore store, DataInputStream dataIn)
		throws IOException
	{
		String prefix = dataIn.readUTF();
		String name = dataIn.readUTF();

		// FIXME dummy boolean to be compatible with older version:
		// the up-to-date status is no longer relevant
		dataIn.readBoolean();

		store.getNamespaceStore().setNamespace(prefix, name);
	}

	private static void writeStatements(MemoryStore store, DataOutputStream dataOut)
		throws IOException, SailException
	{
		CloseableIteration<MemStatement, SailException> stIter = store.createStatementIterator(
				SailException.class, null, null, null, false, store.getCurrentSnapshot(), ReadMode.COMMITTED);

		try {
			while (stIter.hasNext()) {
				MemStatement st = stIter.next();
				Resource context = st.getContext();

				if (st.isExplicit()) {
					if (context == null) {
						dataOut.writeByte(EXPL_TRIPLE_MARKER);
					}
					else {
						dataOut.writeByte(EXPL_QUAD_MARKER);
					}
				}
				else {
					if (context == null) {
						dataOut.writeByte(INF_TRIPLE_MARKER);
					}
					else {
						dataOut.writeByte(INF_QUAD_MARKER);
					}
				}

				writeValue(st.getSubject(), dataOut);
				writeValue(st.getPredicate(), dataOut);
				writeValue(st.getObject(), dataOut);
				if (context != null) {
					writeValue(context, dataOut);
				}
			}
		}
		finally {
			stIter.close();
		}
	}

	private static void readStatement(MemoryStore store, boolean hasContext, boolean isExplicit,
			DataInputStream dataIn)
		throws IOException, ClassCastException
	{
		MemResource memSubj = (MemResource)readValue(store, dataIn);
		MemURI memPred = (MemURI)readValue(store, dataIn);
		MemValue memObj = (MemValue)readValue(store, dataIn);
		MemResource memContext = null;
		if (hasContext) {
			memContext = (MemResource)readValue(store, dataIn);
		}

		MemStatement st = new MemStatement(memSubj, memPred, memObj, memContext, store.getCurrentSnapshot(),
				isExplicit);
		store.getStatements().add(st);
		st.addToComponentLists();
		st.setTxnStatus(TxnStatus.NEUTRAL);
	}

	private static void writeValue(Value value, DataOutputStream dataOut)
		throws IOException
	{
		if (value instanceof URI) {
			dataOut.writeByte(URI_MARKER);
			dataOut.writeUTF(((URI)value).toString());
		}
		else if (value instanceof BNode) {
			dataOut.writeByte(BNODE_MARKER);
			dataOut.writeUTF(((BNode)value).getID());
		}
		else if (value instanceof Literal) {
			Literal lit = (Literal)value;

			String label = lit.getLabel();
			String language = lit.getLanguage();
			URI datatype = lit.getDatatype();

			if (datatype != null) {
				dataOut.writeByte(DATATYPE_LITERAL_MARKER);
				dataOut.writeUTF(label);
				writeValue(datatype, dataOut);
			}
			else if (language != null) {
				dataOut.writeByte(LANG_LITERAL_MARKER);
				dataOut.writeUTF(label);
				dataOut.writeUTF(language);
			}
			else {
				dataOut.writeByte(PLAIN_LITERAL_MARKER);
				dataOut.writeUTF(label);
			}
		}
		else {
			throw new IllegalArgumentException("unexpected value type: " + value.getClass());
		}
	}

	private static Value readValue(MemoryStore store, DataInputStream dataIn)
		throws IOException, ClassCastException
	{
		int valueTypeMarker = dataIn.readByte();

		if (valueTypeMarker == URI_MARKER) {
			String uriString = dataIn.readUTF();
			return store.getValueFactory().createURI(uriString);
		}
		else if (valueTypeMarker == BNODE_MARKER) {
			String bnodeID = dataIn.readUTF();
			return store.getValueFactory().createBNode(bnodeID);
		}
		else if (valueTypeMarker == PLAIN_LITERAL_MARKER) {
			String label = dataIn.readUTF();
			return store.getValueFactory().createLiteral(label);
		}
		else if (valueTypeMarker == LANG_LITERAL_MARKER) {
			String label = dataIn.readUTF();
			String language = dataIn.readUTF();
			return store.getValueFactory().createLiteral(label, language);
		}
		else if (valueTypeMarker == DATATYPE_LITERAL_MARKER) {
			String label = dataIn.readUTF();
			URI datatype = (URI)readValue(store, dataIn);
			return store.getValueFactory().createLiteral(label, datatype);
		}
		else {
			throw new IOException("Invalid value type marker: " + valueTypeMarker);
		}
	}
}
