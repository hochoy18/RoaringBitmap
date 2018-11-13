package org.roaringbitmap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static org.roaringbitmap.RandomisedTestData.TestDataSet.testCase;

@RunWith(Parameterized.class)
public class RoaringBitmapBatchIteratorTest {

    @Parameterized.Parameters
    public static Object[][] params() {
    	Random r = new Random(4214);
        return new Object[][] {
                {testCase().withArrayAt(0,r).withArrayAt(2,r).withArrayAt(4,r).withArrayAt((1 << 15) | (1 << 14),r).build()},
                {testCase().withRunAt(0,r).withRunAt(2,r).withRunAt(4,r).withRunAt((1 << 15) | (1 << 14),r).build()},
                {testCase().withBitmapAt(0,r).withRunAt(2,r).withBitmapAt(4,r).withBitmapAt((1 << 15) | (1 << 14),r).build()},
                {testCase().withArrayAt(0,r).withBitmapAt(2,r).withRunAt(4,r).withBitmapAt((1 << 15) | (1 << 14),r).build()},
                {testCase().withRunAt(0,r).withArrayAt(2,r).withBitmapAt(4,r).withRunAt((1 << 15) | (1 << 14),r).build()},
                {testCase().withBitmapAt(0,r).withRunAt(2,r).withArrayAt(4,r).withBitmapAt((1 << 15) | (1 << 14),r).build()},
                {testCase().withArrayAt(0,r).withBitmapAt(2,r).withRunAt(4,r).withArrayAt((1 << 15) | (1 << 14),r).build()},
                {testCase().withBitmapAt(0,r).withArrayAt(2,r).withBitmapAt(4,r).withRunAt((1 << 15) | (1 << 14),r).build()},
                {testCase().withRunAt((1 << 15) | (1 << 11), r).withBitmapAt((1 << 15) | (1 << 12),r).withArrayAt((1 << 15) | (1 << 13),r).withBitmapAt((1 << 15) | (1 << 14),r).build()},
                {RoaringBitmap.bitmapOf(IntStream.range(1 << 10, 1 << 26).filter(i -> (i & 1) == 0).toArray())},
                {RoaringBitmap.bitmapOf(IntStream.range(1 << 10, 1 << 25).filter(i -> ((i >>> 8) & 1) == 0).toArray())},
                {new RoaringBitmap()}
        };
    }

    private final RoaringBitmap bitmap;

    public RoaringBitmapBatchIteratorTest(RoaringBitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Test
    public void testBatchIterator256() {
        test(256);
    }


    @Test
    public void testBatchIterator1024() {
        test(1024);
    }


    @Test
    public void testBatchIterator65536() {
        test(65536);
    }


    @Test
    public void testBatchIterator8192() {
        test(8192);
    }

    @Test
    public void testBatchIteratorRandom() {
        IntStream.range(0, 10).map(i -> ThreadLocalRandom.current().nextInt(0, 1 << 16))
                .forEach(this::test);
    }

    private void test(int batchSize) {
        int[] buffer = new int[batchSize];
        RoaringBitmap result = new RoaringBitmap();
        RoaringBatchIterator it = bitmap.getBatchIterator();
        int cardinality = 0;
        while (it.hasNext()) {
            int batch = it.nextBatch(buffer);
            for (int i = 0; i < batch; ++i) {
                result.add(buffer[i]);
            }
            cardinality += batch;
        }
        Assert.assertEquals(bitmap, result);
        Assert.assertEquals(bitmap.getCardinality(), cardinality);
    }

}
