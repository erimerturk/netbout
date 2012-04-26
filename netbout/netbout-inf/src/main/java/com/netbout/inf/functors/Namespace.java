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
package com.netbout.inf.motors.bundles;

import com.netbout.inf.Functor;
import com.netbout.inf.PredicateException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Select messages by XML namespace.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@NamedAs
final class Namespace implements Functor, Noticable<MessagePostedNotice> {

    /**
     * The attribute to use.
     */
    private static final String ATTR = "xml-namespace";

    /**
     * {@inheritDoc}
     */
    @Override
    final Term build(final Ray ray, final List<Atom> atoms) {
        return ray.builder().matcher(
            Namespace.ATTR,
            TextAtom.class.cast(atoms.get(0)).value()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Ray ray, final MessagePostedNotice notice) {
        final Message message = notice.message();
        final DomParser parser = new DomParser(message.text());
        if (parser.isXml()) {
            try {
                ray.create(msg.number()).set(
                    Namespace.ATTR,
                    parser.namespace().toString()
                );
            } catch (com.netbout.spi.xml.DomValidationException ex) {
                Logger.warn(
                    Namespace.class,
                    "#see(#%d): %[exception]s",
                    message.number(),
                    ex
                );
            }
        }
    }

}
