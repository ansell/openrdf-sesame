package org.openrdf.http.client;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SesameHTTPClientTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    SesameHTTPClient httpClient = new SesameHTTPClient();

    @Test
    public void setUsernameAndPassword_should_succeed_with_server_url_but_no_query_url() {

        httpClient.setServerURL("http://www.repo.org/server");

        assertFalse(httpClient.getHttpClient().getParams().isAuthenticationPreemptive());

        httpClient.setUsernameAndPassword("user01", "secret");

        assertTrue(httpClient.getHttpClient().getParams().isAuthenticationPreemptive());

    }

    @Test
    public void setUsernameAndPassword_should_throw_exception_when_serverUrl_not_set() {

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Server URL has not been set");

        assertFalse(httpClient.getHttpClient().getParams().isAuthenticationPreemptive());

        httpClient.setUsernameAndPassword("user01", "secret");

        fail("Don't reach this point");
    }

}
