/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.modify.add;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author Herko ter Horst
 */
public class RDFFileUpload extends RDFUpload {

	private MultipartFile contents;

	public MultipartFile getContents() {
		return contents;
	}

	public void setContents(MultipartFile contents) {
		this.contents = contents;
	}

	@Override
	public InputStream getInputStream()
		throws IOException
	{
		return getContents().getInputStream();
	}

	@Override
	public String getI18n()
	{
		return "repository.modify.add.file.success";
	}
	

}
