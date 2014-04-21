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
package com.netbout.rest;

import com.jcabi.log.Logger;
import javax.ws.rs.core.UriBuilder;

/**
 * Login required.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LoginRequiredException extends ForwardException {

    /**
     * Serialization marker.
     */
    private static final long serialVersionUID = 0x7529FA789ED21669L;

    /**
     * Constructor.
     * @param res The originator of the exception
     * @param cause Cause of this problem
     */
    public LoginRequiredException(final NbResource res, final String cause) {
        super(res, LoginRequiredException.path(res), cause);
        Logger.debug(
            this,
            "#LoginRequiredException('%[type]s', '%s'): thrown",
            res,
            cause
        );
    }

    /**
     * Constructor.
     * @param res The originator of the exception
     * @param cause Cause of this problem
     */
    public LoginRequiredException(final NbResource res, final Exception cause) {
        super(res, LoginRequiredException.path(res), cause);
        Logger.debug(
            this,
            "#LoginRequiredException('%[type]s', ..): thrown:\n%[exception]s",
            res,
            cause
        );
    }

    /**
     * Build path to forward to.
     * @param res The originator of the exception
     * @return The destination
     */
    private static UriBuilder path(final NbResource res) {
        return res.base().path("/g");
    }

}