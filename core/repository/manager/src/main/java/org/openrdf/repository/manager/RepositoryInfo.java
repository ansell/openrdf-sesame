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
package org.openrdf.repository.manager;

import java.net.URL;
import java.text.Collator;

public class RepositoryInfo implements Comparable<RepositoryInfo> {

	private String id;

	private URL location;

	private String description;

	private boolean readable;

	private boolean writable;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public URL getLocation() {
		return location;
	}

	public void setLocation(URL location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isReadable() {
		return readable;
	}

	public void setReadable(boolean readable) {
		this.readable = readable;
	}

	public boolean isWritable() {
		return writable;
	}

	public void setWritable(boolean writable) {
		this.writable = writable;
	}

	public int compareTo(RepositoryInfo o) {
		int result = Collator.getInstance().compare(getDescription(), o.getDescription());
		if (result == 0) {
			result = getId().compareTo(o.getId());
		}
		return result;
	}
}
