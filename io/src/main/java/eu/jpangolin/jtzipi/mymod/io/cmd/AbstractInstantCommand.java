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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


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
     * List of our command AND arguments.
     */
    protected List<String> cmdArgL;
    /**
     * Command name.
     */
    protected String cmd;

    /**
     * AbstractInstantCommand.
     * @param commandStr command name
     *
     * @param argStr arbitrary command arguments
     */
    protected AbstractInstantCommand(final String commandStr, final List<String> argStr ) {
        this.cmd = commandStr;
        this.cmdArgL = argStr;
    }

    protected AbstractInstantCommand(final String commandStr, String... argsA ) {
        this(commandStr, Arrays.asList(argsA));
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
    public Optional<T> launch(long timeout, TimeUnit timeUnit) throws IOException, InterruptedException {
        timeout = Math.max(timeout, MIN_TIMEOUT);
        if (null == timeUnit) {
            timeUnit = DEFAULT_TIMEOUT_UNIT;
        }

        final ProcessBuilder pb = getProcessBuilder();

        LOG.info("Start command '{}' with arg '{}'", cmd, cmdArgL);
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


    @Override
    public List<String> getArgs() {
        return cmdArgL;
    }

    public void setArgs(final List<String> cmdArgList) {
        Objects.requireNonNull(cmdArgList);
        this.cmdArgL = cmdArgList;
    }



    /**
     * Check whether we can parse the result.
     * @param result result
     * @return {@code true} if the result is parsable
     * @throws NullPointerException if {@code result}
     */
    protected static boolean isCommandResultParsable(ICommandResult result) {
        Objects.requireNonNull(result);
        return result.getError() == null
                && null != result.getRawResult()
                && !Objects.equals(ICommandResult.RAW_RESULT_ERROR, result.getRawResult());
    }
}
