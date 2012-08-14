/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

import org.openrdf.repository.RepositoryException;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InformationServlet extends TransformationServlet {
	private static final String POM_PROPERTIES = "/META-INF/maven/org.openrdf.sesame/sesame-http-workbench/pom.properties";

	private Logger logger = LoggerFactory.getLogger(InformationServlet.class);

	@Override
	public void service(PrintWriter out, String xslPath)
			throws RepositoryException {
		TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.transform(xslPath, "information.xsl");
		builder.start("version", "os", "jvm", "user", "memory-used",
				"maximum-memory");
		builder.link("info");
		String version = getVersion();
		String os = getOsName();
		String jvm = getJvmName();
		String user = System.getProperty("user.name");
		long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();
		String used = ((total - free) / 1024 / 1024) + " MB";
		String max = (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB";
		builder.result(version, os, jvm, user, used, max);
		builder.end();
	}

	private String getVersion() {
		InputStream in = config.getServletContext().getResourceAsStream(POM_PROPERTIES);
		if (in == null)
			return null;
		Properties pom = new Properties();
		try {
			pom.load(in);
		} catch (IOException e) {
			logger.error(e.toString(), e);
			return null;
		}
		String version = (String) pom.get("version");
		return version;
	}

	private String getOsName() {
		StringBuilder sb = new StringBuilder();
		sb.append(System.getProperty("os.name")).append(" ");
		sb.append(System.getProperty("os.version")).append(" (");
		sb.append(System.getProperty("os.arch")).append(")");
		return sb.toString();
	}

	private String getJvmName() {
		StringBuilder sb = new StringBuilder();
		sb.append(System.getProperty("java.vm.vendor")).append(" ");
		sb.append(System.getProperty("java.vm.name")).append(" (");
		sb.append(System.getProperty("java.version")).append(")");
		return sb.toString();
	}

}
