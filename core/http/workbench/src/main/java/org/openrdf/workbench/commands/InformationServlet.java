/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import java.io.PrintWriter;

import org.openrdf.repository.RepositoryException;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;

public class InformationServlet extends TransformationServlet {

	@Override
	public void service(final PrintWriter out, final String xslPath)
		throws RepositoryException
	{
		final TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.transform(xslPath, "information.xsl");
		builder.start("version", "os", "jvm", "user", "memory-used", "maximum-memory");
		builder.link("info");
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
