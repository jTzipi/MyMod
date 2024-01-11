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
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Run the 'which' command.
 * @author jTzipi
 */
public final class WhichCmd extends AbstractInstantCommand<WhichCmd.Which> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(WhichCmd.class);
    private static final String CMD = "which";


    public enum WhichOption implements Supplier<String> {


        ALL("all"),
        SKIP_DOT("skip-dot"),
        SKIP_TILDE("skip-tilde"),
        SHOW_DOT("show-dot"),
        SHOW_TILDE("show-tilde");

        private final String arg;

        WhichOption(String argStr ) {
        this.arg = "--"+argStr;

        }


        @Override
        public String get() {
            return arg;
        }


    }

    /**
     * Result of 'which' command as record.
     * @param resultList found path(s)
     */
    protected record Which(List<String> resultList) {
    }

    private WhichCmd(String... args) {
        super(CMD, args);
    }


    /**
     * Run the which command with one or more programs to search.
     * @param args option and argument
     * @return instance of which command
     */
    public static WhichCmd of( final String args ) {
        Objects.requireNonNull(args);

        return new WhichCmd(args);
    }

    @Override
    protected Optional<Which> parse(ICommandResult commandResult) {

        return isCommandResultParsable(commandResult)
                ? Optional.of(parseRaw(commandResult.getRawResult()))
                : Optional.empty();
    }

    private static Which parseRaw( String whichResultStr ) {

        List<String> pathL = Stream.of(whichResultStr.split("\n")).toList();
        LOG.info("Read path list {}", pathL);
        return new Which(pathL);
    }
}
