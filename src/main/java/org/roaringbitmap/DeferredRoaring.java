/*
 * Copyright 2013-2014 by Daniel Lemire, Owen Kaser and Samy Chambi
 * Licensed under the Apache License, Version 2.0.
 */

/** 
 * Postpones actual bitwise negation as long as possible (until output to be iterated) 
 * Idea could be pushed into individual Roaring containers, at some overhead
 * and some methods might be hard to support (basic ones are easy enough).
 * equals might be a pain. 
 *
 * Big exercise in Boolean equivalances such as DeMorgan's Laws.
 * 
 * Since we do not actually flip bits, it is possible to bound the number of bits
 * according to the number of set bits in the inputs.  This *might* be useful to
 * argue that negation isn't more evil than other operations for formats like
 * Roaring, where the storage cost is Theta (set bits)  (between setBits/64
 * and setBits/2) for Roaring.
 */
package org.roaringbitmap;
import java.util.*;

public class DeferredRoaring implements MinimalCompressedBitset<DeferredRoaring> {
        private boolean isNegated;
        private int universeSize = UNSET;
        private static final  DeferredRoaring dummyObject = new DeferredRoaring(false,new RoaringBitmap(),UNSET);
        private final RoaringBitmap rb;

        public DeferredRoaring( boolean inv, RoaringBitmap values, int uSize) {
                isNegated = inv;
                rb = values.clone();
                universeSize = uSize;
        }

        public DeferredRoaring() {
                this(false, new RoaringBitmap(), UNSET);
        }

        public void setUniverseSize( final int uSize) {
                universeSize = uSize;
        }

        public void addOp( final int x) {
                if (isNegated) rb.remove(x);
                else rb.add(x);
        }

        public void removeOp(final int x) {
                if (isNegated) rb.add(x);
                else rb.remove(x);
        }

        public boolean containsOp( final int x) {
                return isNegated ^ rb.contains(x);
        }


        /** not "in place", the input bitmaps are unaltered */

        public DeferredRoaring andNotOp(final DeferredRoaring  other) {
                if (!isNegated && !other.isNegated)
                        return new DeferredRoaring(false, RoaringBitmap.andNot(rb, other.rb), universeSize);
                if (isNegated && other.isNegated)
                        return new DeferredRoaring(false, RoaringBitmap.andNot(other.rb, rb), universeSize);
                if (isNegated && !other.isNegated)
                        return new DeferredRoaring(true, RoaringBitmap.or(rb, other.rb), universeSize);
                if (!isNegated && other.isNegated)
                        return new DeferredRoaring(false, RoaringBitmap.and(rb, other.rb), universeSize);
                return null;  // unreachable
        }

        public DeferredRoaring notOp() {
                return new DeferredRoaring( !isNegated, rb, universeSize);
        }


        /** none of the wide logical operations are "in place"; "this" is unused 
         * so we effectively have a static method- real static methods cannot be
         * required in an interface (yet). 
         */

        public DeferredRoaring getInvokingObject() { return dummyObject;}

        // these methods may only be invoked by the dummy "invoking" object
        // they are effectively static methods, which apparently  cannot yet be required in an interface


        public DeferredRoaring andOp(final DeferredRoaring ... others) {
                assert this == dummyObject;  // else API user is confused
                if (others.length == 2) {
                        if (!others[0].isNegated && !others[1].isNegated)
                                return new DeferredRoaring(false, RoaringBitmap.and(others[0].rb, others[1].rb), others[0].universeSize);
                        if (others[0].isNegated && others[1].isNegated)
                                return new DeferredRoaring(true, RoaringBitmap.or(others[0].rb, others[1].rb), others[0].universeSize);
                        if (others[0].isNegated && !others[1].isNegated)
                                return new DeferredRoaring(false, RoaringBitmap.andNot(others[1].rb, others[0].rb), others[0].universeSize);             
                        if (!others[0].isNegated && others[1].isNegated)
                                return new DeferredRoaring(false, RoaringBitmap.andNot(others[0].rb, others[1].rb), others[0].universeSize);             
                }
                else {
                        ArrayList<RoaringBitmap> positives = new ArrayList<RoaringBitmap>();
                        ArrayList<RoaringBitmap> negatives = new ArrayList<RoaringBitmap>();
                        for (DeferredRoaring dr : others)
                                if (dr.isNegated)
                                        negatives.add(dr.rb);
                                else
                                        positives.add(dr.rb);
                        if (negatives.size() == 0)
                                return new DeferredRoaring(false, FastAggregation.and( positives.toArray(new RoaringBitmap[0])), others[0].universeSize);
                        if (positives.size() == 0)
                                return new DeferredRoaring(true, FastAggregation.or( negatives.toArray(new RoaringBitmap[0])), others[0].universeSize);
                        RoaringBitmap pos;
                        if (positives.size() == 1)
                                 pos = positives.get(0);
                        else 
                                pos = FastAggregation.and( positives.toArray( new RoaringBitmap[0]));
                        RoaringBitmap disjOfNegs = FastAggregation.or( negatives.toArray( new RoaringBitmap[0]));
                        return new DeferredRoaring( false, RoaringBitmap.andNot(pos, disjOfNegs), others[0].universeSize);
                }
                return null;  // unreachable
        }

        public DeferredRoaring orOp(final DeferredRoaring ... others) {
                assert this == dummyObject;
                if (others.length == 2) {
                        if (!others[0].isNegated && !others[1].isNegated)
                                return new DeferredRoaring(false, RoaringBitmap.or(others[0].rb, others[1].rb), others[0].universeSize);
                        if (others[0].isNegated && others[1].isNegated)
                                return new DeferredRoaring(true, RoaringBitmap.and(others[0].rb, others[1].rb), others[0].universeSize);
                        if (others[0].isNegated && !others[1].isNegated)
                                return new DeferredRoaring(true, RoaringBitmap.andNot(others[0].rb, others[1].rb), others[0].universeSize);             
                        if (!others[0].isNegated && others[1].isNegated)
                                return new DeferredRoaring(true, RoaringBitmap.andNot(others[1].rb, others[0].rb), others[0].universeSize);             
                }
                else {
                        ArrayList<RoaringBitmap> positives = new ArrayList<RoaringBitmap>();
                        ArrayList<RoaringBitmap> negatives = new ArrayList<RoaringBitmap>();
                        for (DeferredRoaring dr : others)
                                if (dr.isNegated)
                                        negatives.add(dr.rb);
                                else
                                        positives.add(dr.rb);
                        if (negatives.size() == 0)
                                return new DeferredRoaring(false, FastAggregation.or( positives.toArray(new RoaringBitmap[0])), others[0].universeSize);
                        if (positives.size() == 0)
                                return new DeferredRoaring(true, FastAggregation.and( negatives.toArray(new RoaringBitmap[0])), others[0].universeSize);
                        RoaringBitmap neg;
                        // a1 + a2 + not(a3) + not(a4) ==> not ( (a3*a4) andnot (a1+a2))
                        if (negatives.size() == 1)
                                neg = negatives.get(0);
                        else 
                                neg = FastAggregation.and( negatives.toArray( new RoaringBitmap[0]));
                        RoaringBitmap disjOfPositives = FastAggregation.or( positives.toArray( new RoaringBitmap[0]));
                        return new DeferredRoaring( true, RoaringBitmap.andNot(neg, disjOfPositives), others[0].universeSize);
                }
                return null; // unreachable
        }

        public DeferredRoaring xorOp(final DeferredRoaring ... others) {
                assert this == dummyObject;
                if (others.length == 2) {
                        return new DeferredRoaring(others[0].isNegated ^ others[1].isNegated, RoaringBitmap.xor(others[0].rb, others[1].rb), others[0].universeSize);
                }
                else {
                        boolean parity = false;
                        RoaringBitmap [] rbs = new RoaringBitmap[others.length];
                        
                        int ctr=0;
                        for (DeferredRoaring dr : others) {
                                parity ^= dr.isNegated;
                                rbs[ctr++] = dr.rb;
                        }
                        return new DeferredRoaring( parity, FastAggregation.xor(rbs), others[0].universeSize);
                }
        }


        public int getCardinality() {
                assert universeSize != UNSET;
                return isNegated ? (universeSize - rb.getCardinality()) : rb.getCardinality();
        }

        public Iterator<Integer> iterator() {
                if (! isNegated) return rb.iterator();

                assert universeSize != UNSET;
                final int upperBound = universeSize;

                return (new Iterator<Integer>() {
                        int nextVal=-1;
                        Iterator<Integer> unwanted = rb.iterator();
                        int curUnwanted;  //  <= nextVal in advance
                        
                        private Iterator<Integer> setup() {
                                curUnwanted = unwanted.hasNext() ? unwanted.next() : -1;
                                advance();
                                return this;
                        }

                        public boolean hasNext() { return nextVal != -1;}

                        public void remove() {
                                throw new  UnsupportedOperationException("remove in DeferredRoaring iterator");
                        }

                        public Integer next() {
                                if (hasNext()) {
                                        int ans = nextVal;
                                        advance();
                                        return ans;
                                }
                                nextVal = -1;
                                return nextVal;
                        }
                        
                        private void advance() {
                                do {
                                        ++nextVal;
                                        if (nextVal < universeSize)
                                                if (nextVal == curUnwanted)
                                                        curUnwanted = unwanted.hasNext() ? unwanted.next() : -1;
                                                else break;
                                } while (nextVal  < universeSize);
                                if (nextVal >= universeSize)
                                        nextVal = -1;
                        }
                        }).setup();
        }
}

        


        

            









