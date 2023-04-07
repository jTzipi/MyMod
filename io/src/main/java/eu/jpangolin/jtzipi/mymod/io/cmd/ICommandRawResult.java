package eu.jpangolin.jtzipi.mymod.io.cmd;

public interface ICommandRawResult {

    int getExitCode();
    Throwable getError();
    String getRawResult();

}
