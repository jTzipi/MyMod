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

import eu.jpangolin.jtzipi.mymod.utils.Range;
import eu.jpangolin.jtzipi.mymod.utils.TimePerUnit;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for Fx Services.
 *
 * Here we do the base logic to ensure start and stop
 * of a service. Handle abnormal start and stop situations
 * and provide some ways to do things before and after the task.
 *
 * @author jTzipi
 */
public abstract class AbstractBackgroundService implements IBackgroundService {

    protected final String desc;    // service name
    protected boolean started;      // service started

    protected static org.slf4j.Logger LOG = LoggerFactory.getLogger("AbstractFxSer");

    /**
     * Abstract Fx Service.
     * @param descStr desc
     */
    protected AbstractBackgroundService(String descStr) {
        this.desc = descStr;
    }

    /**
     * Each implementing class need to override the start behavior.
     * @throws IOException I
     */
    protected abstract void startService() throws IOException;

    /**
     * Each implementing class need to implement the stop behavior.
     * @throws IOException I/O Error
     */
    protected abstract void stopService() throws IOException;

    /**
     * Set a new logger.
     * @param logger logger
     */
    public static void setLogger( org.slf4j.Logger logger ) {
        AbstractBackgroundService.LOG = Objects.requireNonNull(logger);
    }

    @Override
    public boolean iStarted() {
        return started;
    }

    @Override
    public void start()  {
        //
        // -- Pre Start Setup --
        // 1) check not running
        // 2) check shutdown
        // 3.1) preStart (optional)
        // 3.2) start

        // 1.
        if(isRunning()) {
            LOG.warn("Try to start already started service '{}'", getDescription());
            return;
        }
        // 2.
        if(iStopped()){
            LOG.error("Service '{}' is terminated!", getDescription());
            return;
        }

        try {
            // 3.1
            preStart();
            // 3.2
            startService();
            started = true;
            LOG.info("~ {} started!", getDescription());

        } catch (IOException ioE) {

            onStartFailed(ioE);
        }
    }

    @Override
    public void stop()   {

        //
        // - pre stop
        // 1) check task started
        // 2) check task is not shutdown
        // 3) stop service
        // 3.1) do something after

        // 1.
        if(!isRunning()) {
            LOG.warn("Try to stop service '{}' which is not started", getDescription());
            return;
        }

        // 2.
        if(iStopped()) {
            LOG.warn("Try to stop service '{}' which is terminated", getDescription());
            return;
        }

         try {
            // 3.
             stopService();
             started = false;
            LOG.info("~ {} stopped",getDescription());
            // 3.2
            postShutdown();
        } catch (IOException ioE) {

            onStopFailed(ioE);
        }
    }

    

    /**
     * Optional things to do after shutdown.
     */
    protected void postShutdown() {}

    /**
     * Optional things to do before start.
     */
    protected void preStart() {}


    protected void onStartFailed(Throwable t) {
        LOG.warn("Failed to start service '{}'", getDescription(), t.getCause());
    }


    protected void onStopFailed(Throwable t) {
        LOG.warn("Failed to stop service '{}'", getDescription(), t.getCause());
    }





    @Override
    public String getDescription() {
        return desc;
    }

    @Override
    public void onExit() {

        stop();
    }
}
