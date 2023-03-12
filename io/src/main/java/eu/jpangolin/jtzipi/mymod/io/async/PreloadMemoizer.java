package eu.jpangolin.jtzipi.mymod.io.async;

import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * Preloader and Memoizer for a task.
 * This is similar to {@linkplain AbstractPreloadMemo}.
 * The only difference here is that we provide the task to preload via
 * the constructor as a {@linkplain Function}.
 * So this class is no more {@code abstract}.
 *
 * @param <K> key
 * @param <V> value
 */
public class PreloadMemoizer<K, V> implements IPreloadMemoized<K, V> {


    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( "PreloadMemoizer" );
    private final Function<K, V> cf;
    // ExecutorService
    private final ExecutorService exeSe;
    // Cache
    private final ConcurrentMap<K, Future<V>> cMap = new ConcurrentHashMap<>();

    PreloadMemoizer( Function<K, V> calcu, ExecutorService executorService ) {

        this.cf = calcu;
        this.exeSe = executorService;
    }

    @Override
    public void put( K key ) {

    }

    @Override
    public Future<V> get( K arg ) {

        return null;
    }

    @Override
    public Future<V> start( K arg ) {

        if ( null == arg ) {
            throw new IllegalArgumentException( "Null is not allowed" );
        }

        LOG.info( "try to get future for key '" + arg + "'" );


        Future<V> f = cMap.get( arg );
        if ( null == f ) {


            f = cMap.computeIfAbsent( arg, key -> exeSe.submit( () -> cf.apply( arg ) ) );

        } else {
            LOG.warn( "Try to start computation for already known key '{}'", arg );
        }

        return f;

    }

    @Override
    public boolean isMemoized( K key ) {

        return false;
    }

    @Override
    public Future<V> remove( K key ) {

        return null;
    }

    @Override
    public boolean remove( K key, Future<V> future ) {
        Objects.requireNonNull( key, "key must be non null" );
        Objects.requireNonNull( future, "value must be non null" );
        return cMap.remove( key, future );
    }

    @Override
    public Collection<Future<V>> removeAll() {

        return null;
    }
}