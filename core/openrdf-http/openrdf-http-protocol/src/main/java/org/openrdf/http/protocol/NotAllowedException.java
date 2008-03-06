package org.openrdf.http.protocol;

import org.openrdf.repository.RepositoryException;

/**
 * @author Herko ter Horst
 */
public class NotAllowedException extends RepositoryException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1753268902269152319L;

	public NotAllowedException() {
		// TODO Auto-generated constructor stub
	}

	public NotAllowedException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

	public NotAllowedException(Throwable t) {
		super(t);
		// TODO Auto-generated constructor stub
	}

	public NotAllowedException(String msg, Throwable t) {
		super(msg, t);
		// TODO Auto-generated constructor stub
	}

}
