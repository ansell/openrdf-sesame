package org.openrdf.sail.spin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.fc.AbstractForwardChainingInferencerConnection;
import org.openrdf.sail.inferencer.util.RDFInferencerInserter;

public class SPINSailConnection extends AbstractForwardChainingInferencerConnection {
	private final ValueFactory vf;

	public SPINSailConnection(Sail sail, InferencerConnection con) {
		super(sail, con);
		this.vf = sail.getValueFactory();
	}

	@Override
	protected Model createModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void addAxiomStatements() throws SailException {

		RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
		load(parser, "/schema/sp.ttl", this);
		load(parser, "/schema/spin.ttl", this);
		load(parser, "/schema/spl.spin.ttl", this);
	}

	private void load(RDFParser parser, String file, InferencerConnection con) throws SailException {
		RDFInferencerInserter inserter = new RDFInferencerInserter(con, vf);
		parser.setRDFHandler(inserter);
		URL url = getClass().getResource(file);
		try
		{
			InputStream in = url.openStream();
			try {
				parser.parse(in, url.toString());
			}
			finally {
				in.close();
			}
		} catch(IOException ioe) {
			throw new SailException(ioe);
		} catch(OpenRDFException e) {
			throw new SailException(e);
		}
	}

	/**
	 * update spin:rules modify existing (non-inferred) statements directly.
	 * spin:constructors should be run after spin:rules for each subject of an RDF.TYPE statement.
	 */
	@Override
	protected int applyRules(Model iteration) throws SailException {
		// TODO Auto-generated method stub
		return 0;
	}

}
