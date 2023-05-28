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

package eu.jpangolin.jtzipi.mymod.io.cmd.linux;

import eu.jpangolin.jtzipi.mymod.io.cmd.AbstractInstantCommand;
import eu.jpangolin.jtzipi.mymod.io.cmd.ICommandResult;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class WhichCmd extends AbstractInstantCommand<WhichCmd.Which> {

    private static final String CMD = "which";


    record Which() {

    }

    protected WhichCmd(List<String> args) {
        super(CMD, args);
    }
    public static WhichCmd of( final String progStr ) {
        return new WhichCmd(List.of(Objects.requireNonNull(progStr)));
    }

    @Override
    protected Optional<Which> parse(ICommandResult commandResult) {
        return Optional.empty();
    }

}
