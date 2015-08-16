package org.openrdf.spin;

import java.io.Serializable;


public class ConstraintViolation implements Serializable {
	private static final long serialVersionUID = 3699022598761641221L;

	private final String message;
	private final String root;
	private final String path;
	private final String value;
	private final ConstraintViolationLevel level;

	public ConstraintViolation(String message, String root, String path, String value, ConstraintViolationLevel level) {
		this.message = message;
		this.root = root;
		this.path = path;
		this.value = value;
		this.level = level;
	}

	public String getMessage() {
		return message;
	}

	public String getRoot() {
		return root;
	}

	public String getPath() {
		return path;
	}

	public String getValue() {
		return value;
	}

	public ConstraintViolationLevel getLevel() {
		return level;
	}
}
