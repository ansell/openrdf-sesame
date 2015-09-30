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
package org.openrdf.query.algebra.evaluation.federation;

import org.apache.http.client.HttpClient;

import org.openrdf.http.client.HttpClientDependent;
import org.openrdf.http.client.SesameClient;
import org.openrdf.http.client.SesameClientDependent;
import org.openrdf.http.client.SesameClientImpl;
import org.openrdf.query.QueryEvaluationException;

/**
 * The {@link FederatedServiceResolverImpl} is used to manage a set of
 * {@link FederatedService} instances, which are used to evaluate SERVICE
 * expressions for particular service Urls.
 * <p>
 * Lookup can be done via the serviceUrl using the method
 * {@link #getService(String)}. If there is no service for the specified url, a
 * {@link SPARQLFederatedService} is created and registered for future use.
 * 
 * @author Andreas Schwarte
 * @author James Leigh
 */
public class FederatedServiceResolverImpl extends AbstractFederatedServiceResolver implements FederatedServiceResolver, HttpClientDependent, SesameClientDependent {

	public FederatedServiceResolverImpl() {
		super();
	}

	/** independent life cycle */
	private SesameClient client;

	/** dependent life cycle */
	private SesameClientImpl dependentClient;

	public synchronized SesameClient getSesameClient() {
		if (client == null) {
			client = dependentClient = new SesameClientImpl();
		}
		return client;
	}

	public synchronized void setSesameClient(SesameClient client) {
		this.client = client;
	}

	public HttpClient getHttpClient() {
		return getSesameClient().getHttpClient();
	}

	public void setHttpClient(HttpClient httpClient) {
		if (dependentClient == null) {
			client = dependentClient = new SesameClientImpl();
		}
		dependentClient.setHttpClient(httpClient);
	}
	
	@Override
	protected FederatedService createService(String serviceUrl)
			throws QueryEvaluationException {
		return new SPARQLFederatedService(serviceUrl, getSesameClient());
	}

	@Override
	public void shutDown() {
		super.shutDown();
		if (dependentClient != null) {
			dependentClient.shutDown();
			dependentClient = null;
		}
	}
}
