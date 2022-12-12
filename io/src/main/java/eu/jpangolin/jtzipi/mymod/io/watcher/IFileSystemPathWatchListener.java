package eu.jpangolin.jtzipi.mymod.io.watcher;


import java.nio.file.Path;
import java.nio.file.Watchable;

/**
 * Listener for File System Path events.
 */
public interface IFileSystemPathWatchListener {

    void onModified( Path parent, Path context, int cnt );

    void onCreated( Path parent, Path context, int cnt );

    void onDeleted( Path parent, Path context, int cnt );

    void onOverflow( Path parent, Path context, int cnt );

    void onResetFailed( final Path context );

    void onPathToWatchEmpty( final Path lastPath );

    void onUnknownWatchable( Watchable wt );
}