package eu.jpangolin.jtzipi.mymod.io.cmd;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * AbstractInstantCommand.
 * <p>
 *     Abstract basic implementation of the raw command reading.
 *     <br/>
 *     Here we encapsulate the call of the command.
 *     The result can be inspected with the {@link ICommandResult}
 *     we create from the native command call.
 *     <br/>
 *      The user only needs to override the {@linkplain #parse(ICommandResult)} method.
 *
 *      Either there is a valid result or we can return an empty .
 *      <br/>
 *      If you want to implement your own command setup you can override
 *      the {@link #launch()} method.
 * </p>
 * @author jTzipi
 * @param <T> Type of command result value object
 */
public abstract class AbstractInstantCommand<T> implements IInstantCommand<T> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AbstractInstantCommand.class);
    /**
     * Arguments.
     */
    protected List<String> args;
    /**
     * Command name.
     */
    protected String cmd;
    /**
     * AbstractInstantCommand.
     * @param command command
     * @param argsList option list
     */
    protected AbstractInstantCommand( final String command, final List<String> argsList ) {
        this.cmd = command;
        this.args = argsList;
    }

    /**
     * Parse the raw result wrapped in the command result and return
     * the value object wrapped in an Optional.
     * @param commandResult command result
     * @return Value Object of the command
     * @throws NullPointerException if {@code commandResult} is null
     */
    protected abstract Optional<T> parse(ICommandResult commandResult);

    @Override
    public Optional<T> launch(long timeout, TimeUnit timeUnit, ProcessBuilder processBuilder) throws IOException, InterruptedException {
        timeout = Math.max(timeout, MIN_TIMEOUT);
        if (null == timeUnit) {
            timeUnit = DEFAULT_TIMEOUT_UNIT;
        }

        //
        // - use the builder if set.
        //   if not we create a simple builder with arguments
        ProcessBuilder pb = null == processBuilder
                ? new ProcessBuilder(getArgs())
                : processBuilder;


        // - arguments for our ICommandResult
        //

        String rawResult;           // raw output of command
        Throwable t;                //
        //
        // - start -
        // may throw I/O
        Process p = pb.start();

        // read from the input
        // and error stream async
        // because we don't want to stop our other code
        // in case the command output is not responsive
        Future<String> rawOutputF = Executors.newSingleThreadExecutor().submit(StreamGobbler.of(p.getInputStream()));
        Future<String> rawError = Executors.newSingleThreadExecutor().submit(StreamGobbler.of(p.getErrorStream()));

        // Wait for end of p
        // or destroy forcible
        // This can throw an IE!
        // Caution!
        // the boolean only indicates that
        // the command don't exit until timeout
        boolean done = p.waitFor(timeout, timeUnit);



        // - seem to be okay
        if (done) {

            LOG.info("Cmd completed without error :) (code = {})", p.exitValue() );
            try {

                // this throws an IE
                // TODO: Use CompletableFuture ?
                rawResult = rawOutputF.get(timeout, timeUnit);
                t = null;


            } catch (ExecutionException | TimeoutException eE) {


                t = eE.getCause();
                rawResult = ICommandResult.RAW_RESULT_ERROR;
                LOG.warn("Proc done but Failed to wait for the result of {}, with args '{}' ", getName(), getArgs(), eE);


            }
        } else {
            if(p.isAlive()) {

                LOG.warn("The command {} is not exited" , getName());
            }

            //
            // command did not return until timeout
            //
            rawResult = ICommandResult.RAW_RESULT_ERROR;

            //
            p = p.destroyForcibly();

            // try to read from the error stream
            String errorCode;
            // Caution!
            // if the proc is not exit this throws
            // an IllegalThreadStateException.
            //
            int exit = p.exitValue();
            LOG.warn("Cmd completed with error or because of timeout :( (code = {}) We try to", exit);
            try {
                errorCode = rawError.get(MIN_TIMEOUT, TimeUnit.MILLISECONDS);
                t = new IOException(errorCode);

            } catch (ExecutionException | TimeoutException e) {

                LOG.warn("Command {} did not exit and we can't obtain the error stream", getName(), e );
                t = e.getCause();
            }

        }

        // now parse the java value object
        return parse(new CommandResult(rawResult, p, t));
    }
    @Override
    public String getName() {
        return cmd;
    }

    /**
     * Set arguments.
     * @param firstArg first arg
     * @param otherArgs more args
     * @throws NullPointerException if {@code firstArg}
     */
    public void setArgs( String firstArg, String... otherArgs ) {
        Objects.requireNonNull(firstArg);

        args.clear();
        args.add(firstArg);
        if(null != otherArgs) {

            args.addAll(Stream.of(otherArgs).filter(Objects::nonNull).toList());

        }
    }
    @Override
    public List<String> getArgs() {
        return args;
    }
}
