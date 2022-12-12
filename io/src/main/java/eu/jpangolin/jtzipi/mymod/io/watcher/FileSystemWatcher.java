package eu.jpangolin.jtzipi.mymod.io.watcher;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * File System Watcher.
 *
 * @author jTzipi
 */
public class FileSystemWatcher {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( "FSW" );
    private final WatchService ws;
    private final Map<WatchKey, Path> keyMap = new HashMap<>();
    private final boolean recursive;
    private final Set<IFileSystemPathWatchListener> listenerList = new HashSet<>();
    private boolean trace = true;

    FileSystemWatcher( WatchService ws, boolean recursive ) {

        this.ws = ws;
        this.recursive = recursive;
    }

    /**
     * @param path      path to watch (should be dir)
     * @param recursive recursive register sub dir
     * @return FileSystem Watcher
     * @throws IOException          io
     * @throws NullPointerException if {@code path}
     */
    public static FileSystemWatcher of( final Path path, boolean recursive ) throws IOException {

        Objects.requireNonNull( path, "Path to watch is null" );
        if ( !Files.isReadable( path ) ) {
            throw new IOException( "Path[='path'] is not readable" );
        }
        if ( Files.isDirectory( path, LinkOption.NOFOLLOW_LINKS ) ) {
            throw new IOException( "Path[='path'] to watch is not a dir" );
        }
        WatchService watchService = FileSystems.getDefault().newWatchService();

        FileSystemWatcher fsw = new FileSystemWatcher( watchService, recursive );

        if ( recursive ) {
            fsw.registerRecursive( path );
        } else {
            fsw.register( path );
        }

        return fsw;
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast( WatchEvent<?> wk ) {

        return ( WatchEvent<T> ) wk;
    }

    public void addListener( IFileSystemPathWatchListener listener ) {

        Objects.requireNonNull( listener );

        listenerList.add( listener );
    }

    public boolean removeListener( IFileSystemPathWatchListener listener ) {

        Objects.requireNonNull( listener );
        if ( !listenerList.contains( listener ) ) {
            LOG.warn( "Try to remove not contained listener" );
        }
        return listenerList.remove( listener );
    }

    public void start() {

        watch();
    }

    public void stop() throws IOException {

        ws.close();
    }

    private void registerRecursive( final Path rootPath ) throws IOException {


        Files.walkFileTree( rootPath, new SimpleFileVisitor<>() {


            @Override
            public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs ) throws IOException {

                register( dir );

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed( Path file, IOException exc ) throws IOException {

                LOG.warn( "Failed to register watcher for dir '{}'. Reason:{}", file, exc );
                return FileVisitResult.CONTINUE;
            }
        } );

    }

    private void register( final Path path ) throws IOException {

        WatchKey key = path.register( ws, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY );

        if ( trace ) {
            Path prevPath = keyMap.get( key );
            if ( null == prevPath ) {
                LOG.info( "Register path '{}' to watch", path );
            } else {

                if ( !prevPath.equals( path ) ) {
                    LOG.info( "Update '{}' to '{}'", prevPath, path );
                }
            }
        }
        keyMap.put( key, path );
    }

    private void watch() {

        for ( ; ; ) {
            WatchKey watchKey;
            try {
                // blocked waiting for event
                watchKey = ws.take();
            } catch ( InterruptedException ie ) {
                LOG.warn( "Was interrupted close this watcher" );
                Thread.currentThread().interrupt();
                return;
            }

            Path dir = keyMap.get( watchKey );
            if ( null == dir ) {
                LOG.error( "Received change event from not registered path. Watch Key='{}'", watchKey.watchable() );
                fireUnknownWatchable( watchKey.watchable() );
                continue;
            }


            for ( WatchEvent<?> wevt : watchKey.pollEvents() ) {
                WatchEvent.Kind<?> wk = wevt.kind();


                WatchEvent<Path> pwe = cast( wevt );
                Path changedPath = dir.resolve( pwe.context() );
                SystemWatchEvent swe = SystemWatchEvent.of( wk.name() );

                // send event
                fireStandardEvent( swe, dir, changedPath, pwe.count() );

                if ( SystemWatchEvent.EVENT_CREATE == swe && Files.isDirectory( changedPath ) && recursive ) {

                    try {
                        // if we receive a created dir event, and we
                        // want to listen for all directories recursive
                        // we need to register them
                        registerRecursive( changedPath );
                    } catch ( IOException ioE ) {

                        LOG.warn( "Failed to register sub folders of '{}'!", changedPath );
                    }

                }

                // Failed to reset
                if ( !watchKey.reset() ) {

                    fireResetFailed( dir );

                    keyMap.remove( watchKey );
                    if ( keyMap.isEmpty() ) {

                        fireNoMoreKeys( dir );
                        LOG.warn( "No more folder to watch! Stopping..." );
                        return;
                    }
                }
            }
        }

    }

    private void fireStandardEvent( SystemWatchEvent event, Path parent, Path path, int cnt ) {

        for ( IFileSystemPathWatchListener watchListener : listenerList ) {
            switch ( event ) {

                case EVENT_CREATE:
                    watchListener.onCreated( parent, path, cnt );
                    break;
                case EVENT_DELETE:
                    watchListener.onDeleted( parent, path, cnt );
                    break;
                case EVENT_MODIFY:
                    watchListener.onModified( parent, path, cnt );
                    break;
                case EVENT_OVERFLOW:
                    watchListener.onOverflow( parent, path, cnt );
                    break;
                default:
                    LOG.warn( "System Event unknown '{}'", event );
            }

        }

    }

    private void fireResetFailed( Path dir ) {

        for ( IFileSystemPathWatchListener watchListener : listenerList ) {

            watchListener.onResetFailed( dir );
        }
    }

    private void fireNoMoreKeys( Path lastDir ) {

        for ( IFileSystemPathWatchListener watchListener : listenerList ) {

            watchListener.onPathToWatchEmpty( lastDir );
        }
    }

    private void fireUnknownWatchable( Watchable watchable ) {

        for ( IFileSystemPathWatchListener listener : listenerList ) {
            listener.onUnknownWatchable( watchable );
        }
    }

    /**
     * System Events like those of {@link StandardWatchEventKinds}.
     */
    public enum SystemWatchEvent {

        /**
         * Path created.
         * Same as {@link StandardWatchEventKinds#ENTRY_CREATE}.
         */
        EVENT_CREATE( ENTRY_CREATE.name() ),

        /**
         *
         */
        EVENT_MODIFY( ENTRY_MODIFY.name() ),
        /**
         *
         */
        EVENT_DELETE( ENTRY_DELETE.name() ),
        /**
         *
         */
        EVENT_OVERFLOW( OVERFLOW.name() ),
        /**
         *
         */
        EVENT_RESET_FAILED( "KEY_RESET_FAILED" ),
        /**
         *
         */
        EVENT_PATH_EMPTY( "PATH_MAP_EMPTY" ),
        /**
         *
         */
        UNKNOWN( "?" );


        private final String eventStr;

        SystemWatchEvent( final String eventNameStr ) {

            this.eventStr = eventNameStr;
        }

        public static SystemWatchEvent of( String nameStr ) {

            return Stream.of( values() )
                    .filter( swe -> swe.getEventName().equalsIgnoreCase( nameStr ) )
                    .findFirst()
                    .orElse( UNKNOWN );


        }

        public String getEventName() {

            return eventStr;
        }

    }
}