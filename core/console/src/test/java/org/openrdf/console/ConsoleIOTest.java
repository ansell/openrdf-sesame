package org.openrdf.console;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ConsoleIOTest {

	private ConsoleIO io;

	@Before
	public void initConsoleObject() {
		BufferedReader input = mock(BufferedReader.class);
		PrintStream out = mock(PrintStream.class);
		PrintStream err = mock(PrintStream.class);
		ConsoleState info = mock(ConsoleState.class);
		io = new ConsoleIO(input, out, err, info);
	}

	@Test
	public void shouldSetErrorWrittenWhenErrorsAreWritten() {
		io.writeError(null);
		assertThat(io.wasErrorWritten(), equalTo(true));
	}

	@Test
	public void shouldSetErroWrittenOnParserError() {
		io.writeParseError("", 0, 0, "");
		assertThat(io.wasErrorWritten(), equalTo(true));
	}

	@Test
	public void shouldSetErroWrittenOnWriteUnoppenedError() {
		io.writeUnopenedError();
		assertThat(io.wasErrorWritten(), equalTo(true));
	}
}
