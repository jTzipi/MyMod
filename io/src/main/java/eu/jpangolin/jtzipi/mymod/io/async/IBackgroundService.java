package eu.jpangolin.jtzipi.mymod.io.async;

import java.util.concurrent.TimeUnit;

/**
 * Convenient methods to maintain a service.
 * <p>
 *     With <em>service</em> We mean a background task, which is <u>not</u> a demon thread.
 *     <br>
 *     This service should be controlled by a {@link java.util.concurrent.ExecutorService}.
 *     So we can use the {@link java.util.concurrent.Future} functions to control the service.
 *     <br>
 *     This background task should be
 *     <ul>
 *         <li>Startable</li>
 *         <li>Start with delay</li>
 *         <li>Stoppable</li>
 *         <li>Stop with delay</li>
 *         <li>Optional cancelable</li>
 *         <li>Optional resumable</li>
 *         <li>Respond to program exit</li>
 *     </ul>
 * </p>
 */
public interface IBackgroundService {
    /**
     * Minimum delay for a delayed service.
     */
    long MIN_DELAY = 0L;
    /**
     * Maximum Delay for delayed service.
     */
     long MAX_DELAY = TimeUnit.DAYS.toDays(365);
    /**
     * Default TimeUnit.
     */
    TimeUnit DEF_TIME_UNIT = TimeUnit.SECONDS;
    /**
     * Start this service.
     */
    void start() ;

    /**
     * Start this service with delay.
     * @param delay delay [{@link #MIN_DELAY} ..
     * @param timeUnit timeUnit
     */
    void startDelayed(long delay, TimeUnit timeUnit ) ;

    /**
     * Stop this service.
     */
    void stop();

    /**
     * Stop this service in the future.
     * @param time time [0 ..
     * @param timeUnit time Unit
     */
    void stopDelayed( long time, TimeUnit timeUnit) ;
    /**
     * Short description.
     * @return description
     */
    String getDescription();

    /**
     * Return whether this thread is started.
     * @return {@code true} if this service is started and alive
     */
    boolean isRunning();

    /**
     * If this is true we can not start any new task.
     * @return {@code true} if the service is shutdown
     */
    boolean isFinished();

    /**
     * What to do on application exit.
     * This method may be called
     * on application shut down to do some
     * tasks on shut down.
     */
    void onExit();

}
