package eu.jpangolin.jtzipi.mymod.utils;

import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Common utils.
 *
 * @author jTzipi
 */
public final class ModUtils {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( ModUtils.class );
    private ModUtils() {

    }

    /**
     * Clamp a value to [{@code min} .. {@code max}].
     * @param val value
     * @param min minimal
     * @param max maximal
     * @return value clamped
     * @param <T> subtype of comparable
     */
    public static <T extends Comparable<? super T>> T clamp( T val, T min, T max ) {
        Objects.requireNonNull( val, "value is null" );
        Objects.requireNonNull( min, "min value is null" );
        Objects.requireNonNull( max, "max value is null" );

        final T ret;

        if ( max.compareTo( val ) < 0 ) {

            ret = max;
        } else if ( min.compareTo( val ) > 0 ) {
            ret = min;
        } else {
            ret = val;
        }

        return ret;
    }

    /**
     * Coerce an unchecked Throwable to a RuntimeException.
     * <p>
     * <p>
     * If the Throwable is an Error, throw it; if it is a
     * RuntimeException return it, otherwise throw IllegalStateException.
     * <p>
     * Author: Brian Goetz
     * @param t launderThrowable
     * @return exception
     * @throws IllegalStateException if {@code t} is not of type {@link RuntimeException} or {@link Error}
     * @throws NullPointerException if {@code t} is null
     */
    public static RuntimeException launderThrowable( Throwable t ) {
        Objects.requireNonNull( t );
        if ( t instanceof RuntimeException )
            return ( RuntimeException ) t;
        else if ( t instanceof Error )
            throw ( Error ) t;
        else
            throw new IllegalStateException( "Not unchecked", t );
    }
}
