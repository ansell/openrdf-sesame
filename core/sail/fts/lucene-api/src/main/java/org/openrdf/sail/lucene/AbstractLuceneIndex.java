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
package org.openrdf.sail.lucene;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import com.spatial4j.core.context.SpatialContext;

public abstract class AbstractLuceneIndex extends AbstractSearchIndex {

	/**
	 * keep a lit of old monitors that are still iterating but not closed (open
	 * iterators), will be all closed on shutdown items are removed from list by
	 * ReaderMnitor.endReading() when closing
	 */
	protected final Collection<AbstractReaderMonitor> oldmonitors = new LinkedList<AbstractReaderMonitor>();

	protected abstract AbstractReaderMonitor getCurrentMonitor();

	protected AbstractLuceneIndex() {}

	protected AbstractLuceneIndex(SpatialContext geoContext)
	{
		super(geoContext);
	}

	@Override
	public void beginReading()
	{
		getCurrentMonitor().beginReading();
	}

	@Override
	public void endReading() throws IOException
	{
		getCurrentMonitor().endReading();
	}

	public Collection<AbstractReaderMonitor> getOldMonitors()
	{
		return oldmonitors;
	}
}
