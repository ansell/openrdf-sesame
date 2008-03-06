/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import info.aduna.io.IOUtil;

import org.openrdf.model.Namespace;
import org.openrdf.model.impl.NamespaceImpl;

/**
 * An in-memory store for namespace prefix information that uses a file for
 * persistence. Namespaces are encoded in the file as records as follows:
 * 
 * <pre>
 *   byte 1 - 2     : the length of the encoded namespace name
 *   byte 3 - A     : the UTF-8 encoded namespace name
 *   byte A+1 - A+2 : the length of the encoded namespace prefix
 *   byte A+3 - end : the UTF-8 encoded namespace prefix
 * </pre>
 */
class NamespaceStore implements Iterable<NamespaceImpl> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final String FILE_NAME = "namespaces.dat";

	/**
	 * Magic number "Native Namespace File" to detect whether the file is
	 * actually a namespace file. The first three bytes of the file should be
	 * equal to this magic number.
	 */
	private static final byte[] MAGIC_NUMBER = new byte[] { 'n', 'n', 'f' };

	/**
	 * File format version, stored as the fourth byte in namespace files.
	 */
	private static final byte FILE_FORMAT_VERSION = 1;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The data file for this NamespaceStore.
	 */
	private File _file;

	/**
	 * Map storing namespace information by their prefix.
	 */
	private Map<String, NamespaceImpl> _namespacesMap;

	/**
	 * Flag indicating whether the contents of this NamespaceStore are different
	 * from what is stored on disk.
	 */
	private boolean _contentsChanged;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NamespaceStore(File dataDir)
		throws IOException
	{
		_file = new File(dataDir, FILE_NAME);

		_namespacesMap = new LinkedHashMap<String, NamespaceImpl>(16);

		if (_file.exists()) {
			_readNamespacesFromFile();
		}
		else {
			// Make sure the file exists
			_writeNamespacesToFile();
		}

		_contentsChanged = false;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getNamespace(String prefix) {
		String result = null;
		NamespaceImpl namespace = _namespacesMap.get(prefix);
		if (namespace != null) {
			result = namespace.getName();
		}
		return result;
	}

	public void setNamespace(String prefix, String name) {
		NamespaceImpl ns = _namespacesMap.get(prefix);

		if (ns != null) {
			if (!ns.getName().equals(name)) {
				ns.setName(name);
				_contentsChanged = true;
			}
		}
		else {
			_namespacesMap.put(prefix, new NamespaceImpl(prefix, name));
			_contentsChanged = true;
		}
	}

	public void removeNamespace(String prefix) {
		NamespaceImpl ns = _namespacesMap.remove(prefix);

		if (ns != null) {
			_contentsChanged = true;
		}
	}

	public Iterator<NamespaceImpl> iterator() {
		return _namespacesMap.values().iterator();
	}

	public void clear() {
		if (!_namespacesMap.isEmpty()) {
			_namespacesMap.clear();
			_contentsChanged = true;
		}
	}

	public void sync()
		throws IOException
	{
		if (_contentsChanged) {
			// Flush the changes to disk
			_writeNamespacesToFile();
			_contentsChanged = false;
		}
	}

	public void close() {
		_namespacesMap = null;
		_file = null;
	}

	/*----------*
	 * File I/O *
	 *----------*/

	private void _writeNamespacesToFile()
		throws IOException
	{
		synchronized (_file) {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(_file));

			try {
				out.write(MAGIC_NUMBER);
				out.writeByte(FILE_FORMAT_VERSION);

				for (Namespace ns : _namespacesMap.values()) {
					out.writeUTF(ns.getName());
					out.writeUTF(ns.getPrefix());
				}
			}
			finally {
				out.close();
			}
		}
	}

	private void _readNamespacesFromFile()
		throws IOException
	{
		synchronized (_file) {
			DataInputStream in = new DataInputStream(new FileInputStream(_file));

			try {
				byte[] magicNumber = IOUtil.readBytes(in, MAGIC_NUMBER.length);
				if (!Arrays.equals(magicNumber, MAGIC_NUMBER)) {
					throw new IOException("File doesn't contain compatible namespace data");
				}

				byte version = in.readByte();
				if (version > FILE_FORMAT_VERSION) {
					throw new IOException("Unable to read namespace file; it uses a newer file format");
				}
				else if (version != FILE_FORMAT_VERSION) {
					throw new IOException("Unable to read namespace file; invalid file format version: " + version);
				}

				while (true) {
					try {
						String name = in.readUTF();
						String prefix = in.readUTF();

						NamespaceImpl ns = new NamespaceImpl(prefix, name);
						_namespacesMap.put(name, ns);
					}
					catch (EOFException e) {
						break;
					}
				}
			}
			finally {
				in.close();
			}
		}
	}

	/*-------------------*
	 * Debugging methods *
	 *-------------------*/

	public static void main(String[] args)
		throws Exception
	{
		NamespaceStore nsStore = new NamespaceStore(new File(args[0]));

		for (Namespace ns : nsStore) {
			System.out.println(ns.getPrefix() + " = " + ns.getName());
		}
	}
}
