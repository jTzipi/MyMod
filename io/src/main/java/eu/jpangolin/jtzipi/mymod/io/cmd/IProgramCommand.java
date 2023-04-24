package eu.jpangolin.jtzipi.mymod.io.cmd;

import java.io.IOException;

/**
 * Describes a command which start another program.
 */
public interface IProgramCommand extends ICommand {


    ProcessHandle start() throws IOException;

}
