package eu.jpangolin.jtzipi.mymod.fx.service;


import eu.jpangolin.jtzipi.mymod.io.async.AbstractBackgroundService;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.ReadOnlySetWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashSet;
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
public class WinRootPathWatcher extends AbstractBackgroundService {

    public static final long MIN_RATE = 1L;

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

    // -- property of scheduled task --
    private final long delay;       // initial delay
    private final long rate;        // time between call
    private final TimeUnit tiun;    // time unit
    private boolean started = false;

    WinRootPathWatcher(long delay, long rate, TimeUnit timeUnit) {
        super("Windows Root Path Watcher");
        this.delay = delay;
        this.rate = rate;
        this.tiun = timeUnit;
    }

    /**
     * Start the watch task.
     *
     * @param delay initial delay [0 .. ]
     * @param rate  rate [1 .. ]
     * @param tiun  time unit
     */
    public static WinRootPathWatcher create( long delay, long rate, TimeUnit tiun ) {


        delay = Math.max( delay, MIN_DELAY );
        rate = Math.max(MIN_RATE, rate);
        if( null == tiun) {
            tiun = DEF_TIME_UNIT;
        }

        LOG.info( "Start watching ...\nDelay = {}\nPeriod = {}\nUnit = {}", delay, rate, tiun );

        return new WinRootPathWatcher(delay, rate, tiun);
    }


    /**
     * Property to listener for.
     *
     * @return set property
     */
    public ReadOnlySetProperty<Path> getRootPathSet() {
        return ROOT_PATH_SW.getReadOnlyProperty();
    }

    @Override
    public boolean isFinished() {
        return SES.isShutdown();
    }

    @Override
    public boolean isRunning() {
        return started && !SES.isShutdown();
    }

    @Override
    protected void startService()  {

        SES.scheduleAtFixedRate( SCAN, delay, rate, tiun );
        started = true;
    }

    @Override
    protected void stopService()  {

        SES.shutdown();
        started = false;
    }
}
