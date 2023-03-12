package eu.jpangolin.jtzipi.mymod.fx.service;


import eu.jpangolin.jtzipi.mymod.io.ICloseOnExit;

import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.ReadOnlySetWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Windows Root Path Watcher.
 * <p>
 * On Windows OS you have the problem that you have no root node for the disk, usb , net drives
 * and so on. Therefore if you want to listen for changes we need a kind of task to check
 * the status of those drives.
 * <br/>
 * This is one idea. We use a scheduled executor service to poll the status.
 * To inform observers we use the {@link ObservableSet} and {@link ReadOnlySetWrapper} .
 * <br/>
 * Since there is only one FileSystem to watch we use an Enum class.
 * </p>
 *
 * @author jTzipi
 */
public enum WinRootPathWatcher implements ICloseOnExit {

    /**
     * Single only instance.
     */
    SINGLETON;
    public static final long MIN_DELAY = 0L;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( WinRootPathWatcher.class );
    //
    private static final ScheduledExecutorService SES = Executors.newSingleThreadScheduledExecutor();
    private static final ObservableSet<Path> ROOT_PATH_SET = FXCollections.observableSet();
    private static final ReadOnlySetWrapper<Path> ROOT_PATH_SW = new ReadOnlySetWrapper<>( ROOT_PATH_SET );
    private static final FileSystem FS = FileSystems.getDefault();
    private static final Runnable SCAN = () -> {

        Set<Path> rootS = new HashSet<>();
        for ( Path root : FS.getRootDirectories() ) {
// TODO : ED T?
            if ( ROOT_PATH_SET.add( root ) ) {
                LOG.info( "Root path '{}' added", root );
            }

            rootS.add( root );
        }

        // remove path which may be removed
        // and retain the other
        if ( ROOT_PATH_SET.retainAll( rootS ) ) {
            LOG.info( "Some Root Path are removed!" );
        }
        ;
        // ROOT_PATH_SET.removeIf( path -> !rootS.contains( path ) );
    };


    private boolean watching = false;

    WinRootPathWatcher() {


    }


    /**
     * Property to listener for.
     *
     * @return set property
     */
    public ReadOnlySetProperty<Path> getRootPathSet() {
        return ROOT_PATH_SW.getReadOnlyProperty();
    }

    /**
     * Start the watch task.
     *
     * @param delay initial delay [0 .. ]
     * @param rate  rate [1 .. ]
     * @param tiun  time unit
     * @throws NullPointerException     if {@code tiun} is null
     * @throws IllegalArgumentException if {@code rate} &le; 0
     * @throws IllegalStateException    if scheduled executor shutdown
     */
    public void start( long delay, long rate, TimeUnit tiun ) {
        Objects.requireNonNull( tiun );

        //;
        if ( SES.isShutdown() ) {
            throw new IllegalStateException( "Scheduled E. shutdown" );
        }

        if ( isWatching() ) {

            LOG.warn( "Watcher is watching!" );
            return;
        }

        delay = Math.max( delay, MIN_DELAY );
        LOG.info( "Start watching ...\nDelay = {}\nPeriod = {}\nUnit = {}", delay, rate, tiun );
        SES.scheduleAtFixedRate( SCAN, delay, rate, tiun );
        watching = true;
    }

    /**
     * Stop watching.
     * We also shut down the scheduled executor.
     */
    public void stop() {
        if ( SES.isShutdown() ) {
            LOG.warn( "SES already shutdown" );
            return;
        }
        LOG.info( "Shutting down..." );
        SES.shutdown();
        watching = false;
        LOG.info( "SES is shutdown!" );
    }

    /**
     * Is this watch service watching.
     *
     * @return {@code true} if watching
     */
    public boolean isWatching() {
        return watching;
    }

    @Override
    public void onExit() {

        if ( isWatching() ) {
            stop();
        }
    }
}
