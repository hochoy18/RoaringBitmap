package org.roaringbitmap;
import java.util.*;

interface MinimalCompressedBitset<T> extends Iterable<Integer> {
        public static final int UNSET = -1;  // for universe size
        public MinimalCompressedBitset<T> getInvokingObject();  // ugly Java workaround
        public boolean containsOp( final int x);
        public void addOp( final int x);    
        public void removeOp( final int x);  //optional operation
        // these methods may only be invoked by the dummy "invoking" object
        // they are effectively static methods, which apparently cannot be required in an interface
        public T andOp( final T ... others);
        public T orOp( final T ... others);
        public T xorOp( final T ... others);
        public T andNotOp( final T other);
        public T notOp();
        public void setUniverseSize( final int uSize);
        public int getCardinality();
}