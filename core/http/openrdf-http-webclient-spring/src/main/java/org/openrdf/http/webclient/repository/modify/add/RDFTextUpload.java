/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.modify.add;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Herko ter Horst
 */
public class RDFTextUpload extends RDFUpload {

	private String contents;

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

	@Override
	public InputStream getInputStream()
		throws IOException
	{
		return new ByteArrayInputStream(contents.getBytes(getFormat().getCharset().name()));
	}

	@Override
	public String getI18n()
	{
		return "repository.modify.add.text.success";
	}
}
