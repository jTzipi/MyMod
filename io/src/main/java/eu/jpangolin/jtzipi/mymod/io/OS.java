/*
 * Copyright (c) 2022 Tim Langhammer
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

package eu.jpangolin.jtzipi.mymod.io;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

/**
 * Enum of well known OS.
 *
 * @author jTzipi
 */
public enum OS {

    /**
     * Linux Unix.
     */
    LINUX( "/" ),
    /**
     * Windows.
     */
    WINDOWS( System.getenv( "COMPUTERNAME" ) ),
    /**
     * DOS.
     */
    DOS( "C:" ),
    /**
     * MacOS.
     */
    MAC( "/" ),
    /**
     * Solaris.
     */
    SOLARIS( "/" ),
    /**
     * Other.
     */
    OTHER( null );

    private static final Properties SYS_PROP = System.getProperties();
    private static final Map<String, String> SYS_ENV = System.getenv();
    // root path
    private final String path;

    /**
     * Operating System.
     *
     * @param rootPathStr path to root
     */
    OS( final String rootPathStr ) {

        this.path = rootPathStr;
    }

    /**
     * Try to determine <b>this</b> OS reading System property 'os.name'.
     *
     * @return OS
     */
    public static OS getSystemOS() {

        final String ostr = OS.getOSName().toLowerCase();

        final OS os;
        // Linux Unix
        if ( ostr.matches( ".*(nix|nux|aix).*" ) ) {
            os = LINUX;
        } else if ( ostr.matches( ".*sunos.*" ) ) {
            os = SOLARIS;
        } else if ( ostr.matches( ".*mac.*" ) ) {
            os = MAC;
        } else if ( ostr.matches( ".*win.*" ) ) {
            os = WINDOWS;
        } else if ( ostr.matches( ".*dos.*" ) ) {
            os = DOS;
        } else {
            os = OTHER;
        }

        return os;
    }

    /**
     * Return OS name.
     *
     * @return name of OS or if not readable '<NA>'
     */
    public static String getOSName() {

        return SYS_PROP.getProperty( "os.name", ModIO.NA );
    }

    /**
     * Return home dir of user.
     *
     * @return user home or '.' if failed to read
     */
    public static Path getHomeDir() {

        return Paths.get( SYS_PROP.getProperty( "user.home", "." ) );
    }

    /**
     * Return user dir.
     *
     * @return user dir or '.' if failed to read
     */
    public static Path getUserDir() {

        return Paths.get( SYS_PROP.getProperty( "user.dir", "." ) );
    }

    /**
     * Return username.
     *
     * @return username
     */
    public static String getUser() {

        return SYS_PROP.getProperty( "user.name", ModIO.NA );
    }

    /**
     * Return system environment properties.
     *
     * @return map system env
     */
    public static Map<String, String> readSysProp() {
        return SYS_ENV;
    }

    /**
     * Root path.
     *
     * @return path to system root
     */
    public String getRootPathStr() {

        return path;
    }
}
