package org.openrdf.sail.spin;

import org.openrdf.OpenRDFException;


public class SPINConstraintViolationException extends OpenRDFException {

	private static final long serialVersionUID = 2208275585538203176L;

	private final String violationRoot;
	private final String violationPath;
	private final String violationValue;
	private final SPINConstraintViolationLevel violationLevel;

	public SPINConstraintViolationException(String message, String violationRoot, String violationPath, String violationValue, SPINConstraintViolationLevel violationLevel) {
		super(message);
		this.violationRoot = violationRoot;
		this.violationPath = violationPath;
		this.violationValue = violationValue;
		this.violationLevel = violationLevel;
	}

	public String getViolationRoot() {
		return violationRoot;
	}

	public String getViolationPath() {
		return violationPath;
	}

	public String getViolationValue() {
		return violationValue;
	}

	public SPINConstraintViolationLevel getViolationLevel() {
		return violationLevel;
	}
}
