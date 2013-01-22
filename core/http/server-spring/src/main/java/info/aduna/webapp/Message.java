/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
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
