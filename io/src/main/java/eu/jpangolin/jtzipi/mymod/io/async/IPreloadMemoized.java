package eu.jpangolin.jtzipi.mymod.io.async;

import java.util.Collection;
import java.util.concurrent.Future;

/**
 * This is a combination of a preloader and memoizer.
 * <p>
 * We use the 'memoizer' cache like
 * <a href="https://jcip.net/listings/Memoizer.java" alt="JCIP Memoizer Code">this</a>
 * and preloader code like
 * <a href="https://jcip.net/listings/Preloader.java" alt="JCIP Preloader Code">this</a>.
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
 * </p>
 *
 * @param <K> key
 * @param <V> value
 */
public interface IPreloadMemoized<K, V> {

    /**
     * Start async computation and cache the future.
     *
     * @param key argument
     * @throws IllegalArgumentException if {@code key} is null
     */
    void put( K key );


    /**
     * Return the future of an async comp.
     *
     * @param arg arg
     * @return future if previously stored
     * @throws IllegalArgumentException if this preloader did not know {@code arg}
     */
    Future<V> get( K arg );

    /**
     * Start async computation and cache the future.
     *
     * @param arg argument
     * @throws IllegalArgumentException if {@code arg} is null
     */
    Future<V> start( K arg );

    /**
     * Return whether the key is contained.
     *
     * @param key key
     * @return {@code true} if {@code key} is cached
     */
    boolean isMemoized( K key );

    /**
     * Remove an argument.
     *
     * @param key argument
     * @return future wrapper for {@code key} if contained
     * @throws IllegalArgumentException if {@code key} is not contained
     */
    Future<V> remove( final K key );

    /**
     * Remove and cache item via key <u>and</u> value.
     *
     * @param key    key
     * @param future value
     * @return {@code true} if item was removed
     * @see {@link java.util.concurrent.ConcurrentMap#remove(Object, Object)}
     */
    boolean remove( final K key, Future<V> future );

    /**
     * Remove all values from cache.
     *
     * @return values from cache
     */
    Collection<Future<V>> removeAll();
}