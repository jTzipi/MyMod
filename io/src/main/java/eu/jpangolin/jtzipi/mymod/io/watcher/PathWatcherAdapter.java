package eu.jpangolin.jtzipi.mymod.io.watcher;

import java.nio.file.Path;
import java.nio.file.Watchable;

/**
 * Adapter for {@link IFileSystemPathWatchListener}.
 *
 * @author jTzipi
 */
public class PathWatcherAdapter implements IFileSystemPathWatchListener {
    @Override
    public void onModified( Path parent, Path context, int cnt ) {

    }

    @Override
    public void onCreated( Path parent, Path context, int cnt ) {

    }

    @Override
    public void onDeleted( Path parent, Path context, int cnt ) {

    }

    @Override
    public void onOverflow( Path parent, Path context, int cnt ) {

    }


    @Override
    public void onResetFailed( Path path ) {

    }

    @Override
    public void onPathToWatchEmpty( Path lastPath ) {

    }

    @Override
    public void onUnknownWatchable( Watchable wt ) {

    }

    @Override
    public void onFileNotRegistered( Path path ) {

    }


}