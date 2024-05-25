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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Describes a native os command which we expect to result instantly.
 *
 * @author jTzipi
 */
public interface IInstantCommand<T> extends ICommand {

    /**
     * Min Timeout for the proc to finish.
     */
    long MIN_TIMEOUT = 1L;
    /**
     * Default timeout  waiting for raw result.
     */
    long DEFAULT_TIMEOUT = 17L;
    /**
     * Default timeout unit.
     */
    TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;

    /**
     * Launch command and wrap the {@linkplain Process} .
     * The command will wait for {@link #DEFAULT_TIMEOUT} {@link #DEFAULT_TIMEOUT_UNIT} to
     * finish.
     *
     * @return Wrapper of launched process
     * @throws IOException I/O
     * @throws InterruptedException if cmd was
     */
    default CommandResult<T> launch() throws IOException, InterruptedException {

        return launch(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT);
    }

    /**
     * Launch the command.
     * The command will wait the time and unit to finish.
     * @param timeout timeout [{@linkplain #MIN_TIMEOUT} .. ]
     * @param timeUnit unit
     * @return the parsed result
     * @throws IOException I/O
     * @throws InterruptedException IE
     */
    CommandResult<T> launch(long timeout, TimeUnit timeUnit) throws IOException, InterruptedException;
}
