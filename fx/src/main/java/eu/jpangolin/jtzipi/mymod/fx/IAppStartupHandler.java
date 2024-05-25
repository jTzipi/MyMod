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

package eu.jpangolin.jtzipi.mymod.fx;

/**
 * Handler for startup task.
 * <p>
 * This can be used if you have a <i>long</i> startup task and
 * want to display some splash screen or likewise.
 * <p>
 * Look <a href="https://gist.github.com/jewelsea/2305098#file-taskbasedsplash-java-L27">this</a>.
 *
 *
 * </p>
 *
 * @author Jewelsea
 * @author jTzipi
 */
public interface IAppStartupHandler {

    /**
     * On task running.
     */
    void onRunning();

    /**
     * On task progress.
     */
    void onProgress();

    /**
     * On task completion.
     */
    void onCompletion();

    /**
     * On task
     */
    void onError();


}