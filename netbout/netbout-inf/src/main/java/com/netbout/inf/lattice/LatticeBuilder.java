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
package com.netbout.inf.lattice;

import com.netbout.inf.Lattice;
import com.netbout.inf.Term;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * Builder of Lattice.
 *
 * <p>The class is mutable and NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LatticeBuilder {

    /**
     * The main bitset.
     */
    private transient BitSet main = new BitSet(BitsetLattice.BITS);

    /**
     * The reverse bitset.
     */
    private transient BitSet reverse = new BitSet(BitsetLattice.BITS);

    /**
     * Build the lattice.
     * @return The lattice built
     */
    public Lattice build() {
        return new BitsetLattice(this.main, this.reverse);
    }

    /**
     * Fill lattice with this set of numbers.
     * @param numbers The numbers to use
     * @return This object
     */
    public LatticeBuilder fill(final SortedSet<Long> numbers) {
        long previous = Long.MAX_VALUE;
        for (Long num : numbers) {
            if (previous - num < BitsetLattice.SIZE / 2) {
                continue;
            }
            previous = num;
            final int bit = BitsetLattice.bit(num);
            this.main.set(bit);
        }
        this.reverse.set(0, BitsetLattice.BITS, false);
        final Iterator<Long> iterator = numbers.iterator();
        Long next = Long.MAX_VALUE;
        for (int bit = 0; bit < BitsetLattice.BITS; ++bit) {
            final long window = BitsetLattice.msg(bit + 1);
            boolean seen = false;
            while (next > window && iterator.hasNext()) {
                seen = true;
                next = iterator.next();
            }
            if (!seen) {
                this.reverse.set(bit);
            }
        }
        return this;
    }

    /**
     * Copy existing lattice to the builder (all data in the builder are
     * destroyed).
     * @param lattice The lattice to copy
     * @return This object
     */
    public LatticeBuilder copy(final Lattice lattice) {
        this.main = BitSet.class.cast(
            BitsetLattice.class.cast(lattice).main.clone()
        );
        this.reverse = BitSet.class.cast(
            BitsetLattice.class.cast(lattice).reverse.clone()
        );
        return this;
    }

    /**
     * Set lattice to always true.
     * @return This object
     */
    public LatticeBuilder always() {
        this.main.set(0, BitsetLattice.BITS, true);
        this.reverse.set(0, BitsetLattice.BITS, false);
        return this;
    }

    /**
     * Set lattice to never true.
     * @return This object
     */
    public LatticeBuilder never() {
        this.main.set(0, BitsetLattice.BITS, false);
        this.reverse.set(0, BitsetLattice.BITS, true);
        return this;
    }

    /**
     * AND all lattices from the provided terms.
     * @param terms Terms to get lattices from
     * @return This object
     */
    public LatticeBuilder and(final Collection<Term> terms) {
        for (Term term : terms) {
            final BitsetLattice lattice =
                BitsetLattice.class.cast(term.lattice());
            this.main.and(lattice.main);
            this.reverse.or(lattice.reverse);
        }
        return this;
    }

    /**
     * OR all lattices from the provided terms.
     * @param terms Terms to get lattices from
     * @return This object
     * @checkstyle MethodName (4 lines)
     */
    @SuppressWarnings("PMD.ShortMethodName")
    public LatticeBuilder or(final Collection<Term> terms) {
        for (Term term : terms) {
            final BitsetLattice lattice =
                BitsetLattice.class.cast(term.lattice());
            this.main.or(lattice.main);
            this.reverse.and(lattice.reverse);
        }
        return this;
    }

    /**
     * Revert the lattice.
     * @return This object
     */
    public LatticeBuilder revert() {
        final BitSet temp = this.main;
        this.main = this.reverse;
        this.reverse = temp;
        return this;
    }

    /**
     * Set main and reverse bit for this message.
     * @param number The number of message
     * @param bit Main bit to set to
     * @param numbers Where it is happening
     * @return This object
     */
    public LatticeBuilder set(final long number, final boolean bit,
        final SortedSet<Long> numbers) {
        final int num = BitsetLattice.bit(number);
        if (bit) {
            this.main.set(num);
        }
        if (!bit && this.emptyBit(numbers, number)) {
            this.reverse.set(num);
        }
        return this;
    }

    /**
     * Do we have an empty bit for this message.
     *
     * <p>The method checks all message around the provided one inside the
     * the provided set and returns TRUE only if nothing is found near by. The
     * distance boundaries around the message are defined by the size of
     * the window of this lattice.
     *
     * @param numbers The numbers to work with
     * @param msg The message number
     * @return TRUE if we have no messages around this one
     */
    private boolean emptyBit(final SortedSet<Long> numbers,
        final long msg) {
        final int bit = BitsetLattice.bit(msg);
        final SortedSet<Long> tail = numbers.tailSet(
            BitsetLattice.msg(bit)
        );
        boolean empty = tail.isEmpty();
        try {
            empty |= tail.first() < BitsetLattice.msg(bit + 1);
        } catch (java.util.NoSuchElementException ex) {
            empty = true;
        }
        return empty;
    }

}
