package org.roaringbitmap;

/*
 * Copyright 2013-2014 by Daniel Lemire, Owen Kaser and Samy Chambi
 * Licensed under the Apache License, Version 2.0.
 */
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.junit.Before;

@SuppressWarnings({ "static-method", "javadoc" })
public class TestDeferredRoaring {
        MinimalCompressedBitset pos1246, pos1357, pos5678, neg2347, neg1568, neg0123, dummy;
        @Before 
        public void setup() {
                pos1246 = new DeferredRoaring(false, RoaringBitmap.bitmapOf(1,2,4,6), 10);
                pos1357 = new DeferredRoaring(false, RoaringBitmap.bitmapOf(1,3,5,7), 10);
                pos5678 = new DeferredRoaring(false, RoaringBitmap.bitmapOf(5,6,7,8), 10);
                neg2347 = new DeferredRoaring(true,  RoaringBitmap.bitmapOf(2,3,4,7), 10);
                neg1568 = new DeferredRoaring(true,  RoaringBitmap.bitmapOf(1,5,6,8), 10);
                neg0123 = new DeferredRoaring(true,  RoaringBitmap.bitmapOf(0,1,2,3), 10);
                dummy = pos1246.getInvokingObject();
        }

        
        // equality on contents, array must be sorted
        boolean contains(MinimalCompressedBitset d, int [] desired) {
                //System.out.println("in contains");
                if (d.getCardinality() != desired.length) return false;
                //System.out.println("cardinality "+ d.getCardinality()+ " match");
                int ctr=0;
                for (int i : d)
                        if (desired[ctr++] != i) return false;
                return true;
        }
        
        @Test
        public void testCardinality1() {
                assertEquals(4,pos1246.getCardinality());
        }

        @Test
        public void testCardinality2() {
                assertEquals(6,neg1568.getCardinality());
        }
        
        @Test
        public void testIterator1() {
                assertTrue( contains(pos1246, new int [] {1,2,4,6}));
        }

                        
        @Test
        public void testIterator2() {
                assertTrue( contains(neg1568, new int [] {0,2,3,4,7,9}));
        }

        @Test 
        public void testAddOp1() {
                pos1246.addOp(1); 
                pos1246.addOp(3);
                assertTrue( contains(pos1246, new int [] {1,2,3,4,6}));

        }

        @Test 
        public void testAddOp2() {
                neg1568.addOp(1); 
                neg1568.addOp(3);
                assertTrue( contains(neg1568, new int [] {0,1,2,3,4,7,9}));
        }
                



        @Test 
        public void testRemoveOp1() {
                pos1246.removeOp(1); 
                pos1246.removeOp(3);
                assertTrue( contains(pos1246, new int [] {2,4,6}));

        }

        @Test 
        public void testRemoveOp2() {
                neg1568.removeOp(1); 
                neg1568.removeOp(3);
                assertTrue( contains(neg1568, new int [] {0,2,4,7,9}));
        }

        @Test 
        public void testAndNotOp1() {
                assertTrue( contains(pos1246.andNotOp(pos1357), new int [] {2,4,6}));
        }

        @Test 
        public void testAndNotOp2() {
                assertTrue( contains(pos1246.andNotOp(neg1568), new int [] {1,6}));
        }


        @Test 
        public void testAndNotOp3() {
                assertTrue( contains(neg1568.andNotOp(pos1246), new int [] {0,3,7,9}));
        }


        @Test 
        public void testAndNotOp4() {
                assertTrue( contains(neg1568.andNotOp(neg2347), new int [] {2,3,4,7}));
        }



        @Test 
        public void testNotOp1() {
                assertTrue( contains(neg1568.notOp(), new int [] {1,5,6,8}));
        }


        @Test
        public void testAndOp1() {
                assertTrue( contains(dummy.andOp(pos1246), new int [] {1,2,4,6}));
        }
                


        @Test
        public void testAndOp2() {
                assertTrue( contains(dummy.andOp(pos1246,pos1357), new int [] {1}));
        }
                


        @Test
        public void testAndOp3() {
                assertTrue( contains(dummy.andOp(pos1246,neg1568), new int [] {2,4}));
        }
                


        @Test
        public void testAndOp4() {
                assertTrue( contains(dummy.andOp(neg1568, pos1246), new int [] {2,4}));
        }



        @Test
        public void testAndOp5() {
                assertTrue( contains(dummy.andOp(neg1568, neg2347), new int [] {0,9}));
        }


        @Test
        public void testAndOp6() {
                assertTrue( contains(dummy.andOp(pos1246,pos1357,pos5678), new int [] {}));
        }


        @Test
        public void testAndOp7() {
                assertTrue( contains(dummy.andOp(neg1568,neg2347,neg0123), new int [] {9}));
        }

        @Test
        public void testAndOp8() {
                assertTrue( contains(dummy.andOp(neg2347,neg0123, pos1357), new int [] {5}));
        }


        @Test
        public void testAndOp9() {
                assertTrue( contains(dummy.andOp(neg2347,pos5678, pos1357), new int [] {5}));
        }


        @Test
        public void testOrOp1() {
                assertTrue( contains(dummy.orOp(pos1246), new int [] {1,2,4,6}));
        }
                


        @Test
        public void testOrOp2() {
                assertTrue( contains(dummy.orOp(pos1246,pos1357), new int [] {1,2,3,4,5,6,7}));
        }
                


        @Test
        public void testOrOp3() {
                assertTrue( contains(dummy.orOp(pos1246,neg1568), new int [] {0,1,2,3,4,6,7,9}));
        }
                


        @Test
        public void testOrOp4() {
                assertTrue( contains(dummy.orOp(neg1568,pos1246), new int [] {0,1,2,3,4,6,7,9}));
        }

        @Test
        public void testOrOp5() {
                assertTrue( contains(dummy.orOp(neg1568, neg2347), new int [] {0,1,2,3,4,5,6,7,8,9}));
        }


        @Test
        public void testOrOp6() {
                assertTrue( contains(dummy.orOp(pos1246,pos1357,pos5678), new int [] {1,2,3,4,5,6,7,8}));
        }


        @Test
        public void testOrOp7() {
                assertTrue( contains(dummy.orOp(neg1568,neg2347,neg0123), new int [] {0,1,2,3,4,5,6,7,8,9}));
        }


        public void testOrOp7A() {
                assertTrue( contains(dummy.orOp(neg0123,neg2347,neg0123), new int [] {0,1,4,5,6,7,8,9}));
        }


        @Test
        public void testOrOp8() {
                assertTrue( contains(dummy.orOp(neg2347,neg0123, pos1357), new int [] {0,1,3,4,5,6,7,8,9}));
        }



        @Test
        public void testOrOp9() {
                MinimalCompressedBitset ans = dummy.orOp(neg2347,pos5678, pos1357);
                assertTrue( contains(ans, new int [] {0,1,3,5,6,7,8,9}));
        }



 
        @Test
        public void testXorOp1() {
                assertTrue( contains(dummy.xorOp(pos1246), new int [] {1,2,4,6}));
        }
                


        @Test
        public void testXorOp2() {
                assertTrue( contains(dummy.xorOp(pos1246,pos1357), new int [] {2,3,4,5,6,7}));
        }
                


        @Test
        public void testXorOp3() {
                assertTrue( contains(dummy.xorOp(pos1246,neg1568), new int [] {0,1,3,6,7,9}));
        }
                


        @Test
        public void testXorOp4() {
                assertTrue( contains(dummy.xorOp(neg1568,pos1246), new int [] {0,1,3,6,7,9}));
        }

        @Test
        public void testXorOp5() {
                assertTrue( contains(dummy.xorOp(neg1568, neg2347), new int [] {1,2,3,4,5,6,7,8}));
        }


        @Test
        public void testXorOp6() {
                assertTrue( contains(dummy.xorOp(pos1246,pos1357,pos5678), new int [] {2,3,4,8}));
        }


        @Test
        public void testXorOp7() {
                assertTrue( contains(dummy.xorOp(neg1568,neg2347,neg0123), new int [] {1,2,3,9}));
        }


        public void testXorOp7A() {
                assertTrue( contains(dummy.xorOp(neg0123,neg2347,neg0123), new int [] {0,1,5,6,8,9}));
        }


        @Test
        public void testXorOp8() {
                assertTrue( contains(dummy.xorOp(neg2347,neg0123, pos1357), new int [] {0,3,4,5}));
        }

 
        @Test
        public void testXorOp9() {
                MinimalCompressedBitset ans = dummy.xorOp(neg2347,pos5678, pos1357);
                assertTrue( contains(ans, new int [] {0,3,5,9}));
        }






}