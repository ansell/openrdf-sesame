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

package info.aduna.platform.support;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Platform implementation for Mac OS X platforms.
 */
public class MacOSXPlatform extends PosixPlatform {

	public static final String APPLICATION_DATA = "Library/Application Support/Aduna";

	@Override
	public String getName() {
		return "Mac OS X";
	}

	@Override
	public Path getOSApplicationDataDir() {
		return Paths.get(System.getProperty("user.home"), APPLICATION_DATA);
	}

	public boolean dataDirPreserveCase() {
		return true;
	}

	public boolean dataDirReplaceWhitespace() {
		return false;
	}
}
