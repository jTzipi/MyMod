package eu.jpangolin.jtzipi.mymod.io.cmd;

import eu.jpangolin.jtzipi.mymod.io.cmd.linux.LsblkCmd;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Collection of useful linux commands.
 */
public final class LinuxCmds {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( LinuxCmds.class );


    private LinuxCmds() {
        throw new AssertionError("What do you want");
    }

    /**
     * Launch 'lsblk' command with default argument.
     *
     * @return Lsblk Command
     * @see LsblkCmd#ARGS_DEF
     */
    public static LsblkCmd lsblk() {
        return new LsblkCmd(Arrays.asList(LsblkCmd.ARGS_DEF));
    }


}
