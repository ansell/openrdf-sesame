/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.common.webapp.views;

import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.common.io.IOUtil;
import org.springframework.web.servlet.View;

/**
 * 
 * @author Herko ter Horst
 */
public class SimpleCustomResponseView implements View {

	public static final String SC_KEY = "sc";

	public static final String CONTENT_KEY = "content";

	public static final String CONTENT_LENGTH_KEY = "contentLength";

	public static final String CONTENT_TYPE_KEY = "contentType";

	private static final int DEFAULT_SC = HttpServletResponse.SC_OK;

	private static final SimpleCustomResponseView INSTANCE = new SimpleCustomResponseView();

	public static SimpleCustomResponseView getInstance() {
		return INSTANCE;
	}

	public String getContentType() {
		return null;
	}

	@SuppressWarnings("rawtypes")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		int sc = DEFAULT_SC;
		if (model.containsKey(SC_KEY)) {
			sc = (Integer)model.get(SC_KEY);
		}
		String contentType = (String)model.get(CONTENT_TYPE_KEY);
		Integer contentLength = (Integer)model.get(CONTENT_LENGTH_KEY);
		InputStream content = (InputStream)model.get(CONTENT_KEY);

		try {
			response.setStatus(sc);

			ServletOutputStream out = response.getOutputStream();
			if (content != null) {
				if (contentType != null) {
					response.setContentType(contentType);
				}
				if (contentLength != null) {
					response.setContentLength(contentLength);
				}
				IOUtil.transfer(content, out);
			}
			else {
				response.setContentLength(0);
			}
			out.close();
		}
		finally {
			if (content != null) {
				content.close();
			}
		}
	}
}
