/*
 * Copyright (c) 2022-2024. Tim Langhammer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package eu.jpangolin.jtzipi.mymod.io.async;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Objects;
import java.util.concurrent.Future;

/**
 * Pre loadable task with a time limit to stay in cache.
 * <p>
 * The Idea is to create a time cache with a 'Time and Date' value
 * and the key to cache.
 * <br/>
 * I decided to use {@link Duration} for the 'Time and Date' abstraction because
 * this is the best way to describe a relative amount of a time in a unit.
 * <br/>
 * Beside this there are methods to set the time and date budget via
 * {@link Temporal} and old school with a {@link ChronoUnit} and an amount.
 * <br/>
 * HINT: To add a key|value pair without time restriction you can use either
 * the {@link Duration#ZERO} constant or if you use a long amount a value
 * &le; 0.
 * </p>
 *
 * @param <K> key
 * @param <V> value
 * @author jTzipi
 */
public interface IPreloadMemoizedTemporal<K, V> extends IPreloadMemoized<K, V> {

    /**
     * Unlimited time budget.
     * <p>
     * All values &lt; 0 are considered as no time restriction.
     * </p>
     */
    long UNLIMITED_TIME_BUDGET = 0L;

    /**
     * Put a key for a duration.
     *
     * @param key      key
     * @param duration duration. If the duration is zero or negative the key is cached without time budget
     * @throws NullPointerException if {@code key} | {@code duration} is null
     */
    void putForDuration( K key, Duration duration );

    /**
     * Put a key for a temporal.
     *
     * @param key      key
     * @param temporal temporal
     * @throws NullPointerException if {@code key} | {@code temporal}
     */
    default void put( K key, Temporal temporal ) {
        Objects.requireNonNull( key );
        Objects.requireNonNull( temporal );

        LocalDateTime now = LocalDateTime.now();

        Duration dur = Duration.between( now, temporal );
        if ( dur.isZero() || dur.isNegative() ) {
            dur = Duration.ZERO;
        }

        putForDuration( key, dur );
    }


    /**
     * Put key for given chrono unit and amount.
     * <p>
     * Attention: Each amount &lt; {@linkplain #UNLIMITED_TIME_BUDGET} is considered
     * </p>
     *
     * @param key        key
     * @param chronoUnit chrono unit
     * @param amount     amount
     */
    default void put( K key, ChronoUnit chronoUnit, long amount ) {
        Objects.requireNonNull( key );
        Objects.requireNonNull( chronoUnit );
        Duration dur;
        if ( UNLIMITED_TIME_BUDGET < amount ) {
            LocalDateTime now = LocalDateTime.now();
            Temporal temporal = now.plus( amount, chronoUnit );
            dur = Duration.between( now, temporal );
        } else {

            dur = Duration.ZERO;
        }
        putForDuration( key, dur );
    }

    /**
     * Default put method.
     * Attention: all keys added without a time budget are considered unbound.
     *
     * @param key argument
     * @throws NullPointerException if {@code key} is null
     */
    default void put( K key ) {
        putForDuration( key, Duration.ZERO );
    }

    Future<V> startAndWaitSince( K key, Temporal temporal );
}