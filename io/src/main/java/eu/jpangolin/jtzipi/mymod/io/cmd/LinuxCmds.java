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
     *
     */
    public static LsblkCmd lsblk() {
        return new LsblkCmd();
    }


}
