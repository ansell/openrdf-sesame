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
package org.openrdf.repository.sail;

import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.ParserConfig;

/**
 * @author Jeen Broekstra
 */
public class SailUpdate extends AbstractSailUpdate {

	private final SailRepositoryConnection con;

	protected SailUpdate(ParsedUpdate parsedUpdate, SailRepositoryConnection con) {
		super(parsedUpdate, con.getSailConnection(), con.getValueFactory());
		this.con = con;
	}

	protected SailRepositoryConnection getConnection() {
		return con;
	}

	@Override
	protected ParserConfig getParserConfig() {
		return getConnection().getParserConfig();
	}

	@Override
	protected boolean isLocalTransaction()
		throws RepositoryException
	{
		return !getConnection().isActive();
	}

	@Override
	protected void beginLocalTransaction()
		throws RepositoryException
	{
		getConnection().begin();
	}

	@Override
	protected void commitLocalTransaction()
		throws RepositoryException
	{
		getConnection().commit();
	}
}
