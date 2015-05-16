package org.openrdf.sail.spin;

import org.openrdf.model.Statement;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.InferencerConnectionWrapper;

public class SPINSailConnection extends InferencerConnectionWrapper implements SailConnectionListener {

	public SPINSailConnection(InferencerConnection con) {
		super(con);
		con.addConnectionListener(this);
	}

	@Override
	public void statementAdded(Statement st) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void statementRemoved(Statement st) {
		// TODO Auto-generated method stub
		
	}

}
