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
package com.netbout.rest.jaxb;

import com.netbout.client.RestSession;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Participant;
import com.netbout.spi.Query;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Short version of a bout.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "bout")
@XmlAccessorType(XmlAccessType.NONE)
public final class ShortBout {

    /**
     * The original bout.
     */
    private transient Bout bout;

    /**
     * URI builder.
     */
    private final transient UriBuilder builder;

    /**
     * The viewer of it.
     */
    private final transient Identity viewer;

    /**
     * Public ctor for JAXB.
     */
    public ShortBout() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Private ctor.
     * @param parent Parent bout to refer to
     * @param bldr URI builder
     * @param vwr The viewer
     */
    public ShortBout(final Bout parent, final UriBuilder bldr,
        final Identity vwr) {
        this.bout = parent;
        this.builder = bldr;
        this.viewer = vwr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("#%d", this.bout.number());
    }

    /**
     * Link to the bout.
     * @return The link
     */
    @XmlElement
    public Link getLink() {
        return new Link("page", this.builder);
    }

    /**
     * JAXB related method, to return the number of the bout.
     * @return The number
     */
    @XmlElement
    public Long getNumber() {
        return this.bout.number();
    }

    /**
     * JAXB related method, to return the date of the bout.
     * @return The date
     */
    @XmlElement
    public Date getDate() {
        return this.bout.date();
    }

    /**
     * Most recent date of this bout.
     * @return The date
     */
    @XmlElement
    public Date getRecent() {
        return new Bout.Smart(this.bout).updated();
    }

    /**
     * JAXB related method, to return the title of the bout.
     * @return The title
     */
    @XmlElement
    public String getTitle() {
        return this.bout.title();
    }

    /**
     * JAXB related method, to return participants of the bout.
     * @return The collection
     */
    @XmlElement(name = "participant")
    @XmlElementWrapper(name = "participants")
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Collection<LongParticipant> getParticipants() {
        final Collection<LongParticipant> dudes =
            new LinkedList<LongParticipant>();
        final Participant myself =
            new Bout.Smart(this.bout).participant(this.viewer);
        for (Participant dude : this.bout.participants()) {
            dudes.add(new LongParticipant(dude, this.builder, myself));
        }
        return dudes;
    }

    /**
     * This bout has any unseen messsages?
     * @return TRUE if there are some unseen messages inside
     */
    @XmlAttribute
    public boolean isUnseen() {
        return !new Bout.Smart(this.bout).seen();
    }

    /**
     * List of bundled bouts.
     * @return The collection of links to them
     */
    @XmlElement(name = "link")
    @XmlElementWrapper(name = "bundled")
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Collection<Link> getBundled() {
        final Collection<Link> links = new LinkedList<Link>();
        final Query query = new Query.Textual(
            String.format(
                "(unbundled %d)",
                this.bout.number()
            )
        );
        final Iterator<Bout> bouts = this.viewer.inbox(query).iterator();
        // @checkstyle MagicNumber (1 line)
        int max = 5;
        while (bouts.hasNext() && max > 0) {
            final Bout.Smart item = new Bout.Smart(bouts.next());
            max -= 1;
            final Link link = new Link(
                "bout",
                this.builder.clone().path("/../{num}").build(item.number())
            );
            links.add(
                link.with(new JaxbBundle("number", item.number().toString()))
                    .with(new JaxbBundle("title", item.title()))
                    .with(
                        new JaxbBundle(
                            "unseen",
                            Boolean.toString(!item.seen())
                        )
                    )
            );
        }
        if (max == 0) {
            links.add(
                new Link(
                    "all",
                    this.builder.clone().path("/..")
                        .replaceQueryParam(RestSession.QUERY_PARAM, "{query}")
                        .queryParam(RestSession.BUNDLE_PARAM, "")
                        .build(query)
                )
            );
        }
        return links;
    }

}