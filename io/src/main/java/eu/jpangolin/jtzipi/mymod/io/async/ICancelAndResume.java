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
 * IF to cancel and resume a running task.
 */
public interface ICancelAndResume {

    /**
     * Try to cancel this task/service.
     */
    void cancel();

    /**
     * Try to cancel this task/
     * @param time time
     * @param timeUnit time unit
     */

    void cancelDelayed(long time, TimeUnit timeUnit) throws InterruptedException;


    void resume();


    void resumeDelayed(long time, TimeUnit timeUnit) throws InterruptedException;
}
