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

package eu.jpangolin.jtzipi.mymod.io.cmd;

/**
 * Wrapper for the result of an OS native command.
 *
 */
public interface ICommandResult {

    /**
     * Default Error .
     */
    String RAW_RESULT_ERROR = "<?ERROR>";

    /**
     * Pro of command.
     * @return proc
     */
    Process getProcess();

    /**
     * Optional Error.
     * @return error if something went wrong
     */
    Throwable getError();


    /**
     * Check whether we have an error.
     * @return
     */
    default boolean isError() {
        return null != getError();
    }
    /**
     * Answer of the command.
     * This is only useful for instant commands
     * @return raw answer
     */
    String getRawResult();

}
