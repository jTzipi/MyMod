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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Common IO function.
 *
 * @author jTzipi
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
     * Placeholder for a non av. file time.
     */
    public static final FileTime FILE_TIME_NA = FileTime.from(Instant.EPOCH);
    /**
     * Match all .
     */
    public static final Predicate<? super Path> ACCEPT_ANY_PATH = path -> true;
    /**
     * Match dirs which are no symlink only.
     */
    public static final Predicate<? super Path> ACCEPT_DIR_PATH = Files::isDirectory;
    /**
     * Minimal font size.
     */
    public static final double FONT_MIN_SIZE = 2D;


    public static final Path PATH_LINUX_NOT_FOUND = Paths.get("/dev/null");
    /**
     * Match all files path filer.
     */
    public static final DirectoryStream.Filter<Path> DIR_STREAM_ACCEPT_ALL = path -> true;

    /**
     * Match all readable dirs.
     */
    public static final DirectoryStream.Filter<Path> DIR_STREAM_ACCEPT_DIR = Files::isDirectory;

    /**
     * Multipurpose 'not found' placeholder.
     */
    public static final String NA = "<NA>";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( ModIO.class );

    private ModIO() {

        throw new AssertionError();
    }

    /**
     * Read  resource bundle.
     *
     * @param cls             class from which location the resource loaded
     * @param resourceFileStr name of resource
     * @return resource bundle
     * @throws IOException           if ioe
     * @throws NullPointerException  if {@code cls}|{@code resourceFileStr} is null
     * @throws IllegalStateException if resource is not readable
     */
    public static ResourceBundle loadResourceBundle( final Class<?> cls, final String resourceFileStr ) throws IOException {
        Objects.requireNonNull( cls );

        ResourceBundle resBu;
        try ( final InputStream resIS = cls.getResourceAsStream( resourceFileStr ) ) {
            if ( null == resIS ) {
                throw new IllegalStateException( "ResourceBundle[='" + resourceFileStr + "'] not readable" );
            }
            resBu = new PropertyResourceBundle( resIS );

        }
        return resBu;
    }

    /**
     * Try to load all sub path's of path p.
     *
     * @param p path to lookup
     * @return list of path's or empty list if not readable or no dir
     * @throws IOException          if I/O
     * @throws NullPointerException if {@code p} is null
     * @see #lookupDir(Path, Predicate, boolean)
     */
    public static List<Path> lookupDir( Path p ) throws IOException {

        return lookupDir( p, null, false );
    }

    /**
     * Try to load all sub path's of path p.
     *
     * @param p  path
     * @param pp predicate
     * @return list of sub path
     * @throws IOException          ifs
     * @throws NullPointerException if {@code p} is null
     */
    public static List<Path> lookupDir( Path p, Predicate<? super Path> pp ) throws IOException {

        return lookupDir( p, pp, false );
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
     *
     * @param p             path to lookup
     * @param pathPredicate filter
     * @return list of path
     * @throws NullPointerException {@code p} is {@code null}
     * @see #lookupDir(Path)
     */
    public static List<Path> lookupDir( Path p, final Predicate<? super Path> pathPredicate, boolean followLink ) throws IOException {

        Objects.requireNonNull( p );

        if ( !Files.isReadable( p ) ) {
            LOG.info( "Try to read sub path of unreadable file[='" + p + "']" );
            return Collections.emptyList();
        }
        if ( !Files.isDirectory( p ) ) {
            LOG.info( "Try to read sub path of non directory[='" + p + "']" );
            return Collections.emptyList();
        }

        final DirectoryStream.Filter<Path> filter = null == pathPredicate
                ? DIR_STREAM_ACCEPT_ALL
                : pathPredicate::test;

        // if follow symlink we try to follow
        if ( followLink && Files.isSymbolicLink( p ) ) {
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
     * Return all sub dirs.
     *
     * @param pathToDir root dir
     * @param linkOps   follow link option
     * @return all traversable dirs beneath {@code pathToDir}
     * @throws IllegalArgumentException if {@code pathToDir} is not a dir
     * @throws NullPointerException     if {@code pathToDir} null
     */
    public static List<Path> getSubDirsOf( final Path pathToDir, LinkOption... linkOps ) {

        Objects.requireNonNull( pathToDir );
        if ( !Files.isDirectory( pathToDir, linkOps ) ) {
            throw new IllegalArgumentException( "Path[='" + pathToDir + "'] is not directory!" );
        }
        List<Path> pathL = new ArrayList<>();
        findDirsRecursive( pathToDir, pathL, linkOps );
        return pathL;
    }

    /**
     * Return all sub dirs.
     * This use the java nio {@link Files#find(Path, int, BiPredicate, FileVisitOption...)} method.
     *
     * @param pathToDir root dir
     * @param opt       visit option
     * @return dirs found
     * @throws NullPointerException if
     */
    public static List<Path> getSubDirsOfNIO( final Path pathToDir, FileVisitOption... opt ) {

        Objects.requireNonNull( pathToDir );
        if ( !Files.isDirectory( pathToDir ) ) {
            throw new IllegalArgumentException( "Path[='" + pathToDir + "'] is not directory!" );
        }


        return findDirs( pathToDir, opt );
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
            LOG.info( "Failed to read path[='" + path + "']", ioE );
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
     * Try to load an image via resources.
     *
     * @param cls class
     * @param fileNameStr file
     * @return buffered image
     * @throws IOException Failed
     * @throws IllegalStateException if {@code fileNameStr} is not readable hence the stream is null
     * @throws NullPointerException if {@code cls} | {@code fileNameStr}
     */
    public static java.awt.image.BufferedImage loadBufferedImageFromResource( final Class<?> cls, final String fileNameStr ) throws IOException {
        Objects.requireNonNull( cls );
        Objects.requireNonNull( fileNameStr );
        try( InputStream is = cls.getResourceAsStream( fileNameStr ) ) {
            if( null == is ) {
                throw new IllegalStateException("file [='"+fileNameStr+"'] not found for class [='"+cls+"']");
            }
            return ImageIO.read( is );
        }

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
     * Load a font from resource.
     * @param cls class
     * @param fileNameStr file name
     * @param fontSize font size [{@linkplain #FONT_MIN_SIZE} .. ]
     * @return font
     * @throws IOException fail to load font
     * @throws IllegalStateException if file name is not found
     * @throws NullPointerException if {@code cls} | {@code fileNameStr}
     */
    public static javafx.scene.text.Font loadFontFromResource( final Class<?> cls, final String fileNameStr, double fontSize ) throws
            IOException {

        Objects.requireNonNull( cls );
        Objects.requireNonNull( fileNameStr );
        fontSize = Math.max( fontSize, FONT_MIN_SIZE );

        try( InputStream is = cls.getResourceAsStream( fileNameStr ) ) {
            if( null == is ) {
                throw new IllegalStateException("");
            }

            return Font.loadFont( is, fontSize );
        }
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
     * Read a '.properties'-file as a resource located file.
     *
     * @param cls     cls
     * @param fileStr file name
     * @return properties
     * @throws IOException           if file not readable
     * @throws IllegalStateException if stream is null
     * @throws NullPointerException  if {@code cls}|{@code fileStr} is null
     */
    public static Properties loadPropertiesFromResource( Class<?> cls, String fileStr ) throws IOException {
        Objects.requireNonNull( cls );
        Objects.requireNonNull( fileStr );
        Properties prop = new Properties();
        loadPropertiesFromResource( cls, fileStr, prop );
        return prop;

    }
    /**
     * Read a '.properties'-file as a resource located file.
     *
     * @param cls     cls
     * @param fileStr file name
     * @param properties properties
     * @throws IOException           if file not readable
     * @throws IllegalStateException if stream is null
     * @throws NullPointerException  if {@code cls}|{@code fileStr}|{@code properties} is null
     */
    public static void loadPropertiesFromResource( final Class<?> cls, final String fileStr, final Properties properties ) throws IOException {
        Objects.requireNonNull( cls );
        Objects.requireNonNull( fileStr );
        Objects.requireNonNull( properties );

        LOG.info( "try to load '{}' from '{}'", fileStr, cls.getSimpleName() );
        try ( InputStream is = cls.getResourceAsStream( fileStr ) ) {

            if ( null == is ) {
                throw new IllegalStateException( "File '"+ fileStr+"' not found or not readable" );
            }
            properties.load( is );

        }
        LOG.info( "'{}' loaded Okay!", fileStr );


    }

    /**
     * Load a resource as a string wrapped in an StringBuilder.
     *
     * @param cls           cls to load from
     * @param fileStr       file name
     * @param appendNewLine append new line ({@literal \n}
     * @return String Builder with file content
     * @throws IOException          fail to load
     * @throws NullPointerException if {@code cls}|{@code fileStr}
     */
    public static StringBuilder loadResourceString( final Class<?> cls, String fileStr, boolean appendNewLine ) throws IOException {
        Objects.requireNonNull( cls, "class is null" );
        Objects.requireNonNull( fileStr, "File is null!" );
        StringBuilder sb = new StringBuilder();

        try ( InputStream resIs = cls.getResourceAsStream( fileStr ) ) {
            if ( null == resIs ) {
                throw new IOException( "InputStream for resource '" + fileStr + "' can not created!Was null!" );
            }

            Scanner scan = new Scanner( resIs );
            while ( scan.hasNextLine() ) {
                sb.append( scan.nextLine() );
                if ( appendNewLine ) {
                    sb.append( '\n' );

                }
            }
        }

        return sb;
    }

    /**
     * Write global JaMeLime properties file.
     *
     * @param prop    properties
     * @param comment comment (optional)
     * @throws IOException          io
     * @throws NullPointerException if {@code prop} is null
     */
    public static void writePropertiesToPath( Path path, Properties prop, String comment ) throws IOException {
        Objects.requireNonNull( path )
        ;
        Objects.requireNonNull( prop );

        if ( null == comment ) {
            comment = "<?>";
        }

        try ( BufferedWriter bw = Files.newBufferedWriter( path ) ) {
            prop.store( bw, comment );
        }

        LOG.info( "Wrote to '" + path + "' Properties!" );
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
     *
     * @param path path
     * @return optional of file creation
     * @throws NullPointerException if {@code path} is null
     */
    public static Optional<FileTime> readFileCreationTimeSafe( final Path path ) {

        FileTime ft;

        try {
            ft = readFileCreationTime( path );
        } catch ( IOException ioE ) {

            ft = null;
        }

        return null == ft ? Optional.empty(): Optional.of(ft);
    }

    private static void findDirsRecursive( Path path, List<Path> dirList, LinkOption... lop ) {

        // we collect valid dirs
        if ( !Files.isReadable( path ) || !Files.isDirectory( path, lop ) ) {
            return;
        }

        dirList.add( path );
        try ( DirectoryStream<Path> dsd = Files.newDirectoryStream( path, dir -> Files.isDirectory( path, lop ) ) ) {
            for ( Path dir : dsd ) {
                findDirsRecursive( dir, dirList );
            }
        } catch ( IOException ioE ) {

            LOG.info( "Failed to read dir[='" + path + "']", ioE );
        }

    }

    private static List<Path> findDirs( final Path rootDir, FileVisitOption... fop ) {

        BiPredicate<Path, BasicFileAttributes> bp = ( Path path, BasicFileAttributes bfa ) -> Files.isReadable( path ) && bfa.isDirectory();

        try ( Stream<Path> dirStream = Files.find( rootDir, Integer.MAX_VALUE, bp, fop ) ) {

            return dirStream.collect( Collectors.toList() );
        } catch ( final IOException ioE ) {
            LOG.info( "Failed to read dir", ioE );

            return Collections.emptyList();
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