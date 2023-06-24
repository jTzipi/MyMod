/*
 *    Copyright (c) 2022-2023 Tim Langhammer
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.jpangolin.jtzipi.mymod.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Security;
import java.util.Objects;

/**
 * Common utils.
 *
 * @author jTzipi
 */
public final class ModUtils {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( ModUtils.class );

    /**
     * Minimal Opacity.
     */
    public static final double MIN_OPACITY = 0D;
    /**
     * Maximal Opacity.
     */
    public static final double MAX_OPACITY = 1D;
    /**
     * Minimal Rotation Degree.
     */
    public static final double MIN_ROTATION = -360D;
    /**
     * Maximal Rotation Degree.
     */
    public static final double MAX_ROTATION = 360D;


    private ModUtils() {

    }

    /**
     * Clamp a value to [{@code min} .. {@code max}].
     *
     * @param val value
     * @param min minimal
     * @param max maximal
     * @param <T> subtype of comparable
     * @return value clamped
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
     * Round a double value to 'nc' positions rounding half up.
     *
     * @param value value
     * @param nc    position
     * @return rounded value
     */
    public static double round( double value, int nc ) {
        return round( value, nc, RoundingMode.HALF_UP );
    }

    /**
     * Round a double to value to 'nc' positions.
     *
     * @param value value
     * @param nc    position after .
     * @param rmode rounding mode if
     * @return rounded value
     */
    public static double round( double value, int nc, RoundingMode rmode ) {
        if ( null == rmode ) {
            rmode = RoundingMode.HALF_UP;
        }
        nc = Math.max( 0, nc );
        BigDecimal bd = new BigDecimal( Double.toString( value ) );
        bd = bd.setScale( nc, rmode );
        return bd.doubleValue();
    }

    /**
     * Register 'Bouncy Castle' Provider.
     */
    public static void registerBouncyCastleProvider() {

        int pp = Security.addProvider( new BouncyCastleProvider() );
        LOG.info( "Added BC Provider on position {}", pp );
        if ( -1 == pp ) {
            LOG.warn( "BC Provider already installed" );
        }
    }

    /**
     * Coerce an unchecked Throwable to a RuntimeException.
     * <p>
     * <p>
     * If the Throwable is an Error, throw it; if it is a
     * RuntimeException return it, otherwise throw IllegalStateException.
     * <p>
     *
     * @param t launderThrowable
     * @return exception
     * @throws IllegalStateException if {@code t} is not of type {@link RuntimeException} or {@link Error}
     * @throws NullPointerException  if {@code t} is null
     * @author: Brian Goetz
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