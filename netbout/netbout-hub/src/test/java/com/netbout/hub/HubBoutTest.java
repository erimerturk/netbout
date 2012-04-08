/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
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
package com.netbout.hub;

import com.netbout.inf.Infinity;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case of {@link HubBout}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (200 lines)
 */
public final class HubBoutTest {

    /**
     * The viewer.
     */
    private transient Identity viewer;

    /**
     * The bout data type to work with.
     */
    private transient BoutDtMocker boutDtMocker;

    /**
     * The hub.
     */
    private transient PowerHub hub;

    /**
     * Prepare all mocks.
     * @throws Exception If there is some problem inside
     */
    @Before
    public void prepare() throws Exception {
        this.viewer = new IdentityMocker().mock();
        this.hub = new PowerHubMocker()
            .withIdentity(this.viewer.name(), this.viewer)
            .mock();
        this.boutDtMocker = new BoutDtMocker()
            .withParticipant(
                new ParticipantDtMocker()
                    .withIdentity(this.viewer.name())
                    .confirmed()
                    .mock()
        );
    }

    /**
     * HubBout can "wrap" BoutDt class.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void wrapsBoutDtDataProperties() throws Exception {
        final BoutDt data = this.boutDtMocker.mock();
        final Bout bout = new HubBout(this.hub, this.viewer, data);
        bout.number();
        Mockito.verify(data).getNumber();
        bout.title();
        Mockito.verify(data).getTitle();
    }

    /**
     * HubBout can "wrap" BoutDt renaming mechanism.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void wrapsBoutRenamingMechanism() throws Exception {
        final BoutDt data = this.boutDtMocker.mock();
        final Bout bout = new HubBout(this.hub, this.viewer, data);
        final String title = "some title, \u0443\u0440\u0430!";
        bout.rename(title);
        Mockito.verify(data).setTitle(title);
    }

    /**
     * HubBout can accept invitation requests and add participants to the bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void acceptsInvitationRequestsAndPassesThemToDt() throws Exception {
        final BoutDt data = this.boutDtMocker.mock();
        final Bout bout = new HubBout(this.hub, this.viewer, data);
        final Identity friend = Mockito.mock(Identity.class);
        final Urn fname = new UrnMocker().mock();
        Mockito.doReturn(fname).when(friend).name();
        bout.invite(friend);
        Mockito.verify(data).addParticipant(fname);
    }

    /**
     * HubBout can create proper predicate for infinity.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void createsProperRequestForInfinity() throws Exception {
        final PowerHub ihub = Mockito.mock(PowerHub.class);
        final Infinity infinity = Mockito.mock(Infinity.class);
        Mockito.doReturn(infinity).when(ihub).infinity();
        final Identity identity = new IdentityMocker().mock();
        final BoutDt data = new BoutDtMocker().mock();
        new HubBout(ihub, identity, data).messages("(pos 0)");
        Mockito.verify(infinity).messages(
            Mockito.argThat(
                Matchers.<String>allOf(
                    Matchers.containsString(
                        String.format(
                            "(equal $bout.number %d)",
                            data.getNumber()
                        )
                    ),
                    Matchers.not(Matchers.containsString("'(pos 0)'"))
                )
            )
        );
    }

    /**
     * HubBout can reject too long titles.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = com.netbout.spi.IllegalOperationException.class)
    public void rejectsTooLongTitles() throws Exception {
        // @checkstyle MagicNumber (1 line)
        final String title = StringUtils.repeat("some text", 200);
        new HubBout(this.hub, this.viewer, this.boutDtMocker.mock())
            .rename(title);
    }

    /**
     * HubBout can be equal to other bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void exposesEqualityFeature() throws Exception {
        MatcherAssert.assertThat(
            new HubBout(this.hub, this.viewer, this.boutDtMocker.mock()),
            Matchers.equalTo(
                new HubBout(this.hub, this.viewer, this.boutDtMocker.mock())
            )
        );
    }

}
