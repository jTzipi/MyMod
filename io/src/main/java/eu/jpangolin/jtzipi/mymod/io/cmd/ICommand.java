package eu.jpangolin.jtzipi.mymod.io.cmd;

import java.util.List;

/**
 * Abstraction of a OS native command.
 *
 * @author jTzipi
 */
public interface ICommand {

    /**
     * Return name of command.
     * @return command name
     */
    String getName();

    /**
     * Return argument s.
     * @return argument list
     */
    List<String> getArgs();

}
