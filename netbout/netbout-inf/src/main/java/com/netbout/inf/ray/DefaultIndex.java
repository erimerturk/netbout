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
package com.netbout.inf.ray;

import com.jcabi.log.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;

/**
 * Index.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings({
    "PMD.TooManyMethods", "PMD.AvoidInstantiatingObjectsInLoops"
})
final class DefaultIndex implements Index {

    /**
     * Main map.
     */
    private final transient ConcurrentMap<String, SortedSet<Long>> map;

    /**
     * Reverse map.
     */
    private final transient ConcurrentMap<Long, Set<String>> rmap;

    /**
     * Public ctor.
     */
    public DefaultIndex() {
        this.map = new ConcurrentHashMap<String, SortedSet<Long>>();
        this.rmap = new ConcurrentHashMap<Long, Set<String>>();
    }

    /**
     * Public ctor.
     * @param file File to read from
     * @throws IOException If some IO error
     */
    public DefaultIndex(final File file) throws IOException {
        final InputStream stream = new FileInputStream(file);
        try {
            this.map = DefaultIndex.restore(stream);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        this.rmap = DefaultIndex.reverse(this.map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(final long msg, final String value) {
        this.validate(msg);
        this.clean(msg);
        this.add(msg, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final long msg, final String value) {
        this.validate(msg);
        this.numbers(value).add(msg);
        this.texts(msg).add(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final long msg, final String value) {
        this.validate(msg);
        this.numbers(value).remove(msg);
        this.texts(msg).remove(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clean(final long msg) {
        this.validate(msg);
        for (SortedSet<Long> set : this.map.values()) {
            set.remove(msg);
        }
        this.texts(msg).clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> values(final long msg) {
        this.validate(msg);
        return Collections.unmodifiableSet(this.texts(msg)).iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Long> msgs(final String value) {
        return Collections.unmodifiableSortedSet(this.numbers(value));
    }

    /**
     * Find lost messages.
     * @param msgs Numbers to look for
     * @return Collection of numbers, which are not found
     */
    public Collection<Long> lost(final Collection<Long> msgs) {
        final Collection<Long> lost = new LinkedList<Long>();
        for (Long number : msgs) {
            if (!this.rmap.containsKey(number)
                || this.rmap.get(number).isEmpty()) {
                lost.add(number);
            }
        }
        return lost;
    }

    /**
     * Flush this map to file.
     * @param file Where to write
     * @throws IOException If some problem
     */
    public void flush(final File file) throws IOException {
        final long start = System.currentTimeMillis();
        final OutputStream stream = new FileOutputStream(file);
        try {
            final PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(stream, CharEncoding.UTF_8)
            );
            for (String value : this.map.keySet()) {
                writer.println(value);
                for (Long number : this.map.get(value)) {
                    writer.print(' ');
                    writer.println(number.toString());
                }
            }
            writer.flush();
        } finally {
            IOUtils.closeQuietly(stream);
        }
        Logger.debug(
            this,
            "#save(): saved %d values to %s (%d bytes) in %[ms]s",
            this.map.size(),
            file,
            file.length(),
            System.currentTimeMillis() - start
        );
    }

    /**
     * Restore map from stream.
     * @param stream The stream to read from
     * @return The data restored
     * @throws IOException If some IO error
     */
    private static ConcurrentMap<String, SortedSet<Long>> restore(
        final InputStream stream) throws IOException {
        final ConcurrentMap<String, SortedSet<Long>> data =
            new ConcurrentHashMap<String, SortedSet<Long>>();
        final long start = System.currentTimeMillis();
        final BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, CharEncoding.UTF_8)
        );
        String value = null;
        while (true) {
            final String line = reader.readLine();
            if (line == null || line.isEmpty()) {
                break;
            }
            if (line.charAt(0) == ' ') {
                data.get(value).add(Long.valueOf(line.substring(1)));
            } else {
                value = line;
                data.put(
                    value,
                    new ConcurrentSkipListSet(Collections.reverseOrder())
                );
            }
        }
        Logger.debug(
            DefaultIndex.class,
            "#restore(): restored %d values in %[ms]s",
            data.size(),
            System.currentTimeMillis() - start
        );
        return data;
    }

    /**
     * Reverse the map.
     * @param origin Original map
     * @return The reversed one
     */
    private static ConcurrentMap<Long, Set<String>> reverse(
        final ConcurrentMap<String, SortedSet<Long>> origin) {
        final ConcurrentMap<Long, Set<String>> data =
            new ConcurrentHashMap<Long, Set<String>>();
        for (ConcurrentMap.Entry<String, SortedSet<Long>> entry
            : origin.entrySet()) {
            for (Long number : entry.getValue()) {
                data.putIfAbsent(
                    number,
                    new ConcurrentSkipListSet()
                );
                data.get(number).add(entry.getKey());
            }
        }
        return data;
    }

    /**
     * Validate this message number and throw runtime exception if it's not
     * valid (is ZERO or MAX_VALUE).
     * @param msg The number of msg
     */
    private void validate(final long msg) {
        if (msg == 0L) {
            throw new IllegalArgumentException("msg number can't be ZERO");
        }
        if (msg == Long.MAX_VALUE) {
            throw new IllegalArgumentException("msg number can't be MAX_VALUE");
        }
    }

    /**
     * Texts for given number.
     * @param number The number
     * @return Texts (link to existing structure)
     */
    private Set<String> texts(final long number) {
        this.rmap.putIfAbsent(number, new ConcurrentSkipListSet());
        return this.rmap.get(number);
    }

    /**
     * Numbers for the given value.
     * @param text The text value
     * @return Numbers (link to existing structure in the MAP)
     */
    private SortedSet<Long> numbers(final String text) {
        this.map.putIfAbsent(
            text,
            new ConcurrentSkipListSet(Collections.reverseOrder())
        );
        return this.map.get(text);
    }

}