package eu.jpangolin.jtzipi.mymod.io.cmd;

/**
 * Wrapper for the result of an OS native command.
 *
 */
public interface ICommandResult {

    /**
     * Default Error .
     */
    String RAW_RESULT_ERROR = "<?ERROR>";

    /**
     * Pro of command.
     * @return proc
     */
    Process getProcess();

    /**
     * Optional Error.
     * @return error if something went wrong
     */
    Throwable getError();

    /**
     * Answer of the command.
     * This is only useful for instant commands
     * @return raw answer
     */
    String getRawResult();

}
