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


import java.util.ArrayList;
import java.util.List;

/**
 * Abstraction of an OS native command.
 * <p>
 *     <u>Command structure</u>
 *     <br/>
 *     <ul>
 *         <li>command name ({@link #getName()})</li>
 *
 *         <li>command arguments ({@link #getArgs()}) and options</li>
 *     </ul>
 *
 *     like {@literal $ which -a pip pipx python}
 *     where `{@code which}` is the command name.
 *     `{@code -a}` is the option.
 *     `{@code pip pipx pyhton}` are the arguments.
 * </p>
 * @author jTzipi
 */
public interface ICommand {

    /**
     * Return name of command.
     * @return command name
     */
    String getName();

    /**
     * Return argument list.
     * <p>
     *     This contains both the options (prefixed with {@literal --}, and
     *     the arguments of the command).
     * </p>
     * @return option/argument list
     */
    List<String> getArgs();



    /**
     * Return ProcessBuilder for the args set.
     * Here we only create the builder, and didn't configure .
     * @return ProcessBuilder
     * @throws NullPointerException if {@link #getName()} is null
     */
    default ProcessBuilder getProcessBuilder() {

        List<String> cmdList = new ArrayList<>();
        cmdList.add(getName());
        cmdList.addAll(getArgs());
        return new ProcessBuilder(cmdList);
    }

}
