package eu.jpangolin.jtzipi.mymod.io.cmd;

/**
 * This is the 'result' of a native system command.
 * @param result raw result
 * @param proc proc
 * @param t error
 *
 * @author jTzipi
 */
record CommandResult(  String result, Process proc, Throwable t) implements ICommandResult {


    @Override
    public Process getProcess() {
        return proc;
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
