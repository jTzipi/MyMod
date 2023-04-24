package eu.jpangolin.jtzipi.mymod.io.cmd;

import java.io.IOException;
import java.util.Optional;
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
    default Optional<T> launch() throws IOException, InterruptedException {

        return launch(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT);
    }

    /**
     * Launch the command.
     * The command will wait the time and unit to finish.
     * @param timeout timeout [{@linkplain #MIN_TIMEOUT} .. ]
     * @param timeUnit unit
     * @return the parsed result wrapped with Optional
     * @throws IOException I/O
     * @throws InterruptedException IE
     */
    default Optional<T> launch(long timeout, TimeUnit timeUnit) throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(getArgs());
        return launch(timeout, timeUnit, pb);
    }

    /**
     * Launch the command.
     *
     * @param timeout timeout
     * @param timeUnit time unit
     * @param processBuilder proc builder
     * @return the parsed value object wrapped into Optional. If {@linkplain Optional#isEmpty()}
     *          there is probably an error
     * @throws IOException IO
     * @throws InterruptedException IE
     */
    Optional<T> launch(long timeout , TimeUnit timeUnit , ProcessBuilder processBuilder ) throws IOException , InterruptedException;


}
