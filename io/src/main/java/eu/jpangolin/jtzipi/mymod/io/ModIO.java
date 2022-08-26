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

import javafx.scene.text.Font;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import java.util.function.Predicate;

/**
 * Common IO function.
 */
public final class ModIO {
    /**
     * Length of dir.
     */
    public static final long PATH_DIR_LENGTH = -1L;

    /**
     * Length of a file we can not determine.
     */
    public static final long LENGTH_PATH_NA = -2L;

    /**
     * Match all .
     */
    public static final Predicate<? super Path> ACCEPT_ANY_PATH = path -> true;
    /**
     * Match dirs which are no symlink only.
     */
    public static final Predicate<? super Path> ACCEPT_DIR_PATH = Files::isDirectory;

    public static final double FONT_MIN_SIZE = 2D;
    /**
     * Match all files path filer.
     */
    public static final DirectoryStream.Filter<Path> DIR_STREAM_ACCEPT_ALL = path -> true;


    /**
     * File Time for failed read attempt.
     */
    public static final FileTime FILE_TIME_NA = FileTime.from( Instant.from( LocalDate.of( 1900, 1, 1 ) ) );
    /**
     * Multipurpose 'not found' placeholder.
     */
    public static final String NA = "<NA>";
    private static final org.slf4j.Logger LOG  = LoggerFactory.getLogger( ModIO.class );

    private ModIO() {

        throw new AssertionError();
    }

    /**
     * Try to load all sub path's of path p.
     *
     * @param p path to lookup
     * @return list of path's or empty list if not readable or no dir
     * @throws IOException if I/O
     * @throws NullPointerException if {@code p} is null
     * @see #lookupDir(Path, Predicate, boolean)
     */
    public static List<Path> lookupDir(   Path p ) throws IOException {

        return lookupDir( p, null, false );
    }

    /**
     * Try to load all sub path's of path p.
     * @param p path
     * @param pp predicate
     * @return list of sub path
     * @throws IOException ifs
     * @throws NullPointerException if {@code p} is null
     */
    public static List<Path> lookupDir( Path p, Predicate<? super Path> pp ) throws IOException {

        return lookupDir( p, pp, false );
    }

    public static long getSubDirsOf( final Path pathToDir ) {

        long cnt = 0L;
        dir( pathToDir, cnt );
        return cnt;
    }

    /**
     * Lookup path for sub path.
     * <p>
     * Path should be a directory to make sense.
     * <p>
     * In case of a <u>non</u> dir file or a not readable dir file
     * we return an {@link Collections#emptyList()}.
     * <p>
     * In case of a <u>symbolic link</u> with the {@code followLink} option set
     * we try to follow the symlink and return this path's files.
     * @param p             path to lookup
     * @param pathPredicate filter
     * @return list of path
     * @throws NullPointerException {@code p} is {@code null}
     * @see #lookupDir(Path)
     */
    public static List<Path> lookupDir(   Path p, final Predicate<? super Path> pathPredicate, boolean followLink ) throws IOException {

        Objects.requireNonNull( p );

        if ( !Files.isReadable( p ) ) {
            LOG.info( "Try to read sub path of unreadable file[='" + p + "']" );
            return Collections.emptyList();
        }
        if ( !Files.isDirectory( p ) ) {
            LOG.info( "Try to read sub path of non directory[='"+p+"']" );
            return Collections.emptyList();
        }

        final DirectoryStream.Filter<Path> filter = null == pathPredicate
                ? DIR_STREAM_ACCEPT_ALL
                : pathPredicate::test;

        // if follow symlink we try to follow
        if( followLink && Files.isSymbolicLink( p ) ) {
            p = Files.readSymbolicLink( p );
        }

        final List<Path> nodeL = new ArrayList<>();

        try ( final DirectoryStream<Path> ds = Files.newDirectoryStream( p, filter ) ) {

            for ( final Path path : ds ) {
                System.out.println( "Gadi : [" + path + "]" );
                nodeL.add( path );
            }

        }

        return nodeL;
    }

    /**
     * Format bytes.
     *
     * @param bytes byte
     * @param si    standard unit
     * @return formatted file size
     */
    public static String formatFileSize( final long bytes, final boolean si ) {
        // no formatting
        if ( 0 >= bytes ) {
            return "0 B";
        }
        final int unit = si ? 1000 : 1024;
        // if no need to format
        if ( unit > bytes ) {
            return bytes + " B";
        }

        final String unitSymbol = si ? "kMGT" : "KMGT";

        final int exp = ( int ) ( Math.log( bytes ) / Math.log( unit ) );

        final double ri = bytes / Math.pow( unit, exp );


        final String pre = unitSymbol.charAt( exp - 1 ) + ( si ? "" : "i" );
        return String.format( "%.1f %sB", ri, pre );
    }


    /**
     * Try to probe the file type.
     *
     * @param path path
     * @return MIME type of file if java can probe or {@linkplain #NA}
     * @throws NullPointerException if {@code path} is null
     */
    public static String probePathTypeSafe( final Path path ) {

        Objects.requireNonNull( path );
        String type;
        try {
            type = Files.probeContentType( path );
        } catch ( IOException ioE ) {
            type = NA;
        }

        return type;
    }

    /**
     * Return prefix of a file.
     * <p>
     * Return the prefix that is the part of the file until the last dot.
     * For Example the prefix of /home/user/data.dat == data.
     * If the file is
     *     <ul>
     *         <li>a dir; we return the file name</li>
     *         <li>not readable; we return {@link #NA}</li>
     *         <li>a file without a path name(like 'C:') we return the path name</li>
     *     </ul>
     * </p>
     *
     * @param path path
     * @return prefix of the file; or path name if path have no path name or is a dir; or {@link #NA} if path is not readable
     * @throws NullPointerException if {@code path} is null
     */
    public static String getFileNamePrefixSafe( final Path path ) {

        try {
            return getFileNamePrefix( path );
        } catch ( IOException ioE ) {
            LOG.debug( "Failed to read path[='" + path + "']", ioE );
            return NA;
        }
    }

    /**
     * Return suffix of a file.
     * <p>
     * Return the suffix that is the part of the file after the last dot.
     * For Example the suffix of /home/user/data.dat == dat.
     * If the file is
     *     <ul>
     *         <li>a dir; we return the file name</li>
     *         <li>not readable; we return {@link #NA}</li>
     *         <li>a file without a path name(like 'C:') we return the path name</li>
     *     </ul>
     * </p>
     *
     * @param path path
     * @return suffix of the file; or path name if path have no path name or is a dir; or {@link #NA} if path is not readable
     * @throws NullPointerException if {@code path} is null
     * @see #getFileNameSuffix(Path)
     */
    public static String getFileNameSuffixSafe( final Path path ) {

        try {
            return getFileNameSuffix( path );
        } catch ( IOException ioE ) {
            LOG.debug( "Failed to read path[='" + path + "']", ioE );
            return NA;
        }
    }

    /**
     * Read the path's file name prefix.
     *
     * @param path path
     * @return prefix of a file or
     * @throws IOException          if {@code path} can not read
     * @throws NullPointerException if {@code path} is null
     */
    public static String getFileNamePrefix( final Path path ) throws IOException {

        Objects.requireNonNull( path );
        if ( !Files.isReadable( path ) ) {
            throw new IOException( "The file[='" + path + "] is not readable!" );
        }
        if ( PathInfo.isDir( path ) ) {

            LOG.info( "Try to read prefix of a directory[=" + path + "]" );
        }
        Path pathName = path.getFileName();
        // Some special files like 'C:' have null file name
        if ( null == pathName ) {
            LOG.info( "Try to read prefix of no filename path[=" + path + "]" );
            return path.toString();
        }

        // this is always String[2]
        String[] part = split( pathName.toString() );

        return part[0];
    }

    /**
     * Read the path's file suffix.
     *
     * @param path path
     * @return file suffix or
     * @throws IOException          if {@code path} is not readable
     * @throws NullPointerException if {@code path} is null
     */
    public static String getFileNameSuffix( final Path path ) throws IOException {

        Objects.requireNonNull( path );
        if ( !Files.isReadable( path ) ) {
            throw new IOException( "The file[='" + path + "] is not readable!" );
        }
        if ( PathInfo.isDir( path ) ) {

            LOG.info( "Try to read prefix of a directory[=" + path + "]" );
        }
        Path pathName = path.getFileName();
        // Some special files like 'C:' have null file name
        if ( null == pathName ) {
            LOG.info( "Try to read prefix of no filename path[=" + path + "]" );
            return path.toString();
        }

        // this is always String[2]
        String[] part = split( pathName.toString() );

        return part[1];
    }

    /**
     * Try to load a file as a BufferedImage.
     *
     * @param path path to file
     * @return buffered image
     * @throws IOException          if file is not readable or failed to read as image
     * @throws NullPointerException if  {@code path} is null
     */
    public static java.awt.image.BufferedImage loadBufferedImage( final Path path ) throws IOException {

        Objects.requireNonNull( path );
        if ( !Files.isReadable( path ) ) {
            throw new IOException( "Path[='" + path + "'] is not readable" );
        }
        return ImageIO.read( path.toFile() );

    }

    /**
     * Try to load a file as a javafx image.
     *
     * @param path path to image
     * @return image
     * @throws IOException          failed to read file
     * @throws NullPointerException if {@code path} is null
     */
    public static javafx.scene.image.Image loadJavaFXImage( final Path path ) throws IOException {

        Objects.requireNonNull( path, "Path is null" );

        if ( !Files.isReadable( path ) ) {
            throw new IOException( "Path[='" + path + "'] is not readable" );
        }

        try ( InputStream fis = Files.newInputStream( path ) ) {
            return new javafx.scene.image.Image( fis );
        }
    }

    /**
     * Load a JavaFX font from path.
     *
     * @param path path
     * @param size size
     * @return Font object
     * @throws IOException          io loading font or {@code path} is not readable
     * @throws NullPointerException if {@code path} is null
     */
    public static javafx.scene.text.Font loadFont( final Path path, final double size ) throws IOException {

        Objects.requireNonNull( path );
        // Error
        if ( !Files.isReadable( path ) ) {
            throw new IOException( "Path '" + path + "' is not readable" );
        }

        // try
        final Font font;
        try ( final InputStream io = Files.newInputStream( path ) ) {

            font = Font.loadFont( io, size );
        }

        return font;

    }

    /**
     * Try to load a JavaFX font or return default system font.
     *
     * @param path path to font
     * @param size font size
     * @return font or system default
     * @throws NullPointerException if {@code path} is null
     */
    public static javafx.scene.text.Font loadFontSafe( final Path path, double size ) {

        Objects.requireNonNull( path );
        if ( size < FONT_MIN_SIZE ) {
            size = FONT_MIN_SIZE;
        }
        Font font;
        try {
            font = loadFont( path, size );
        } catch ( final IOException ioe ) {
            LOG.info( "Failed to load font for path[='" + path + "']" );
            font = Font.getDefault();
        }
        return font;
    }

    /**
     * Load Properties from path.
     *
     * @param pathToProp path to properties
     * @param prop       properties
     * @throws IOException              if {@code pathToProp} !readable
     * @throws NullPointerException     if {@code pathToProp} is null
     * @throws IllegalArgumentException if {@code pathToProp} is dir
     */
    public static void loadProperties( final Path pathToProp, Properties prop ) throws IOException {

        Objects.requireNonNull( pathToProp );
        //
        if ( !Files.isReadable( pathToProp ) ) {
            throw new IOException( "Path[='" + pathToProp + "'] not readable" );
        }
        // If no file throw
        if ( Files.isDirectory( pathToProp ) ) {
            throw new IllegalArgumentException( "You try to read properties from dir[='" + pathToProp + "']" );
        }
        //
        if ( null == prop ) {
            prop = new Properties();
        }

        try ( final InputStream inStream = Files.newInputStream( pathToProp ) ) {
            prop.load( inStream );
        }

    }

    /**
     * Try to read the properties contained in the file.
     *
     * @param path path to file
     * @return properties
     * @throws IOException              read of path failed
     * @throws NullPointerException     if {@code path} is null
     * @throws IllegalArgumentException if {@code pathToProp} is dir
     */
    public static Properties loadProperties( final Path path ) throws IOException {

        Properties prop = new Properties();

        loadProperties( path, prop );

        return prop;
    }

    /**
     * Try to read file creation time of path.
     *
     * @param path path
     * @return creation time
     * @throws IOException          failed
     * @throws NullPointerException if {@code path} is null
     */
    public static FileTime readFileCreationTime( final Path path ) throws IOException {

        Objects.requireNonNull( path );

        BasicFileAttributes bfa = basicFileAttributes( path );

        return bfa.creationTime();
    }

    /**
     * Try to read file creation time.
     * @param path path
     * @return file creation time or {@linkplain #FILE_TIME_NA}
     *@throws NullPointerException if {@code path} is null
     */
    public static FileTime readFileCreationTimeSafe( final Path path ) {

        FileTime ft;

        try {
            ft = readFileCreationTime( path );
        } catch ( IOException ioE ) {

            ft = FILE_TIME_NA;
        }

        return ft;
    }

    private static void dir( Path path, long cnt ) {

        if ( !Files.isReadable( path ) || !Files.isDirectory( path ) ) {
            return;
        }
        cnt++;
        try {
            for ( Path dir : ModIO.lookupDir( path, ACCEPT_DIR_PATH ) ) {
                dir( dir, cnt );
            }
        } catch ( IOException e ) {

            LOG.info( "Failed to read dir[='" + path + "']" );
        }

    }

    /**
     * Split a string before the last '.'.
     * <p>
     * If the file name did not contain a '.' we return the file name
     * and set the second item ''.
     * </p>
     *
     * @param fileName file name
     * @return [0]=file name part until last '.' [1] file name part after last '.'
     */
    private static String[] split( final String fileName ) {

        assert null != fileName : "File name is null";

        final int lastDot = fileName.lastIndexOf( '.' );

        final String[] ret = new String[2];
        ret[0] = lastDot > 0 ? fileName.substring( 0, lastDot ) : fileName;
        ret[1] = lastDot > 0 ? fileName.substring( lastDot ) : "";

        return ret;
    }


    /**
     * Try to read {@link BasicFileAttributes}.
     *
     * @param path path
     * @return basic file attributes
     * @throws IOException I/O
     */
    private static BasicFileAttributes basicFileAttributes( final Path path ) throws IOException {

        return Files.readAttributes( path, BasicFileAttributes.class );
    }
}
