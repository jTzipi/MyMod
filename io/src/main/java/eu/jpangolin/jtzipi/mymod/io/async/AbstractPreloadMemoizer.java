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
import java.util.concurrent.*;

/**
 * This is a combination of a preloader and memoizer.
 * <p>we use the memoizer cache like <a href="https://jcip.net/listings/Memoizer.java">this</a>
 * and preloader code like <a href="https://jcip.net/listings/Preloader.java">this</a>.
 * Both from Brian Goetz and Tim Peierls.
 * We tweak this to cache the future of a computation not the result.
 * <br/>
 * <br/>
 * <u>Attention</u>
 * On the caller site you have to be careful with the resulting future.
 * e.G. if you try to load some ImageData async and this task is interrupted you have to restore the
 * thread state.
 * <pre>
 *     {@code
 *         ImagePreloader<Path, List<ImageData>> pre = ...
 *         Future<List<ImageData>> future = pre.start( Paths.get( ... ));
 *         // later
 *         //
 *
 *         try {
 *             List<ImageData> imageData = future.get();
 *             for (ImageData data : imageData)
 *                 renderImage(data);
 *         } catch (InterruptedException e) {
 *             // Re-assert the thread's interrupted status
 *             Thread.currentThread().interrupt();
 *             // We don't need the result, so cancel the task too
 *             future.cancel(true);
 *         } catch (ExecutionException e) {
 *             throw launderThrowable(e.getCause());
 *         }}
 * </pre>
 * The {@code launderThrowable} method is part of the {@code utils} package.
 *
 * </p>
 * <p>
 * We use an {@link ExecutorService} to run the
 * </p>
 *
 * @param <K> key
 * @param <V> value
 * @author jTzipi
 */
public abstract class AbstractPreloadMemoizer<K, V> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( AbstractPreloadMemoizer.class );


    private static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;
    private static final long DEFAULT_TIMEOUT = 170L;

    // Timeout
    private TimeUnit timeoutUn = DEFAULT_TIMEOUT_UNIT;
    // how long
    private long timeout = DEFAULT_TIMEOUT;
    // ExecutorService
    private final ExecutorService exeSe;
    // Cache
    private final ConcurrentMap<K, Future<V>> cMap = new ConcurrentHashMap<>();

    /**
     * Constructor.
     * @param executorService exec service
     */
    protected AbstractPreloadMemoizer( ExecutorService executorService ) {

        this.exeSe = null == executorService ? Executors.newCachedThreadPool() : executorService;
    }

    /**
     * Default constructor.
     */
    public AbstractPreloadMemoizer() {
        this(Executors.newCachedThreadPool());
    }

    /**
     * Set timeout unit.
     * @param timeoutUn timeout unit
     */
    public void setTimeoutUnit( final TimeUnit timeoutUn ) {

        this.timeoutUn = null == timeoutUn ? DEFAULT_TIMEOUT_UNIT : timeoutUn;
    }

    /**
     * Set timeout value.
     * if {@code timeout} is lower than 0 we use 0.
     * @param timeout timeout
     */
    public void setTimeout( long timeout ) {

        this.timeout = Math.max( timeout, 0L );
    }

    /**
     * This is your code to implement.
     *
     * @param arg argument
     * @return value
     */
     protected abstract V compute( final K arg );

    /**
     * Start async computation and cache the future.
     *
     * @param arg argument
     *
     * @throws IllegalArgumentException if {@code arg} is null
     */
    public void start( final K arg ) {

        if ( null == arg ) {
            throw new IllegalArgumentException( "Null is not allowed" );
        }

        LOG.info( "try to get future for key '" + arg + "'" );

        // THIS IS THE APPROACH IF WE WANT TO RETURN THE FUTURE
        // cMap.computeIfAbsent( arg, key -> exeSe.submit( () -> compute( arg ) ) );

        Future<V> f = cMap.get( arg );
        if( null == f) {

            LOG.info( "Start computation for key '{}'", arg );
            cMap.putIfAbsent( arg, exeSe.submit( () -> compute( arg ) ) );
        } else {
            LOG.warn( "Try to start computation for already known key '{}'", arg );
        }

    }

    /**
     * Try to finish all running tasks and shutdown.
     *
     * @return {@code true} if executor shut down
     * @throws InterruptedException if await
     */
    public boolean stop() throws InterruptedException {

        exeSe.shutdown();
        return exeSe.awaitTermination( timeout, timeoutUn );
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

    /**
     * Return whether the key is contained.
     * @param key key
     * @return {@code true} if {@code key} is cached
     */
    public final boolean isMemoized( K key ) {
        return cMap.containsKey( key );
    }
    /**
     * Return the future of an async comp.
     *
     * @param arg arg
     * @return future if previously stored
     * @throws IllegalArgumentException if this preloader did not know {@code arg}
     */
    public Future<V> get( K arg ) {

        if ( !cMap.containsKey( arg ) ) {
            throw new IllegalArgumentException( "No value for key[='" + arg + "']!" );
        }
        return cMap.get( arg );
    }

    /**
     * Remove an argument.
     *
     * @param arg argument
     * @return future wrapper for {@code arg} if contained
     * @throws IllegalArgumentException if {@code arg} is not contained
     */
    public Future<V> remove( K arg ) {

        if ( !cMap.containsKey( arg ) ) {
            throw new IllegalArgumentException( "No value for key[='" + arg + "']!" );
        }
        return cMap.remove( arg );
    }

    /**
     * Remove all values from cache.
     *
     * @return values from cache
     */
    public Collection<Future<V>> removeAll() {

        Collection<Future<V>> val = cMap.values();
        cMap.clear();
        return val;
    }
}