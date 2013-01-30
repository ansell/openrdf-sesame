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
package org.openrdf.sail.rdbms.managers.helpers;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.openrdf.sail.rdbms.schema.Batch;

/**
 * 
 * @author James Leigh
 */
public class BatchBlockingQueue extends AbstractQueue<Batch> implements
		BlockingQueue<org.openrdf.sail.rdbms.schema.Batch>
{

	private LinkedHashSet<Batch> queue;

	private int capacity;

	private int size;

	public BatchBlockingQueue(int capacity) {
		queue = new LinkedHashSet<Batch>(capacity / 16);
		this.capacity = capacity;
	}

	@Override
	public boolean remove(Object o) {
		synchronized (queue) {
			if (queue.remove(o)) {
				size -= ((Batch)o).size();
				queue.notify();
				return true;
			}
			return false;
		}
	}

	@Override
	public Iterator<Batch> iterator() {
		synchronized (queue) {
			Batch[] array = queue.toArray(new Batch[queue.size()]);
			return Arrays.asList(array).iterator();
		}
	}

	@Override
	public int size() {
		return queue.size();
	}

	public boolean offer(Batch e) {
		synchronized (queue) {
			boolean added = queue.add(e);
			size += e.size();
			queue.notify();
			return added;
		}
	}

	public Batch peek() {
		synchronized (queue) {
			return queue.iterator().next();
		}
	}

	public Batch poll() {
		synchronized (queue) {
			Iterator<Batch> iter = queue.iterator();
			if (iter.hasNext()) {
				Batch e = iter.next();
				iter.remove();
				size -= e.size();
				queue.notify();
				return e;
			}
			return null;
		}
	}

	public int drainTo(Collection<? super Batch> c) {
		synchronized (queue) {
			return drainTo(c, queue.size());
		}
	}

	public int drainTo(Collection<? super Batch> c, int n) {
		synchronized (queue) {
			Iterator<Batch> iter = queue.iterator();
			int i;
			for (i = 0; i < n && iter.hasNext(); i++) {
				Batch next = iter.next();
				c.add(next);
				iter.remove();
				size -= next.size();
				queue.notify();
			}
			return i;
		}
	}

	public boolean offer(Batch e, long timeout, TimeUnit unit)
		throws InterruptedException
	{
		return offer(e);
	}

	public Batch poll(long timeout, TimeUnit unit)
		throws InterruptedException
	{
		return poll();
	}

	public void put(Batch e)
		throws InterruptedException
	{
		synchronized (queue) {
			while (size >= capacity) {
				queue.wait();
			}
			queue.add(e);
			size += e.size();
			queue.notify();
		}
	}

	public int remainingCapacity() {
		return Integer.MAX_VALUE;
	}

	public Batch take()
		throws InterruptedException
	{
		synchronized (queue) {
			while (queue.isEmpty()) {
				queue.wait();
			}
			Iterator<Batch> iter = queue.iterator();
			Batch e = iter.next();
			iter.remove();
			size -= e.size();
			queue.notify();
			return e;
		}
	}
}
