package org.roaringbitmap.buffer;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

import static org.roaringbitmap.RandomisedTestData.TestDataSet.testCase;

public class BufferParallelAggregationTest {
  private static ForkJoinPool POOL;

  private static ForkJoinPool NO_PARALLELISM_AVAILABLE;

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
  
  Random r = new Random(42142);

  @Test
  public void singleContainerOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0,r).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(0,r).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(0,r).build().toMutableRoaringBitmap();
    Assert.assertEquals(BufferFastAggregation.or(one, two, three), BufferParallelAggregation.or(one, two, three));
  }

  @Test
  public void twoContainerOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0,r).withArrayAt(1,r).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(1,r).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(1,r).build().toMutableRoaringBitmap();
    Assert.assertEquals(BufferFastAggregation.or(one, two, three), BufferParallelAggregation.or(one, two, three));
  }

  @Test
  public void disjointOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0,r).withArrayAt(2,r).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(1,r).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(3,r).build().toMutableRoaringBitmap();
    Assert.assertEquals(BufferFastAggregation.or(one, two, three), BufferParallelAggregation.or(one, two, three));
  }

  @Test
  public void disjointBigKeysOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0,r).withArrayAt(2,r).withBitmapAt((1 << 15) | 1,r).build()
            .toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(1,r).withRunAt((1 << 15) | 2,r).build()
            .toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(3,r).withRunAt((1 << 15) | 3,r).build()
            .toMutableRoaringBitmap();
    Assert.assertEquals(BufferFastAggregation.or(one, two, three), BufferParallelAggregation.or(one, two, three));
  }

  @Test
  public void wideOr() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 20)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2, r).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    Assert.assertEquals(BufferFastAggregation.or(input), BufferParallelAggregation.or(input));
  }

  @Test
  public void hugeOr1() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 513)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    Assert.assertEquals(BufferFastAggregation.or(input), BufferParallelAggregation.or(input));
  }


  @Test
  public void hugeOr2() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 1999)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    Assert.assertEquals(BufferFastAggregation.or(input), BufferParallelAggregation.or(input));
  }

  @Test
  public void hugeOr3() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 4096)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    Assert.assertEquals(BufferFastAggregation.or(input), BufferParallelAggregation.or(input));
  }

  @Test
  public void hugeOrNoParallelismAvailable1() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 513)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    Assert.assertEquals(BufferFastAggregation.or(input),
            NO_PARALLELISM_AVAILABLE.submit(() -> BufferParallelAggregation.or(input)).join());
  }


  @Test
  public void hugeOrNoParallelismAvailable2() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 2000)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    Assert.assertEquals(BufferFastAggregation.or(input),
            NO_PARALLELISM_AVAILABLE.submit(() -> BufferParallelAggregation.or(input)).join());
  }

  @Test
  public void hugeOrNoParallelismAvailable3() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 4096)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    Assert.assertEquals(BufferFastAggregation.or(input),
            NO_PARALLELISM_AVAILABLE.submit(() -> BufferParallelAggregation.or(input)).join());
  }


  @Test
  public void hugeOrInFJP1() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 513)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    Assert.assertEquals(BufferFastAggregation.or(input),
            POOL.submit(() -> BufferParallelAggregation.or(input)).join());
  }


  @Test
  public void hugeOrInFJP2() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 2000)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    Assert.assertEquals(BufferFastAggregation.or(input),
            POOL.submit(() -> BufferParallelAggregation.or(input)).join());
  }

  @Test
  public void hugeOrInFJP3() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 4096)
            .mapToObj(i -> testCase().withBitmapAt(0,r).withArrayAt(1,r).withRunAt(2,r).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    Assert.assertEquals(BufferFastAggregation.or(input),
            POOL.submit(() -> BufferParallelAggregation.or(input)).join());
  }

  @Test
  public void singleContainerXOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0,r).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(0,r).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(0,r).build().toMutableRoaringBitmap();
    Assert.assertEquals(BufferFastAggregation.xor(one, two, three), BufferParallelAggregation.xor(one, two, three));
  }


  @Test
  public void missingMiddleContainerXOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0,r).withBitmapAt(1,r).withArrayAt(2,r).build()
            .toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(0,r).withArrayAt(2,r).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(0,r).withBitmapAt(1,r).withArrayAt(2,r).build()
            .toMutableRoaringBitmap();
    Assert.assertEquals(BufferFastAggregation.xor(one, two, three), BufferParallelAggregation.xor(one, two, three));
  }

  @Test
  public void twoContainerXOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0,r).withArrayAt(1,r).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(1,r).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(1,r).build().toMutableRoaringBitmap();
    Assert.assertEquals(BufferFastAggregation.xor(one, two, three), BufferParallelAggregation.xor(one, two, three));
  }

  @Test
  public void disjointXOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0,r).withArrayAt(2,r).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(1,r).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(3,r).build().toMutableRoaringBitmap();
    Assert.assertEquals(BufferFastAggregation.xor(one, two, three), BufferParallelAggregation.xor(one, two, three));
  }

  @Test
  public void disjointBigKeysXOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0,r).withArrayAt(2,r).withBitmapAt((1 << 15) | 1,r).build()
            .toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(1,r).withRunAt((1 << 15) | 2,r).build()
            .toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(3,r).withRunAt((1 << 15) | 3,r).build()
            .toMutableRoaringBitmap();
    Assert.assertEquals(BufferFastAggregation.xor(one, two, three), BufferParallelAggregation.xor(one, two, three));
  }
}



