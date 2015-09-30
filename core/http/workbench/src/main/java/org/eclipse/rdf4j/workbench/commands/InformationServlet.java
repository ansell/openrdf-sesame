/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.workbench.commands;

import java.util.Arrays;

import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.workbench.base.TransformationServlet;
import org.eclipse.rdf4j.workbench.util.TupleResultBuilder;

public class InformationServlet extends TransformationServlet {

	@Override
	public void service(final TupleResultBuilder builder, final String xslPath)
		throws RepositoryException, QueryResultHandlerException
	{
		// final TupleResultBuilder builder = getTupleResultBuilder(req, resp);
		builder.transform(xslPath, "information.xsl");
		builder.start("version", "os", "jvm", "user", "memory-used", "maximum-memory");
		builder.link(Arrays.asList(INFO));
		final String version = this.appConfig.getVersion().toString();
		final String osName = getOsName();
		final String jvm = getJvmName();
		final String user = System.getProperty("user.name");
		final long total = Runtime.getRuntime().totalMemory();
		final long free = Runtime.getRuntime().freeMemory();
		final String used = ((total - free) / 1024 / 1024) + " MB";
		final String max = (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB";
		builder.result(version, osName, jvm, user, used, max);
		builder.end();
	}

	private String getOsName() {
		final StringBuilder builder = new StringBuilder();
		builder.append(System.getProperty("os.name")).append(" ");
		builder.append(System.getProperty("os.version")).append(" (");
		builder.append(System.getProperty("os.arch")).append(")");
		return builder.toString();
	}

	private String getJvmName() {
		final StringBuilder builder = new StringBuilder();
		builder.append(System.getProperty("java.vm.vendor")).append(" ");
		builder.append(System.getProperty("java.vm.name")).append(" (");
		builder.append(System.getProperty("java.version")).append(")");
		return builder.toString();
	}

}
