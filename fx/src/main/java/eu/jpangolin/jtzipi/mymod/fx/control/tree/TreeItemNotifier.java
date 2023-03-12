package eu.jpangolin.jtzipi.mymod.fx.control.tree;

import eu.jpangolin.jtzipi.mymod.io.watcher.FileSystemWatcher;
import eu.jpangolin.jtzipi.mymod.io.watcher.IFileSystemPathWatchListener;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Watchable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class TreeItemNotifier implements IFileSystemPathWatchListener {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( "TreeItemNotifier" );

    private final ConcurrentMap<Path, AbstractPathNodeTreeItem> nodeCacheMap = new ConcurrentHashMap<>();

    private final FileSystemWatcher fsw;
    private boolean observing;

    private TreeItemNotifier( final FileSystemWatcher fileSystemWatcher ) {

        this.fsw = fileSystemWatcher;

    }

    public static TreeItemNotifier of( FileSystemWatcher fileSystemWatcher ) {
        Objects.requireNonNull( fileSystemWatcher );

        return new TreeItemNotifier( fileSystemWatcher );
    }

    /**
     * Observe a path node tree item.
     *
     * @param treeItem path node tree item
     * @throws NullPointerException if {@code treeItem}
     */
    public void observe( final AbstractPathNodeTreeItem treeItem ) {
        Objects.requireNonNull( treeItem );

        Path path = treeItem.getValue().getValue();
        AbstractPathNodeTreeItem oldItem = nodeCacheMap.putIfAbsent( path, treeItem );

        // we have a new item to observe
        if ( null == oldItem ) {
            fsw.putPath( path, false );
        } else {

            Path oldPath = oldItem.getValue().getValue();
            LOG.warn( "We added '{}' to already observed Tree Item '{}' with path '{}' !?", path, oldItem, oldPath );
        }

    }

    /**
     * Start watching file system changes on registered tree items.
     * <p>
     * Hint: If the {@link FileSystemWatcher} is not started we try to start it.
     * since otherwise we can't receive anything.
     * <br/>
     * We also add the listener here so there may be other events already happen.
     * Especially those events from the file system root. We can't receive prior to create
     * the {@linkplain AbstractPathNodeTreeItem} for those file nodes.
     * </p>
     *
     * @param startWatcherIfIsNotRunning start the {@code FileSystemWatcher} if not start
     */
    public void start( boolean startWatcherIfIsNotRunning ) {
        if ( isObserving() ) {
            LOG.warn( "TIN is observing!" );
            return;
        }
        // if we don't start the system watcher when not running
        // we can' receive events!
        if ( !fsw.isWatching() && !startWatcherIfIsNotRunning ) {

            LOG.warn( "FileSystemWatcher is not watching and you don't allow to start. Can't watch for events!" );
            this.observing = false;
            return;
        }

        // add listener
        fsw.addListener( this );

        // start watcher if not started
        if ( !fsw.isWatching() ) {
            fsw.start();
        }


        this.observing = true;
    }

    public boolean isObserving() {
        return observing;
    }

    @Override
    public void onModified( Path parent, Path path, int cnt ) {

    }

    @Override
    public void onCreated( Path parent, Path path, int cnt ) {

        boolean parentKnown = nodeCacheMap.containsKey( parent );
        LOG.info( "onCreated! with parent='{}' and path='{} {} times", parent, parent, cnt );
        LOG.info( "Parent is observed? {}", parentKnown );
        if ( parentKnown ) {
            AbstractPathNodeTreeItem parentPNT = nodeCacheMap.get( parent );

            // TODO : reload?
            LOG.info( "compute sub nodes" );
            parentPNT.computeSubNodes();

        } else {
            LOG.error( "Parent '{}' path is not known!?", parent );
        }
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
