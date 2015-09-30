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
package info.aduna.io;

import java.io.IOException;
import java.io.Writer;

/**
 * A writer that adds indentation to written text.
 * 
 * @author Arjohn Kampman
 */
public class IndentingWriter extends Writer {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * The (platform-dependent) line separator.
	 */
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The underlying writer.
	 */
	protected Writer out;

	/**
	 * The current indentation level, i.e. the number of tabs to indent a start
	 * or end tag.
	 */
	protected int indentationLevel = 0;

	/**
	 * The string to use for indentation, e.g. a tab or a number of spaces.
	 */
	private String indentationString = "\t";

	/**
	 * Flag indicating whether indentation has been written for the current line.
	 */
	private boolean indentationWritten = false;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public IndentingWriter(Writer out) {
		this.out = out;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Sets the string that should be used for indentation. The default
	 * indentation string is a tab character.
	 * 
	 * @param indentString
	 *        The indentation string, e.g. a tab or a number of spaces.
	 */
	public void setIndentationString(String indentString) {
		this.indentationString = indentString;
	}

	/**
	 * Gets the string used for indentation.
	 * 
	 * @return the indentation string.
	 */
	public String getIndentationString() {
		return indentationString;
	}

	public int getIndentationLevel() {
		return indentationLevel;
	}

	public void setIndentationLevel(int indentationLevel) {
		this.indentationLevel = indentationLevel;
	}

	public void increaseIndentation() {
		indentationLevel++;
	}

	public void decreaseIndentation() {
		indentationLevel--;
	}

	/**
	 * Writes an end-of-line character sequence and triggers the indentation for
	 * the text written on the next line.
	 */
	public void writeEOL()
		throws IOException
	{
		write(LINE_SEPARATOR);
		indentationWritten = false;
	}

	@Override
	public void close()
		throws IOException
	{
		out.close();
	}

	@Override
	public void flush()
		throws IOException
	{
		out.flush();
	}

	@Override
	public void write(char cbuf[], int off, int len)
		throws IOException
	{
		if (!indentationWritten) {
			for (int i = 0; i < indentationLevel; i++) {
				out.write(indentationString);
			}

			indentationWritten = true;
		}

		out.write(cbuf, off, len);
	}
}
