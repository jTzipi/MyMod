package eu.jpangolin.jtzipi.mymod.io.watcher;


import java.nio.file.Path;
import java.nio.file.Watchable;

/**
 * Listener for File System Path events.
 *
 * @author jTzipi
 */
public interface IFileSystemPathWatchListener {
    /**
     * Path was modified.
     *
     * @param parent parent path
     * @param path   path modified
     * @param cnt    how often
     */
    void onModified( Path parent, Path path, int cnt );

    /**
     * Path was created.
     *
     * @param parent parent dir
     * @param path   path created
     * @param cnt    how often
     */
    void onCreated( Path parent, Path path, int cnt );

    /**
     * Path deleted.
     *
     * @param parent parent dir
     * @param path   path deleted
     * @param cnt    how often
     */
    void onDeleted( Path parent, Path path, int cnt );

    /**
     * Path events overflow.
     * That is some events are lost.
     *
     * @param parent parent dir
     * @param path   path
     * @param cnt    how often
     */
    void onOverflow( Path parent, Path path, int cnt );

    /**
     * Reset of watch key failed.
     *
     * @param path path
     */
    void onResetFailed( final Path path );

    /**
     * There are no more watch keys.
     *
     * @param lastPath last path
     */
    void onPathToWatchEmpty( final Path lastPath );

    /**
     * There was an unknown watchable found.
     *
     * @param wt watchable
     */
    void onUnknownWatchable( Watchable wt );

    /**
     * @param path
     */
    void onFileNotRegistered( final Path path );
}