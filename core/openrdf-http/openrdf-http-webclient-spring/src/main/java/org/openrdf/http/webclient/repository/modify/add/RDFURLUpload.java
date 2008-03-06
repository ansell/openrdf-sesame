/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.modify.add;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Herko ter Horst
 */
public class RDFURLUpload extends RDFUpload {

	private URL contents;

	public URL getContents() {
		return contents;
	}

	public void setContents(URL contents) {
		this.contents = contents;
	}

	@Override
	public InputStream getInputStream()
		throws IOException
	{
		return getContents().openStream();
	}

	@Override
	public String getI18n()
	{
		return "repository.modify.add.url.success";
	}
}
