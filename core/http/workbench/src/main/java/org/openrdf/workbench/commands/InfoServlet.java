package org.openrdf.workbench.commands;

import static org.openrdf.query.parser.QueryParserRegistry.getInstance;

import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.openrdf.query.parser.QueryParserFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;

public class InfoServlet extends TransformationServlet {

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	public void service(PrintWriter out, String xslPath)
			throws RepositoryException {
		TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.start("id", "description", "location", "server",
				"upload-format", "query-format", "download-format");
		String id = info.getId();
		String desc = info.getDescription();
		URL loc = info.getLocation();
		String server = getServer();
		builder.result(id, desc, loc, server);
		for (RDFParserFactory parser : RDFParserRegistry.getInstance().getAll()) {
			String mimeType = parser.getRDFFormat().getDefaultMIMEType();
			String name = parser.getRDFFormat().getName();
			builder.binding("upload-format", mimeType + " " + name);
		}
		for (QueryParserFactory factory : getInstance().getAll()) {
			String name = factory.getQueryLanguage().getName();
			builder.binding("query-format", name + " " + name);
		}
		for (RDFWriterFactory writer : RDFWriterRegistry.getInstance().getAll()) {
			String mimeType = writer.getRDFFormat().getDefaultMIMEType();
			String name = writer.getRDFFormat().getName();
			builder.binding("download-format", mimeType + " " + name);
		}
		builder.end();
	}

	private String getServer() {
		if (manager instanceof LocalRepositoryManager) {
			return ((LocalRepositoryManager) manager).getBaseDir().toString();
		} else if (manager instanceof RemoteRepositoryManager) {
			return ((RemoteRepositoryManager) manager).getServerURL();
		}
		return null;
	}

}
