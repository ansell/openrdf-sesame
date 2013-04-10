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
package org.openrdf.repository.event;

import java.nio.file.Path;
import java.util.EventListener;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

/**
 * Interceptor interface for Repository state changes.
 * 
 * @author Herko ter Horst
 */
public interface RepositoryInterceptor extends EventListener {

	public abstract boolean getConnection(Repository repo, RepositoryConnection conn);

	public abstract boolean initialize(Repository repo);

	public abstract boolean setDataDir(Repository repo, Path dataDir);

	public abstract boolean shutDown(Repository repo);

}
