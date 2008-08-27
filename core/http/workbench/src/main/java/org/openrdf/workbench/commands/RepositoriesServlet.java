/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import java.io.PrintWriter;

import org.openrdf.StoreException;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;

public class RepositoriesServlet extends TransformationServlet {

	@Override
	public void service(PrintWriter out, String xslPath)
			throws StoreException {
		TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.transform(xslPath, "repositories.xsl");
		builder.start("readable", "writeable", "id", "description", "location");
		builder.link("info");
		for (RepositoryInfo info : manager.getAllRepositoryInfos()) {
			builder.result(info.isReadable(), info.isWritable(), info.getId(),
					info.getDescription(), info.getLocation());
		}
		builder.end();
	}

}
