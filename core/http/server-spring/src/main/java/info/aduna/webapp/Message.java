/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.webapp;

/**
 * 
 * @author Herko ter Horst
 */
public class Message {

	public static final String ATTRIBUTE_KEY = "message";
	
	public static enum Type {
		ERROR,
		WARN,
		INFO
	};

	private Type type;

	private String i18n;

	public Message(Type type, String i18n) {
		this.type = type;
		this.i18n = i18n;
	}

	/**
	 * @return Returns the type.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return Returns the i18n.
	 */
	public String getI18n() {
		return i18n;
	}

}
