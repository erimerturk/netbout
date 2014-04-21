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
import com.netbout.hub.Hub;
import com.netbout.rest.BoutRs;
import com.netbout.rest.StageCoordinates;
import com.netbout.rest.period.Period;
import com.netbout.rest.period.PeriodsBuilder;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.netbout.spi.Query;
import com.rexsl.page.Link;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bout convertable to XML through JAXB.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@XmlRootElement(name = "bout")
@XmlAccessorType(XmlAccessType.NONE)
@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
public final class LongBout {

    /**
     * The bus.
     */
    private final transient Hub hub;

    /**
     * The bout.
     */
    private final transient Bout bout;

    /**
     * Stage coordinates.
     */
    private final transient StageCoordinates coords;

    /**
     * Search keyword.
     */
    private final transient String query;

    /**
     * The URI builder.
     */
    private final transient UriBuilder builder;

    /**
     * The viewer of it.
     */
    private final transient Identity viewer;

    /**
     * The view.
     */
    private final transient String view;

    /**
     * Periods to show.
     */
    private final transient Collection<Link> periods = new LinkedList<Link>();

    /**
     * Public ctor for JAXB.
     */
    public LongBout() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Private ctor.
     * @param ihub The hub
     * @param bot The bout
     * @param crds The coordinates of the stage to render
     * @param keyword Search keyword
     * @param bldr The builder of URIs
     * @param vwr The viewer
     * @param period Which period to view
     * @checkstyle ParameterNumber (7 lines)
     */
    public LongBout(
        final Hub ihub, final Bout bot,
        final StageCoordinates crds,
        final String keyword, final UriBuilder bldr, final Identity vwr,
        final String period
    ) {
        this.hub = ihub;
        this.bout = bot;
        this.coords = crds;
        this.query = keyword;
        this.builder = bldr;
        this.viewer = vwr;
        this.view = period;
    }

    /**
     * Get number.
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
     * Get its title.
     * @return The title
     */
    @XmlElement
    public String getTitle() {
        return this.bout.title();
    }

    /**
     * Get view.
     * @return The view
     */
    @XmlElement(nillable = false)
    public String getView() {
        return this.view;
    }

    /**
     * List of stages.
     * @return The list
     */
    @XmlElement(name = "stage")
    @XmlElementWrapper(name = "stages")
    public List<ShortStage> getStages() {
        final List<ShortStage> stages = new LinkedList<ShortStage>();
        for (Friend stage : this.coords.all()) {
            stages.add(new ShortStage(stage, this.builder.clone()));
        }
        return stages;
    }

    /**
     * Get XML of one stage.
     * @return The XML
     */
    @XmlElement
    public LongStage getStage() {
        LongStage stage = null;
        if (!this.coords.stage().isEmpty()) {
            stage = new LongStage(
                this.hub,
                this.bout,
                this.coords,
                this.viewer
            );
        }
        return stage;
    }

    /**
     * List of messages in it.
     * @return The list
     */
    @XmlElement(name = "message")
    @XmlElementWrapper(name = "messages")
    public List<LongMessage> getMessages() {
        // @checkstyle MagicNumber (1 line)
        final Period period = PeriodsBuilder.parse(this.view, 20L);
        final Iterable<Message> discussion =
            this.bout.messages(new Query.Textual(period.query(this.query)));
        final PeriodsBuilder pbld = new PeriodsBuilder(
            period,
            UriBuilder.fromUri(
                this.builder.clone()
                    .replaceQueryParam(RestSession.QUERY_PARAM, "{query}")
                    .build(this.query)
            )
        ).setQueryParam(BoutRs.PERIOD_PARAM);
        final List<LongMessage> msgs = new LinkedList<LongMessage>();
        for (Message msg : discussion) {
            Boolean show = this.hub.make("is-message-visible")
                .synchronously()
                .inBout(this.bout)
                .arg(this.bout.number())
                .arg(msg.number())
                .arg(msg.text())
                .asDefault(true)
                .exec();
            if (!show) {
                continue;
            }
            try {
                show = pbld.show(msg.date());
            } catch (com.netbout.rest.period.PeriodViolationException ex) {
                throw new IllegalStateException(
                    String.format("Invalid date of message #%d", msg.number()),
                    ex
                );
            }
            if (show) {
                msgs.add(new LongMessage(this.hub, this.bout, msg));
            }
            if (!pbld.more()) {
                break;
            }
        }
        this.periods.addAll(pbld.links());
        return msgs;
    }

    /**
     * List of participants.
     * @return The list
     */
    @XmlElement(name = "participant")
    @XmlElementWrapper(name = "participants")
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
     * List of periods.
     * @return The list
     */
    @XmlElement(name = "link")
    @XmlElementWrapper(name = "periods")
    public Collection<Link> getPeriods() {
        return this.periods;
    }

}