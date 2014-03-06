package org.roaringbitmap;
import java.util.*;

public interface MinimalCompressedBitset extends Iterable<Integer> {
        public static final int UNSET = -1;  // for universe size
        public MinimalCompressedBitset getInvokingObject();  // ugly Java workaround
        public boolean containsOp( final int x);
        public void addOp( final int x);    
        public void removeOp( final int x);  //optional operation
        // these methods may only be invoked by the dummy "invoking" object
        // they are effectively static methods, which apparently cannot be required in an interface
        public MinimalCompressedBitset andOp( final MinimalCompressedBitset ... others);
        public MinimalCompressedBitset orOp( final MinimalCompressedBitset ... others);
        public MinimalCompressedBitset xorOp( final MinimalCompressedBitset ... others);
        public MinimalCompressedBitset andNotOp( final MinimalCompressedBitset other);
        public MinimalCompressedBitset notOp();
        public void setUniverseSize( final int uSize);
        public int getCardinality();
}