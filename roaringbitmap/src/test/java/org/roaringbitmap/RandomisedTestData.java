package org.roaringbitmap;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import static org.roaringbitmap.RoaringBitmapWriter.writer;

public class RandomisedTestData {

  private static final ThreadLocal<long[]> bits = ThreadLocal.withInitial(() -> new long[1 << 10]);

  public static RoaringBitmap randomBitmap(int maxKeys, double rleLimit, double denseLimit, Random r) {
    return randomBitmap(maxKeys, rleLimit, denseLimit, writer().initialCapacity(maxKeys).optimiseForArrays().get(), r);
  }

  public static <T extends BitmapDataProvider> T randomBitmap(int maxKeys,
                                                              double rleLimit,
                                                              double denseLimit,
                                                              RoaringBitmapWriter<T> writer, Random r) {
    int[] keys = createSorted16BitInts(r.nextInt(maxKeys - 1) + 1, r);
    IntStream.of(keys)
            .forEach(key -> {
              double choice = r.nextDouble();
              final IntStream stream;
              if (choice < rleLimit) {
                stream = rleRegion(r);
              } else if (choice < denseLimit) {
                stream = denseRegion(r);
              } else {
                stream = sparseRegion(r);
              }
              stream.map(i -> (key << 16) | i).forEach(writer::add);
            });
    return writer.get();
  }

  public static RoaringBitmap randomBitmap(int maxKeys, Random r) {
    double rleLimit = r.nextDouble();
    double denseLimit = r.nextDouble() * (1D - rleLimit);
    return randomBitmap(maxKeys, rleLimit, denseLimit, r);
  }

  public static <T extends BitmapDataProvider> T randomBitmap(int maxKeys, RoaringBitmapWriter<T> writer, Random r) {
    double rleLimit = r.nextDouble();
    double denseLimit = r.nextDouble() * (1D - rleLimit);
    return randomBitmap(maxKeys, rleLimit, denseLimit, writer, r);
  }

  public static IntStream rleRegion(Random r) {
    int numRuns = r.nextInt(2048 - 1) + 1;
    int[] runs = createSorted16BitInts(numRuns * 2, r);
    return IntStream.range(0, numRuns)
            .map(i -> i * 2)
            .mapToObj(i -> IntStream.range(runs[i], runs[i + 1]))
            .flatMapToInt(i -> i);
  }

  public static IntStream sparseRegion(Random r) {
    return IntStream.of(createSorted16BitInts(r.nextInt(4096 - 1) + 1, r));
  }


  public static IntStream denseRegion(Random r) {
    return IntStream.of(createSorted16BitInts(r.nextInt(1 << 16 - 4096), r));
  }

  private static int[] createSorted16BitInts(int howMany, Random r) {
    // we can have at most 65536 keys in a RoaringBitmap
    long[] bitset = bits.get();
    Arrays.fill(bitset, 0L);
    int consumed = 0;
    while (consumed < howMany) {
      int value = r.nextInt(1 << 16);
      long bit = (1L << value);
      consumed += 1 - Long.bitCount(bitset[value >>> 6] & bit);
      bitset[value >>> 6] |= bit;
    }
    int[] keys = new int[howMany];
    int prefix = 0;
    int k = 0;
    for (int i = bitset.length - 1; i >= 0; --i) {
      long word = bitset[i];
      while (word != 0) {
        keys[k++] = prefix + Long.numberOfTrailingZeros(word);
        word ^= Long.lowestOneBit(word);
      }
      prefix += 64;
    }
    return keys;
  }

  public static class TestDataSet {


    public static TestDataSet testCase() {
      return new TestDataSet();
    }

    RoaringBitmapWriter<RoaringBitmap> writer = RoaringBitmapWriter.writer().constantMemory().get();

    public TestDataSet withRunAt(int key, Random r) {
      assert key < 1 << 16;
      rleRegion(r).map(i -> (key << 16) | i).forEach(writer::add);
      return this;
    }

    public TestDataSet withArrayAt(int key, Random r) {
      assert key < 1 << 16;
      sparseRegion(r).map(i -> (key << 16) | i).forEach(writer::add);
      return this;
    }

    public TestDataSet withBitmapAt(int key, Random r) {
      assert key < 1 << 16;
      denseRegion(r).map(i -> (key << 16) | i).forEach(writer::add);
      return this;
    }

    public TestDataSet withRange(long minimum, long supremum) {
      writer.add(minimum, supremum);
      return this;
    }

    public RoaringBitmap build() {
      writer.flush();
      return writer.getUnderlying();
    }
  }
}
