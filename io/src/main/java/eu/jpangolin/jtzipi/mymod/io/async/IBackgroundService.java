/*
 *    Copyright (c) 2022-2023 Tim Langhammer
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
    void startDelayed(long delay, TimeUnit timeUnit );

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
     * Return whether this thread is started and the task maintained by this
     * service is active.
     * @return {@code true} if this service is started and alive
     */
    boolean isRunning();

    /**
     * Indicate that this service is started.
     * The opposite is {@link #iStopped()}.
     * @return {@code true} if this service is started
     */
    boolean iStarted();
    /**
     * If this is true we can not start any new task.
     * @return {@code true} if the service is shutdown
     */
    boolean iStopped();

    /**
     * What to do on application exit.
     * This method may be called
     * on application shut down to do some
     * tasks on shut down.
     */
    void onExit();

}
