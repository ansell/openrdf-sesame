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
package info.aduna.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A wrapper for an input stream to avoid allowing libraries to close input
 * streams unexpectedly using the {@link #close()} method. Instead, they must be
 * closed by the creator using {@link #doClose()}.
 * 
 * @author Peter Ansell
 */
public class UncloseableInputStream extends FilterInputStream {

	public UncloseableInputStream(InputStream parent) {
		super(parent);
	}

	public void close()
		throws IOException
	{
		// do nothing
	}

	public void doClose()
		throws IOException
	{
		super.close();
	}
}
