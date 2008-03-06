/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openrdf.util.StringUtil;

/**
 * An element in an HTTP header value. An HTTP header element has a value and
 * zero or more parameters consisting of a key and a value. An example header
 * element is <tt>audio/*; q=0.2</tt>.
 */
public class HeaderElement {
	
	/*----------------*
	 * Static methods *
	 *----------------*/

	public static HeaderElement parse(String encodedValue) {
		HeaderElement result = new HeaderElement();

		List tokens = HTTPUtil.splitHeaderString(encodedValue, ';');

		if (!tokens.isEmpty()) {
			// First token is the value of the header element
			String token = (String)tokens.get(0);

			// Remove any whitespace and double quotes from the token
			token = StringUtil.trimDoubleQuotes( token.trim() );
			
			result.setValue(token);
			
			// Add parameters to the header element
			for (int i = 1; i < tokens.size(); i++) {
				token = (String)tokens.get(i);
				
				int splitIdx = token.indexOf('=');
				
				if (splitIdx == -1) {
					// No value, only key
					token = StringUtil.trimDoubleQuotes( token.trim() );
					
					// Ignore empty parameters
					if (token.length() > 0) {
						result.addParameter(token);
					}
				}
				else {
					String key = token.substring(0, splitIdx).trim();
					String value = token.substring(splitIdx + 1).trim();
					value = StringUtil.trimDoubleQuotes(value);
					result.addParameter(key, value);
				}
			}
		}

		return result;
	}

	/*-----------*
	 * Variables *
	 *-----------*/
	
	private String _value;
	
	private List<Parameter> _parameters;
	
	/*--------------*
	 * Constructors *
	 *--------------*/
	
	public HeaderElement() {
		this("");
	}

	public HeaderElement(String value) {
		setValue(value);
		_parameters = new ArrayList<Parameter>();
	}
	
	/*---------*
	 * Methods *
	 *---------*/
	
	public String getValue() {
		return _value;
	}
	
	public void setValue(String value) {
		_value = value;
	}
	
	public int getParameterCount() {
		return _parameters.size();
	}
	
	public Parameter getParameter(int i) {
		return _parameters.get(i);
	}
	
	public Parameter getParameter(String key) {
		for (int i = 0; i < _parameters.size(); i++) {
			Parameter param = _parameters.get(i);
			if (param.getKey().equals(key)) {
				return param;
			}
		}
		
		return null;
	}
	
	public String getParameterValue(String key) {
		Parameter param = getParameter(key);
		
		if (param != null) {
			return param.getValue();
		}
		
		return null;
	}
	
	public List<Parameter> getParameters() {
		return Collections.unmodifiableList(_parameters);
	}
	
	public void addParameter(String key) {
		addParameter(key, null);
	}

	public void addParameter(String key, String value) {
		addParameter( new Parameter(key, value) );
	}
	
	public void addParameter(Parameter param) {
		_parameters.add(param);
	}
	
	public Parameter removeParameter(int idx) {
		return _parameters.remove(idx);
	}
	
	public boolean removeParameter(Parameter param) {
		return _parameters.remove(param);
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof HeaderElement) {
			HeaderElement other = (HeaderElement)obj;
			
			return
				_value.equals(other.getValue()) &&
				_parameters.equals(other.getParameters());
		}
		
		return false;
	}
	
	public int hashCode() {
		return _value.hashCode();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(32);
		sb.append(_value);
		
		for (int i = 0; i < _parameters.size(); i++) {
			Parameter param = _parameters.get(i);

			sb.append("; ");
			sb.append(param.toString());
		}
		
		return sb.toString();
	}
}
