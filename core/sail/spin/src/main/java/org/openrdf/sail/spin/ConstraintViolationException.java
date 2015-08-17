package org.openrdf.sail.spin;

import org.openrdf.sail.SailException;
import org.openrdf.spin.ConstraintViolation;


public class ConstraintViolationException extends SailException {

	private static final long serialVersionUID = 2208275585538203176L;

	private final ConstraintViolation violation;

	public ConstraintViolationException(ConstraintViolation violation) {
		super(violation.getMessage());
		this.violation = violation;
	}

	public ConstraintViolation getConstraintViolation() {
		return violation;
	}
}
