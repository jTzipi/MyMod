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

package eu.jpangolin.jtzipi.mymod.io.cmd.independent;

import eu.jpangolin.jtzipi.mymod.io.cmd.IProgramCommand;

import java.io.IOException;
import java.util.List;

public class VlcCmd implements IProgramCommand {


    private static final String CMD = "vlc";

    private List<String> args;

    private VlcCmd(final String... argStr) {
this.args = List.of(argStr);
    }

    @Override
    public String getName() {
        return CMD;
    }

    @Override
    public List<String> getArgs() {

        return args;
    }



    @Override
    public ProcessHandle start() throws IOException {
        return null;
    }
}
