/*
 *    Copyright (c) 2022 Tim Langhammer
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

package eu.jpangolin.jtzipi.mymod.io.watcher;

import eu.jpangolin.jtzipi.mymod.io.ICloseOnExit;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * File System Watcher.
 *
 * @author jTzipi
 */
public final class FileSystemWatcher implements ICloseOnExit {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( "FSW" );
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
    // -- Attribute
    private final WatchService ws;
    private final Set<Path> rootSet = new HashSet<>();
    private final Set<Path> unregPathSet = new HashSet<>();
    private final Map<WatchKey, Path> keyMap = new HashMap<>();
    private final Set<IFileSystemPathWatchListener> listenerList = new HashSet<>();
    private final boolean trace;
    private Future<?> task;

    FileSystemWatcher( final WatchService ws, final Set<Path> rootSet, final boolean trace ) {

        this.ws = ws;
        this.rootSet.addAll( rootSet );
        this.trace = trace;
    }

    /**
     * Create new instance for one or more root folder.
     *
     * @param trace trace changed watch key
     * @param roots one or more root paths [1 .. ]
     * @return FileSystem Watcher
     * @throws IOException          fail to create watch service
     * @throws NullPointerException if {@code roots}
     */
    public static FileSystemWatcher of( final boolean trace, final Path... roots ) throws IOException {

        Objects.requireNonNull( roots, "Path to watch is null" );

        WatchService watchService = FileSystems.getDefault().newWatchService();

        FileSystemWatcher fsw = new FileSystemWatcher( watchService, Set.of( roots ), trace );
        fsw.init();
        return fsw;
    }

    /**
     * Cast to the only valid type.
     *
     * @param wk  key
     * @param <T> type
     * @return watch event of type
     * @see StandardWatchEventKinds
     */
    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast( WatchEvent<?> wk ) {

        return ( WatchEvent<T> ) wk;
    }

    @Override
    public void onExit() throws IOException {

        stop();
    }

    private void init() {

        for ( final Path root : rootSet ) {

            register( root );
        }


    }

    /**
     * Add a file system path watch listener.
     *
     * @param listener listener
     * @throws NullPointerException if {@code listener} is null
     */
    public void addListener( IFileSystemPathWatchListener listener ) {

        Objects.requireNonNull( listener );

        listenerList.add( listener );
    }

    /**
     * Remove a file system path watch listener.
     *
     * @param listener listener
     * @return {@code true} listener was removed
     * @throws NullPointerException if {@code listener} is null
     */
    public boolean removeListener( IFileSystemPathWatchListener listener ) {

        Objects.requireNonNull( listener );
        if ( !listenerList.contains( listener ) ) {
            LOG.warn( "Try to remove not contained listener" );
        }
        return listenerList.remove( listener );
    }

    /**
     * Start watch service.
     * <p></p>
     */
    public void start() {
        if ( isWatching() ) {
            LOG.warn( "This watcher " );

        }
        LOG.info( "Start Watch Service" );
        task = EXEC.submit( this::watch );

    }

    public boolean restart( boolean cancelIfStart ) {
        if ( isWatching() ) {

            LOG.warn( "Watcher already watching" );
            if ( cancelIfStart ) {
                boolean canceled = task.cancel( true );
                LOG.info( "Task Canceled ? {}", canceled );
                if ( !canceled ) {
                    return false;
                }
            } else {
                return false;
            }
        }

        start();
        return true;
    }

    /**
     * Stop watch service.
     *
     * @throws IOException IO Error
     */
    public void stop() throws IOException {

        if ( !isWatching() ) {
            LOG.warn( "This watcher was not watching" );
            return;
        }

        // TODO: shutdown
        // we don't need result so we can
        EXEC.shutdownNow();
        // close watch service
        ws.close();
    }

    /**
     * Return whether this watcher is watching.
     *
     * @return {@code true} when watching
     */
    public boolean isWatching() {
        return null != task && !task.isCancelled();
    }

    private void registerRecursive( final Path rootPath ) {
        assert null != rootPath : "path is null";

        try {
            Files.walkFileTree( rootPath, new SimpleFileVisitor<>() {


                @Override
                public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs ) throws IOException {

                    register( dir );

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed( Path file, IOException exc ) throws IOException {

                    LOG.warn( "Failed to register watcher for dir '{}'.\nReason:'{}'", file, exc );
                    unregPathSet.add( file );
                    firePathNotRegistered( file );
                    return FileVisitResult.CONTINUE;
                }
            } );
        } catch ( IOException ioE ) {

            LOG.warn( "Error walking the file tree!", ioE );
        }

    }

    public void putPath( final Path path, boolean recursive ) {
        Objects.requireNonNull( path );


        if ( recursive ) {
            registerRecursive( path );
        } else {
            register( path );
        }

    }

    /**
     * If this path is not registered return true.
     *
     * @param path path
     * @return {@code true} if {@code path} is not registered
     * @throws NullPointerException if {@code path} is null
     */
    public boolean isPathNotRegistered( final Path path ) {
        Objects.requireNonNull( path );
        return unregPathSet.contains( path );
    }

    private void register( final Path path ) {

        try {
            WatchKey key = path.register( ws, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY );

            //
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
        } catch ( final IOException ioE ) {

            unregPathSet.add( path );
            firePathNotRegistered( path );
            LOG.warn( "Failed to register path '{}' to watch", path );
        }


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
            } catch ( ClosedWatchServiceException cwsE ) {

                LOG.error( "Watch Service closed!", cwsE );
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

//                if ( SystemWatchEvent.EVENT_CREATE == swe && Files.isDirectory( changedPath ) && recursive ) {
//
//                    try {
//                        // if we receive a created dir event, and we
//                        // want to listen for all directories recursive
//                        // we need to register them
//                        registerRecursive( changedPath );
//                    } catch ( IOException ioE ) {
//
//                        LOG.warn( "Failed to register sub folders of '{}'!", changedPath );
//                    }
//
//                }

                final boolean reset = watchKey.reset();
                LOG.debug( "Watch key reset ? {}", reset );
                // Failed to reset
                // we remove the watch key
                // if no more keys we stop watching
                //

                if ( !reset ) {

                    fireResetFailed( dir );

                    keyMap.remove( watchKey );
                    if ( keyMap.isEmpty() ) {
// IMPORTANT: this case should not occur
                        // on a default file system since the root node should never be removed
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
                case EVENT_CREATE -> watchListener.onCreated( parent, path, cnt );
                case EVENT_DELETE -> watchListener.onDeleted( parent, path, cnt );
                case EVENT_MODIFY -> watchListener.onModified( parent, path, cnt );
                case EVENT_OVERFLOW -> watchListener.onOverflow( parent, path, cnt );
                default -> LOG.warn( "System Event unknown '{}'", event );
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


    private void firePathNotRegistered( Path path ) {
        for ( IFileSystemPathWatchListener listener : listenerList ) {
            listener.onFileNotRegistered( path );
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