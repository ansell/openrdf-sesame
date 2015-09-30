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
package org.openrdf.workbench.commands;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.Iterations;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;

public class SummaryServlet extends TransformationServlet {

	private final ExecutorService executorService = Executors.newCachedThreadPool();

	private static final Logger LOGGER = LoggerFactory.getLogger(SummaryServlet.class);

	@Override
	public void service(TupleResultBuilder builder, String xslPath)
		throws RepositoryException, QueryEvaluationException, MalformedQueryException,
		QueryResultHandlerException
	{
		builder.transform(xslPath, "summary.xsl");
		builder.start("id", "description", "location", "server", "size", "contexts");
		builder.link(Arrays.asList(INFO));
		final RepositoryConnection con = repository.getConnection();
		try {
			String size = null;
			String numContexts = null;
			try {
				List<Future<String>> futures = getRepositoryStatistics(con);
				size = getResult("repository size.", futures.get(0));
				numContexts = getResult("labeled contexts.", futures.get(1));
			}
			catch (InterruptedException e) {
				LOGGER.warn("Interrupted while requesting repository statistics.", e);
			}
			builder.result(info.getId(), info.getDescription(), info.getLocation(), getServer(), size,
					numContexts);
			builder.end();
		}
		finally {
			con.close();
		}
	}

	private String getResult(String itemRequested, Future<String> future) {
		String result = "Unexpected interruption while requesting " + itemRequested;
		try {
			if (future.isCancelled()) {
				result = "Timed out while requesting " + itemRequested;
			}
			else {
				try {
					result = future.get();
				}
				catch (ExecutionException e) {
					LOGGER.warn("Exception occured during async request.", e);
					result = "Exception occured while requesting " + itemRequested;
				}
			}
		}
		catch (InterruptedException e) {
			LOGGER.error("Unexpected exception", e);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private List<Future<String>> getRepositoryStatistics(final RepositoryConnection con)
		throws InterruptedException
	{
		List<Future<String>> futures;
		futures = executorService.invokeAll(Arrays.asList(new Callable<String>() {

			@Override
			public String call()
				throws RepositoryException
			{
				return Long.toString(con.size());
			}

		}, new Callable<String>() {

			@Override
			public String call()
				throws RepositoryException
			{
				return Integer.toString(Iterations.asList(con.getContextIDs()).size());
			}

		}), 2000, TimeUnit.MILLISECONDS);
		return futures;
	}

	private String getServer() {
		String result = null; // gracefully ignored by builder.result(...)
		if (manager instanceof LocalRepositoryManager) {
			result = ((LocalRepositoryManager)manager).getBaseDir().toString();
		}
		else if (manager instanceof RemoteRepositoryManager) {
			result = ((RemoteRepositoryManager)manager).getServerURL();
		}
		return result;
	}
}
