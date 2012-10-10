/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.webapp.views;

import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

/**
 * @author Herko ter Horst
 */
public class SimpleResponseView implements View {

	public static final String SC_KEY = "sc";

	public static final String CONTENT_KEY = "content";

	private static final int DEFAULT_SC = HttpServletResponse.SC_OK;

	private static final String CONTENT_TYPE = "text/plain; charset=UTF-8";

	private static final SimpleResponseView INSTANCE = new SimpleResponseView();

	public static SimpleResponseView getInstance() {
		return INSTANCE;
	}

	private SimpleResponseView() {
	}

	public String getContentType() {
		return CONTENT_TYPE;
	}

	@SuppressWarnings("rawtypes")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		Integer sc = (Integer)model.get(SC_KEY);
		if (sc == null) {
			sc = DEFAULT_SC;
		}
		response.setStatus(sc.intValue());

		response.setContentType(CONTENT_TYPE);

		OutputStream out = response.getOutputStream();

		String content = (String)model.get(CONTENT_KEY);
		if (content != null) {
			byte[] contentBytes = content.getBytes("UTF-8");
			response.setContentLength(contentBytes.length);
			out.write(contentBytes);
		}
		else {
			response.setContentLength(0);
		}

		out.close();
	}
}
