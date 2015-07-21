package org.openrdf.sail.spin;

import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.fc.AbstractForwardChainingInferencer;

public class SPINSail extends AbstractForwardChainingInferencer
{
	public SPINSail() {
	}

	public SPINSail(NotifyingSail baseSail) {
		super(baseSail);
	}

	@Override
	public SPINSailConnection getConnection() throws SailException {
		try {
			InferencerConnection con = (InferencerConnection)super.getConnection();
			return new SPINSailConnection(this, con);
		}
		catch (ClassCastException e) {
			throw new SailException(e.getMessage(), e);
		}
	}

	@Override
	public void initialize() throws SailException {
		super.initialize();

		SPINSailConnection con = getConnection();
		try {
			con.begin();
			con.addAxiomStatements();
			con.commit();
		}
		finally {
			con.close();
		}
	}
}
