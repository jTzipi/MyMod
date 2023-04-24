package eu.jpangolin.jtzipi.mymod.utils;

import java.util.Comparator;

public record Range<T extends Number & Comparable<? super T>>( T min, T max ) {

    public Range( T min, T max) {
        if( min.compareTo(max) >= 0 ) {
            throw new IllegalArgumentException("min[''] is < or <= max['']");
        }
        this.min = min;
        this.max = max;
    }
}
