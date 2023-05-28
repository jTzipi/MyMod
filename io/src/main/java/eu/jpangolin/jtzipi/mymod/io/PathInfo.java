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

package eu.jpangolin.jtzipi.mymod.io;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Path information.
 * <p>
 * Here we want to get <u>system dependent</u> path information.
 * Therefor we do <u>not</u> use the java.nio.path API but the older
 * {@link FileSystemView} API and the {@link File} API.
 * <p>
 */
public final class PathInfo {


    private static final FileSystemView FSV = FileSystemView.getFileSystemView();

    /**
     * Private Access to prevent instance.
     */
    private PathInfo() {

        throw new AssertionError();
    }

    /**
     * Return whether a path exist.
     *
     * @param path path
     * @return {@code true} if this file is existing
     * @throws NullPointerException {@code path} is null
     */
    public static boolean isExisting( final Path path ) {

        return tof( path ).exists();
    }

    /**
     * Return whether {@code path} is a real file or directory.
     *
     * @param path path
     * @return {@code true} if {@code path} is a real file or directory
     */
    public static boolean isRegular( final Path path ) {

        return FSV.isFileSystem( tof( path ) );
    }

    /**
     * Return whether a file exists and file  can read.
     * This may return {@code true} but  unprivileged user
     * can <u>not</u> enter.
     *
     * @param path path
     * @return file is readable
     */
    public static boolean isReadable( final Path path ) {

        final File file = tof( path );

        return file.exists() && file.canRead();
    }


    public static boolean isWritable( final Path path ) {

        final File file = tof( path );

        return file.exists() && file.canWrite();
    }

    public static boolean isExecutable( final Path path ) {

        final File file = tof( path );

        return file.exists() && file.canExecute();
    }

    public static boolean isDrive( final Path path ) {

        return FSV.isDrive( tof( path ) );
    }

    /**
     * Return whether path is computer 'root'.
     * on linux like systems this is '/'.
     *
     * @param path path
     * @return {@code true} if {@code path} is root path

    public static boolean isRoot( final Path path ) {

        Objects.requireNonNull( path );
        return OS.getSystemOS().getRootPathStr().equalsIgnoreCase( path.toString() );
    }
*/
    public static boolean isDir( final Path path ) {

        return tof( path ).isDirectory();
    }

    public static boolean isLink( final Path path ) {

        return FSV.isLink( tof( path ) );
    }

    public static boolean isUserHome( final Path path ) {

        return OS.getHomeDir().equals( path );
    }

    public static boolean isFileSystemNode( final Path path ) {

        return FSV.isComputerNode( tof( path ) );
    }

    public static boolean isDirTraversable( final Path path ) {

        return FSV.isTraversable( tof( path ) );
    }

    public static boolean isFileSystemRoot( final Path path ) {

        return FSV.isFileSystemRoot( tof( path ) );
    }


    public static long getLength( final Path path ) {

        return isDir( path ) ? ModIO.PATH_DIR_LENGTH : tof( path ).length();
    }

    /**
     * Return whether path is hidden.
     *
     * @param path path
     * @return
     */
    public static boolean isHidden( final Path path ) {

        return tof( path ).isHidden();
    }

    public static String fileSystemName( final Path path ) {

        return FSV.getSystemDisplayName( tof( path ) );
    }

    /**
     * Return file type description.
     *
     * @param path path
     * @return System description of file
     */
    public static String fileSystemTypeDesc( final Path path ) {

        return FSV.getSystemTypeDescription( tof( path ) );
    }

    /**
     * Return system dependent icon for path.
     *
     * @param path path
     * @return javax.swing.Icon icon
     * @throws NullPointerException if {@code path} is null
     */
    public static javax.swing.Icon fileSystemIcon( final Path path ) {

        return FSV.getSystemIcon( tof( path ) );
    }

    private static File tof( final Path path ) {

        return Objects.requireNonNull( path ).toFile();
    }
}
