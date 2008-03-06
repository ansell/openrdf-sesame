package org.openrdf.http.protocol;

import org.openrdf.repository.RepositoryException;


public class UnauthorizedException extends RepositoryException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4322677542795160482L;

	public UnauthorizedException() {
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedException(Throwable t) {
		super(t);
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedException(String msg, Throwable t) {
		super(msg, t);
		// TODO Auto-generated constructor stub
	}

}
