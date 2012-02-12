/**
 * Copyright (c) 2009-2011, netBout.com
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
package com.netbout.inf.predicates;

import com.netbout.inf.Meta;
import com.netbout.inf.Msg;
import com.netbout.inf.Predicate;
import com.ymock.util.Logger;
import java.util.List;

/**
 * We have this number of elements in the result list, not more.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Meta(name = "limit")
public final class LimitPred extends AbstractVarargPred {

    /**
     * How many we already allowed to go?
     */
    private transient int passed;

    /**
     * Public ctor.
     * @param args The arguments
     */
    public LimitPred(final List<Predicate> args) {
        super(args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object evaluate(final Msg msg, final int pos) {
        final int limit = Integer.valueOf(
            this.arg(0).evaluate(msg, pos).toString()
        );
        boolean matches;
        synchronized (this) {
            matches = this.passed < limit;
            if (matches) {
                this.passed += 1;
            }
        }
        Logger.debug(
            this,
            "#evaluate(#%d, %d): %d already passed, limit is #%d: %B",
            msg.number(),
            pos,
            this.passed,
            limit,
            matches
        );
        return matches;
    }

}