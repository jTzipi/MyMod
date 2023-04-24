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
