package eu.jpangolin.jtzipi.mymod.fx.control;

import eu.jpangolin.jtzipi.mymod.io.watcher.IFileSystemPathWatchListener;

import java.nio.file.Path;
import java.nio.file.Watchable;

public final class DirectoryViewNotifier implements IFileSystemPathWatchListener {
    @Override
    public void onModified( Path parent, Path path, int cnt ) {

    }

    @Override
    public void onCreated( Path parent, Path path, int cnt ) {

    }

    @Override
    public void onDeleted( Path parent, Path path, int cnt ) {

    }

    @Override
    public void onOverflow( Path parent, Path path, int cnt ) {

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
