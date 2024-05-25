/*
 * Copyright (c) 2022-2024. Tim Langhammer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractScheduledBackgroundService extends AbstractBackgroundService {


    protected long delay;       // initial delay
    protected long rate;        // time between call
    protected TimeUnit tiun;    // time unit
    protected AbstractScheduledBackgroundService(String descStr, long delay, long period, TimeUnit timeUnit) {
        super(descStr);
        this.delay = delay;
        this.rate = period;
        this.tiun = timeUnit;
    }

    protected abstract ScheduledExecutorService getService();

    /**
     * Start a command with a scheduled executor , delay and a fixed rate.
     * @param cmd runnable
     * @throws NullPointerException if {@code cmd} is null
     */
    protected void startScheduledServiceFixed(Runnable cmd) {
        Objects.requireNonNull(cmd);
        getService().scheduleAtFixedRate(cmd, delay, rate, tiun);
    }

    protected void startScheduledServiceDelayed(Runnable cmd) {
        getService().scheduleWithFixedDelay(cmd, delay, rate, tiun);
    }
    @Override
    public boolean isRunning() {
        return iStarted() && !getService().isShutdown();
    }

    @Override
    public boolean iStopped() {
        return getService().isShutdown();
    }

    @Override
    protected void stopService()  {

        getService().shutdown();

    }
}
