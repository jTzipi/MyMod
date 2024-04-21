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

package eu.jpangolin.jtzipi.mymod.io.cmd;

/**
 * This is the 'result' of a native system command which may hold an instance of the command.
 * @param object Result object should be a record
 * @param result raw result
 * @param proc proc
 * @param t error (may be null)
 * @param <R>
 *
 * @author jTzipi
 */
public record CommandResult<R>( R object, String result, Process proc, Throwable t) implements ICommandResult, Comparable<CommandResult<R>> {


    @Override
    public Process getProcess() {
        return proc;
    }

    @Override
    public Throwable getError() {
        return t;
    }

    @Override
    public String getRawResult() {
        return result;
    }

    @Override
    public int compareTo(CommandResult<R> o) {
        return 0;
    }
}
