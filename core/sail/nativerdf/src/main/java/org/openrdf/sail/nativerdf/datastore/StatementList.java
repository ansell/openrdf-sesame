/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.nativerdf.datastore;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.iteration.LookAheadIteration;
import info.aduna.iteration.UnionIteration;

/**
 * Buffered Disk-based list of statements pending add/remove.
 *
 * @author James Leigh
 */
public class StatementList implements Closeable {

	private static final int BUF_SIZE = 512;

	private final List<int[]> buffer = new ArrayList<int[]>(BUF_SIZE);

	long flushed;

	File disk;

	private ObjectOutputStream out;

	@Override
	public void close()
		throws IOException
	{
		if (out != null) {
			out.close();
			out = null;
		}
		if (disk != null) {
			disk.delete();
			flushed = 0;
		}
		buffer.clear();
	}

	public long size() {
		return flushed + buffer.size();
	}

	public void add(int subj, int pred, int obj, int context)
		throws IOException
	{
		if (buffer.size() >= BUF_SIZE) {
			flush();
		}
		buffer.add(new int[] { subj, pred, obj, context });
	}

	private void flush()
		throws IOException
	{
		if (disk == null) {
			disk = File.createTempFile("sesame-operation", ".dat");
		}
		if (out == null) {
			out = new ObjectOutputStream(new FileOutputStream(disk, true));
		}
		for (int[] st : buffer) {
			out.writeObject(st);
			flushed++;
		}
		buffer.clear();
	}

	public CloseableIteration<int[], IOException> iteration()
		throws IOException
	{
		if (out != null) {
			out.close();
			out = null;
		}
		CloseableIteration<int[], IOException> iter;
		iter = new CloseableIteratorIteration<int[], IOException>(buffer.iterator());
		if (disk == null)
			return iter;
		LookAheadIteration<int[], IOException> head = new LookAheadIteration<int[], IOException>() {

			private final ObjectInputStream in = new ObjectInputStream(new FileInputStream(disk));

			private long count;

			@Override
			protected void handleClose()
				throws IOException
			{
				in.close();
			}

			@Override
			protected int[] getNextElement()
				throws IOException
			{
				try {
					if (++count > flushed)
						return null;
					return (int[])in.readObject();
				}
				catch (ClassNotFoundException e) {
					throw new AssertionError(e);
				}
			}
		};
		return new UnionIteration<int[], IOException>(head, iter);
	}

}
