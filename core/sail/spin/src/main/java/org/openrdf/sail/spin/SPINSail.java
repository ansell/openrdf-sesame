package org.openrdf.sail.spin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.sail.helpers.RDFSailInserter;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailWrapper;
import org.openrdf.sail.inferencer.InferencerConnection;

public class SPINSail extends NotifyingSailWrapper
{
	public SPINSail() {
	}

	public SPINSail(NotifyingSail baseSail) {
		super(baseSail);
	}

	@Override
	public NotifyingSailConnection getConnection() throws SailException {
		try {
			InferencerConnection con = (InferencerConnection)super.getConnection();
			return new SPINSailConnection(con);
		}
		catch (ClassCastException e) {
			throw new SailException(e.getMessage(), e);
		}
	}

	@Override
	public void initialize() throws SailException {
		super.initialize();
		RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
		SailConnection con = getConnection();
		try {
			con.begin();
			load(parser, "/schema/sp.ttl", con);
			load(parser, "/schema/spin.ttl", con);
			load(parser, "/schema/spl.spin.ttl", con);
			con.commit();
		}
		catch(OpenRDFException e) {
			con.rollback();
			throw new SailException(e.getMessage(), e);
		}
		catch(IOException e) {
			con.rollback();
			throw new SailException(e.getMessage(), e);
		}
		finally {
			con.close();
		}
	}

	private void load(RDFParser parser, String file, SailConnection con) throws OpenRDFException, IOException {
		RDFSailInserter inserter = new RDFSailInserter(con, getValueFactory());
		parser.setRDFHandler(inserter);
		URL url = getClass().getResource(file);
		InputStream in = url.openStream();
		try {
			parser.parse(in, url.toString());
		}
		finally {
			in.close();
		}
	}
}
