package eu.jpangolin.jtzipi.mymod.io.cmd;

/**
 * This is the 'result' of a native system command.
 * @param result raw result
 * @param exitCode exit code
 * @param t error
 */
record CommandResult(String result, int exitCode, Throwable t) implements ICommandRawResult {

    @Override
    public int getExitCode() {
        return exitCode;
    }

    @Override
    public Throwable getError() {
        return t;
    }

    @Override
    public String getRawResult() {
        return result;
    }

}
