package org.roaringbitmap;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

import static org.roaringbitmap.RandomisedTestData.TestDataSet.testCase;

public class ParallelAggregationTest {

  private static ForkJoinPool POOL;

  private static ForkJoinPool NO_PARALLELISM_AVAILABLE;
  Random r = new Random(1245);

  @BeforeClass
  public static void init() {
    POOL = new ForkJoinPool(4);
    NO_PARALLELISM_AVAILABLE = new ForkJoinPool(1);
  }

  @AfterClass
  public static void teardown() {
    POOL.shutdownNow();
    NO_PARALLELISM_AVAILABLE.shutdownNow();
  }
  

  @Test
  public void singleContainerOR() {
    RoaringBitmap one = testCase().withRunAt(0,r).build();
    RoaringBitmap two = testCase().withBitmapAt(0,r).build();
    RoaringBitmap three = testCase().withArrayAt(0,r).build();
    Assert.assertEquals(FastAggregation.or(one, two, three), ParallelAggregation.or(one, two, three));
  }

  @Test
  public void twoContainerOR() {
    RoaringBitmap one = testCase().withRunAt(0,r).withArrayAt(1,r).build();
    RoaringBitmap two = testCase().withBitmapAt(1,r).build();
    RoaringBitmap three = testCase().withArrayAt(1,r).build();
    Assert.assertEquals(FastAggregation.or(one, two, three), ParallelAggregation.or(one, two, three));
  }

  @Test
  public void disjointOR() {
    RoaringBitmap one = testCase().withRunAt(0,r).withArrayAt(2,r).build();
    RoaringBitmap two = testCase().withBitmapAt(1,r).build();
    RoaringBitmap three = testCase().withArrayAt(3,r).build();
    Assert.assertEquals(FastAggregation.or(one, two, three), ParallelAggregation.or(one, two, three));
  }


  @Test
  public void wideOr() {
    RoaringBitmap[] input = IntStream.range(0, 20)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build())
            .toArray(RoaringBitmap[]::new);
    Assert.assertEquals(FastAggregation.or(input), ParallelAggregation.or(input));
  }

  @Test
  public void hugeOr1() {
    RoaringBitmap[] input = IntStream.range(0, 513)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build())
            .toArray(RoaringBitmap[]::new);
    Assert.assertEquals(FastAggregation.or(input), ParallelAggregation.or(input));
  }


  @Test
  public void hugeOr2() {
    RoaringBitmap[] input = IntStream.range(0, 1999)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build())
            .toArray(RoaringBitmap[]::new);
    Assert.assertEquals(FastAggregation.or(input), ParallelAggregation.or(input));
  }

  @Test
  public void hugeOr3() {
    RoaringBitmap[] input = IntStream.range(0, 4096)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build())
            .toArray(RoaringBitmap[]::new);
    Assert.assertEquals(FastAggregation.or(input), ParallelAggregation.or(input));
  }

  @Test
  public void hugeOrNoParallelismAvailable1() {
    RoaringBitmap[] input = IntStream.range(0, 513)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build())
            .toArray(RoaringBitmap[]::new);
    Assert.assertEquals(FastAggregation.or(input),
            NO_PARALLELISM_AVAILABLE.submit(() -> ParallelAggregation.or(input)).join());
  }


  @Test
  public void hugeOrNoParallelismAvailable2() {
    RoaringBitmap[] input = IntStream.range(0, 2000)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build())
            .toArray(RoaringBitmap[]::new);
    Assert.assertEquals(FastAggregation.or(input),
            NO_PARALLELISM_AVAILABLE.submit(() -> ParallelAggregation.or(input)).join());
  }

  @Test
  public void hugeOrNoParallelismAvailable3() {
    RoaringBitmap[] input = IntStream.range(0, 4096)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build())
            .toArray(RoaringBitmap[]::new);
    Assert.assertEquals(FastAggregation.or(input),
            NO_PARALLELISM_AVAILABLE.submit(() -> ParallelAggregation.or(input)).join());
  }


  @Test
  public void hugeOrInFJP1() {
    RoaringBitmap[] input = IntStream.range(0, 513)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build())
            .toArray(RoaringBitmap[]::new);
    Assert.assertEquals(FastAggregation.or(input),
                        POOL.submit(() -> ParallelAggregation.or(input)).join());
  }


  @Test
  public void hugeOrInFJP2() {
    RoaringBitmap[] input = IntStream.range(0, 2000)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build())
            .toArray(RoaringBitmap[]::new);
    Assert.assertEquals(FastAggregation.or(input),
            POOL.submit(() -> ParallelAggregation.or(input)).join());
  }

  @Test
  public void hugeOrInFJP3() {
    RoaringBitmap[] input = IntStream.range(0, 4096)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build())
            .toArray(RoaringBitmap[]::new);
    Assert.assertEquals(FastAggregation.or(input),
            POOL.submit(() -> ParallelAggregation.or(input)).join());
  }

  @Test
  public void disjointBigKeysOR() {
    RoaringBitmap one = testCase().withRunAt(0,r).withArrayAt(2,r).withBitmapAt((1 << 15) | 1,r).build();
    RoaringBitmap two = testCase().withBitmapAt(1,r).withRunAt((1 << 15) | 2,r).build();
    RoaringBitmap three = testCase().withArrayAt(3,r).withRunAt((1 << 15) | 3,r).build();
    Assert.assertEquals(FastAggregation.or(one, two, three), ParallelAggregation.or(one, two, three));
  }

  @Test
  public void singleContainerXOR() {
    RoaringBitmap one = testCase().withRunAt(0,r).build();
    RoaringBitmap two = testCase().withBitmapAt(0,r).build();
    RoaringBitmap three = testCase().withArrayAt(0,r).build();
    Assert.assertEquals(FastAggregation.xor(one, two, three), ParallelAggregation.xor(one, two, three));
  }


  @Test
  public void missingMiddleContainerXOR() {
    RoaringBitmap one = testCase().withRunAt(0,r).withBitmapAt(1,r).withArrayAt(2,r).build();
    RoaringBitmap two = testCase().withBitmapAt(0,r).withArrayAt(2,r).build();
    RoaringBitmap three = testCase().withArrayAt(0,r).withBitmapAt(1,r).withArrayAt(2,r).build();
    Assert.assertEquals(FastAggregation.xor(one, two, three), ParallelAggregation.xor(one, two, three));
  }

  @Test
  public void twoContainerXOR() {
    RoaringBitmap one = testCase().withRunAt(0,r).withArrayAt(1,r).build();
    RoaringBitmap two = testCase().withBitmapAt(1,r).build();
    RoaringBitmap three = testCase().withArrayAt(1,r).build();
    Assert.assertEquals(FastAggregation.xor(one, two, three), ParallelAggregation.xor(one, two, three));
  }

  @Test
  public void disjointXOR() {
    RoaringBitmap one = testCase().withRunAt(0,r).withArrayAt(2,r).build();
    RoaringBitmap two = testCase().withBitmapAt(1,r).build();
    RoaringBitmap three = testCase().withArrayAt(3,r).build();
    Assert.assertEquals(FastAggregation.xor(one, two, three), ParallelAggregation.xor(one, two, three));
  }

  @Test
  public void disjointBigKeysXOR() {
    RoaringBitmap one = testCase().withRunAt(0,r).withArrayAt(2,r).withBitmapAt((1 << 15) | 1,r).build();
    RoaringBitmap two = testCase().withBitmapAt(1,r).withRunAt((1 << 15) | 2,r).build();
    RoaringBitmap three = testCase().withArrayAt(3,r).withRunAt((1 << 15) | 3,r).build();
    Assert.assertEquals(FastAggregation.xor(one, two, three), ParallelAggregation.xor(one, two, three));
  }


}