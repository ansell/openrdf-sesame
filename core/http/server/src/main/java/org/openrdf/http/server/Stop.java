/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import info.aduna.net.http.HttpClientUtil;

/**
 * Main class for shutting down a Sesame server.
 * 
 * @author Arjohn Kampman
 */
public class Stop {

	public static void main(String[] args) {
		// Parse command line options
		Options options = new Options();

		Option helpOption = new Option("h", "help", false, "print this help");
		Option serverURLOption = new Option("s", "serverURL", true,
				"URL of Sesame server to connect to, e.g. http://localhost/openrdf-sesame/ (required)");
		Option keyOption = new Option("k", "shutdownKey", true, "Key to shut down server");

		options.addOption(helpOption);
		options.addOption(serverURLOption);
		options.addOption(keyOption);

		CommandLineParser argsParser = new PosixParser();

		try {
			CommandLine commandLine = argsParser.parse(options, args);

			if (commandLine.hasOption(helpOption.getOpt())) {
				printUsage(options);
				System.exit(0);
			}

			String serverURL = commandLine.getOptionValue(serverURLOption.getOpt());
			String[] otherArgs = commandLine.getArgs();

			if (serverURL == null || otherArgs.length > 1) {
				System.out.println("Please specify a server URL");
				printUsage(options);
				System.exit(2);
			}

			if (!serverURL.endsWith("/")) {
				serverURL += "/";
			}
			serverURL += SesameServer.SHUTDOWN_PATH;

			String keyString = commandLine.getOptionValue(keyOption.getOpt());

			try {
				URL url = new URL(serverURL);

				System.out.println("connecting to the server...");
				HttpURLConnection con = (HttpURLConnection)url.openConnection();
				con.setDoOutput(true);
				con.setRequestMethod("POST");

				if (keyString != null) {
					String form = HttpClientUtil.encodeParameter(SesameServer.SHUTDOWN_KEY_PARAM, keyString);
					OutputStream out = con.getOutputStream();
					out.write(form.getBytes("ISO-8859-1"));
				}

				if (HttpClientUtil.is2xx(con.getResponseCode())) {
					System.out.println("shutdown command sent successfully");
				}
				else {
					System.out.println("Shutdown request failed: " + con.getResponseMessage());
					System.exit(3);
				}
			}
			catch (MalformedURLException e) {
				System.err.println("Malformed URL: " + serverURL);
			}
			catch (IOException e) {
				System.err.println("I/O error: " + e.getMessage());
				e.printStackTrace();
			}
		}
		catch (ParseException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	private static void printUsage(Options options) {
		System.out.println("Sesame Server, a standalone RDF server.");
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(80);
		formatter.printHelp("server [OPTION]", options);
	}
}
