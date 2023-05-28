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

import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Simple reader for an OS process line by line.
 * <p>This is stolen from <a href="https://www.baeldung.com/run-shell-command-in-java">this</a> site.
 * </p>
 */
final class StreamGobbler implements Callable<String> {

    private static final org.slf4j.Logger LOG  = LoggerFactory.getLogger("StreamGobbler");
    private final InputStream is;

    private Consumer<String> ic;

    StreamGobbler(final InputStream inputStream, Consumer<String> inputConsumer ) {
        this.is = inputStream;
        this.ic = inputConsumer;
    }
    StreamGobbler(final InputStream inputStream) {
        this(inputStream, null);
    }

    public static StreamGobbler of( final InputStream inputStream, Consumer<String> inputConsumer) {
Objects.requireNonNull(inputStream);
return new StreamGobbler(inputStream, inputConsumer);
    }
    public static StreamGobbler of( final  InputStream inputStream) {

        return of(inputStream, null);
    }

    public void setInputConsumer( final Consumer<String> inputConsumer) {
        this.ic = inputConsumer;
    }
    @Override
    public String call() {

        StringBuilder sb = new StringBuilder();
        try (Scanner scanner = new Scanner(is)) {

            while (scanner.hasNextLine()) {


                String line = scanner.nextLine();

                if( null != ic ) {
                    ic.accept(line);
                }
                LOG.info("Gobbler scan {}", line);
                sb.append(line).append("\n");
            }
        }


        return sb.toString();
    }
}
