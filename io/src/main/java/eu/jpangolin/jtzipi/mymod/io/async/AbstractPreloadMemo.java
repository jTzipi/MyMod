/*
 * Copyright (c) 2022 Tim Langhammer
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

package eu.jpangolin.jtzipi.mymod.io.async;

import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Abstract Implementation of PreloaderMemoizer.
 * <p>
 * Here we use a {@linkplain ConcurrentMap} to cache the argument.
 * <br/>
 * <br/>
 * To launch threads we use a ExecutorService.
 * This can be client provided.
 * Default is {@linkplain Executors#newCachedThreadPool()}
 * To stop running threads we have two methods.
 * <br/>
 * One with timeout ({@linkplain AbstractPreloadMemo#stop(TimeUnit, long)}
 * One stops immediately ({@linkplain AbstractPreloadMemo#stopNow()} )
 * <br/>
 * The {@code launderThrowable} method is part of the {@code utils} package.
 *
 * </p>
 * <p>
 * We use an {@link ExecutorService} to run the computation.
 * <br/>
 * This was inspired by the great book "Java Concurrency in practice".
 * See page 97 for 'preloader' and page 108 for 'memoizer'.
 * For more code see <a href="https://jcip.net/" alt="JCIP homepage">here</a>.
 * </p>
 *
 * @param <K> key
 * @param <V> value
 * @author jTzipi
 */
public abstract class AbstractPreloadMemo<K, V> implements IPreloadMemoized<K, V> {

    static final org.slf4j.Logger LOG = LoggerFactory.getLogger( AbstractPreloadMemo.class );

    // ExecutorService
    private final ExecutorService exeSe;
    // Cache
    private final ConcurrentMap<K, Future<V>> cMap = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param executorService exec service
     */
    public AbstractPreloadMemo( ExecutorService executorService ) {

        this.exeSe = null == executorService ? Executors.newCachedThreadPool() : executorService;
    }

    /**
     * Default constructor.
     */
    public AbstractPreloadMemo() {
        this( Executors.newCachedThreadPool() );
    }


    /**
     * This is your code to implement.
     *
     * @param arg argument
     * @return value
     */
    protected abstract V compute( final K arg );


    /**
     * Try to finish all running tasks and shutdown.
     *
     * @param timeoutUn time unit
     * @param time      time
     * @return {@code true} if executor shut down
     * @throws InterruptedException if await
     */
    public boolean stop( TimeUnit timeoutUn, long time ) throws InterruptedException {

        if ( null == timeoutUn ) {
            timeoutUn = TimeUnit.SECONDS;
        }
        time = Math.max( 0L, time );
        exeSe.shutdown();
        return exeSe.awaitTermination( time, timeoutUn );
    }

    /**
     * Stop this preloader without waiting.
     * <p>
     * This stop all running comp.
     * </p>
     *
     * @return Tasks not finished
     */
    public List<Runnable> stopNow() {

        return exeSe.shutdownNow();
    }

    @Override
    public void put( final K arg ) {

        if ( null == arg ) {
            throw new IllegalArgumentException( "Null is not allowed" );
        }

        LOG.info( "try to get future for key '" + arg + "'" );

        // THIS IS THE APPROACH IF WE WANT TO RETURN THE FUTURE
        // cMap.computeIfAbsent( arg, key -> exeSe.submit( () -> compute( arg ) ) );

        Future<V> f = cMap.get( arg );
        if ( null == f ) {

            LOG.info( "Start computation for key '{}'", arg );
            Future<V> old = cMap.putIfAbsent( arg, exeSe.submit( () -> compute( arg ) ) );
            if ( null == old ) {
                LOG.info( "Started computation!" );
            } else {
                LOG.info( "Other thread already started this" );
            }
        } else {
            LOG.warn( "Try to start computation for already known key '{}'", arg );
        }

    }

    @Override
    public Future<V> get( K arg ) {

        if ( !cMap.containsKey( arg ) ) {
            throw new IllegalArgumentException( "No value for key[='" + arg + "']!" );
        }
        return cMap.get( arg );
    }

    @Override
    public Future<V> start( K arg ) {
        if ( null == arg ) {
            throw new IllegalArgumentException( "Null is not allowed" );
        }

        LOG.info( "try to get future for key '" + arg + "'" );


        Future<V> f = cMap.get( arg );
        if ( null == f ) {


            f = cMap.computeIfAbsent( arg, key -> exeSe.submit( () -> compute( arg ) ) );

        } else {
            LOG.warn( "Try to start computation for already known key '{}'", arg );
        }

        return f;
    }

    @Override
    public final boolean isMemoized( K key ) {
        return cMap.containsKey( key );
    }

    @Override
    public Future<V> remove( K arg ) {

        if ( !cMap.containsKey( arg ) ) {
            throw new IllegalArgumentException( "No value for key[='" + arg + "']!" );
        }
        return cMap.remove( arg );
    }

    @Override
    public boolean remove( K key, Future<V> val ) {
        Objects.requireNonNull( key, "key must be non null" );
        Objects.requireNonNull( val, "value must be non null" );
        return cMap.remove( key, val );
    }

    @Override
    public Collection<Future<V>> removeAll() {

        Collection<Future<V>> val = cMap.values();
        cMap.clear();
        return val;
    }

}