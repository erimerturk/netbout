/**
 * Copyright (c) 2009-2015, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
 * incident to the author by email.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.netbout.rest;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import java.net.HttpURLConnection;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.Test;

/**
 * Integration case for {@link TkApp}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 */
@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
public final class TkAppITCase {

    /**
     * Home page of Tomcat.
     */
    private static final String HOME = System.getProperty("takes.home");

    /**
     * TkApp can render static resources.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void downloadsStaticResources() throws Exception {
        final String[] pages = {
            "/robots.txt",
            "/css/style.css",
            "/js/bout.js",
            "/xsl/login.xsl",
            "/xsl/bout.xsl",
            "/lang/en.xml",
        };
        for (final String page : pages) {
            new JdkRequest(TkAppITCase.HOME)
                .uri().path(page).queryParam("alpha", "boom1").back()
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        }
    }

    /**
     * TkApp can render non-found pages.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void downloadsNonFoundResources() throws Exception {
        final String[] pages = {
            "/the-page-doesnt-exist",
            "/-this-one-also",
        };
        for (final String page : pages) {
            new JdkRequest(TkAppITCase.HOME)
                .uri().path(page).back()
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    /**
     * TkApp can authenticate test user.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void authenticatesTestUser() throws Exception {
        new JdkRequest(TkAppITCase.HOME)
            .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .assertXPath("/page/identity");
    }

    /**
     * TkApp can render HTML by default.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void rendersHtmlByDefault() throws Exception {
        new JdkRequest(TkAppITCase.HOME)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .assertXPath("/xhtml:html");
    }

}
