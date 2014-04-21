/**
 * Copyright (c) 2009-2014, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
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
package com.netbout.rest.auth;

import com.jcabi.urn.URN;
import com.netbout.rest.jaxb.LongIdentity;
import com.netbout.spi.Identity;
import com.netbout.spi.xml.JaxbPrinter;
import com.rexsl.test.XhtmlMatchers;
import java.net.URL;
import java.util.Locale;
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case for {@link ResolvedIdentity}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ResolvedIdentityTest {

    /**
     * ResolvedIdentity can marshall itself to XML text.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void marshallsItselfToXml() throws Exception {
        final Identity identity = new ResolvedIdentity(
            new URL("http://localhost/authority"),
            new URN("urn:test:johnny")
        );
        identity.profile().alias("Johnny");
        identity.profile().setPhoto(new URL("http://localhost/pic.png"));
        identity.profile().setLocale(Locale.CHINESE);
        MatcherAssert.assertThat(
            new JaxbPrinter(
                new LongIdentity(
                    identity,
                    UriBuilder.fromPath("http://localhost")
                )
            ).print(),
            XhtmlMatchers.hasXPaths(
                "/identity[name='urn:test:johnny']",
                "/identity[authority='http://localhost/authority']",
                "/identity/aliases[alias='Johnny']",
                "/identity[locale='zh']"
            )
        );
    }

}