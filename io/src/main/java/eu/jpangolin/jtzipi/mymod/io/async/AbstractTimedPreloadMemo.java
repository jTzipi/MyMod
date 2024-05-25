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

import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Abstract Timed Preload Memo.
 * <p>
 *
 * </p>
 *
 * @param <K> key
 * @param <V> value
 */
public abstract class AbstractTimedPreloadMemo<K, V> implements IPreloadMemoizedTemporal<K, V> {

    static final org.slf4j.Logger LOG = LoggerFactory.getLogger( AbstractTimedPreloadMemo.class );
    private final BlockingQueue<TemporalItem<K>> delayQ = new DelayQueue<>();
    // -- Cache --
    private final ConcurrentMap<K, Future<V>> cMap = new ConcurrentHashMap<>();
    // -- Executor for tasks --
    private final ExecutorService exeSe;
    // -- loop and wait for next expiring entry
    private final Runnable loopy = () -> {

        while ( !Thread.currentThread().isInterrupted() ) {

            try {
                TemporalItem<K> tempi = delayQ.take();
                LOG.info( "Remove key '{}' from delay queue", tempi.key );
                boolean removed = cMap.remove( tempi.key, tempi.computation );
                LOG.info( "and also from cache? {}", removed );
            } catch ( InterruptedException iE ) {

                Thread.currentThread().interrupt();
            }

        }

    };
    private Thread loopThread;

    protected AbstractTimedPreloadMemo( final ExecutorService executorService ) {
        this.exeSe = executorService;

    }

    void startWatch() {
        loopThread = new Thread( loopy );
        loopThread.setDaemon( true );
        loopThread.start();
    }

    /**
     * This is your code to implement.
     *
     * @param arg argument
     * @return value
     */
    protected abstract V compute( final K arg );

    @Override
    public void putForDuration( K key, Duration duration ) {
        Objects.requireNonNull( key, "Null is not allowed" );
        if ( null == duration ) {
            duration = Duration.ZERO;
        }
        LOG.info( "try to get future for key '{}' with time budget since {}", key, duration );


        // THIS IS THE APPROACH IF WE WANT TO RETURN THE FUTURE
        // cMap.computeIfAbsent( arg, key -> exeSe.submit( () -> compute( arg ) ) );

        Future<V> f = cMap.get( key );
        LOG.info( "Look for existing entry for key -> '{}'", f );

        if ( null == f ) {


            LOG.info( "Start computation for key '{}'", key );

            //
            // If comp is null this thread is the first thread who start the computation
            //
            Future<V> comp = cMap.putIfAbsent( key, exeSe.submit( () -> compute( key ) ) );
            if ( null == comp && duration != Duration.ZERO ) {

                //
                // We have the first thread and the duration is not unlimited
                // -> offer the tempKey to our delay queue
                // CAUTION: here is a little window of vulnerability that
                // another thread remove the entry previously added to out cache
                // but this should be no problem in practice
                TemporalItem<K> tempKey = new TemporalItem<>( key, cMap.get( key ), duration );
                LOG.info( "Put temp key='{}'", tempKey );
                boolean offered = delayQ.offer( tempKey );
                LOG.info( "done ? {}", offered );
                if ( null == loopThread ) {
                    startWatch();
                }
            }
        } else {
            LOG.warn( "Try to start computation for already known key '{}'", key );
        }
    }

    private record TemporalItem<K>(K key, Future<?> computation, Duration dur) implements Delayed {
        @Override
        public int compareTo( Delayed o ) {
            Objects.requireNonNull( o );
            if ( o instanceof AbstractTimedPreloadMemo.TemporalItem<?> that ) {
                return this.dur.compareTo( that.dur );
            }
            throw new ClassCastException( "No supported Delayed[='" + o.getClass() + "']" );
        }

        @Override
        public long getDelay( TimeUnit unit ) {
            return unit.convert( dur );
        }

    }
}