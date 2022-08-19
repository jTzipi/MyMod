package eu.jpangolin.jtzipi.mymod.io;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import java.util.function.Predicate;

/**
 * Common IO function.
 */
public final class ModIO {
    /**
     * Length of dir.
     */
    public static final long DIR_LENGTH = -1L;
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
     * Try to probe the file type.
     * @param path path
     * @return MIME type of file if java can probe or {@linkplain #NA}
     */
    public static String probePathTypeSafe( final Path path ) {
        Objects.requireNonNull( path );
        String type;
        try {
            type= Files.probeContentType(path);
        } catch ( IOException ioE ) {
            type = NA;
        }

        return type;
    }

    /**
     * Try to read file creation time of path.
     * @param path path
     * @return creation time
     * @throws IOException failed
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

    private static BasicFileAttributes basicFileAttributes( final Path path ) throws IOException {

        return Files.readAttributes( path, BasicFileAttributes.class );
    }
}
