/*
 * Copyright (c) 2022-2024. Tim Langhammer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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


import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * Checksum Utils.
 * <p>
 * Several methods to compute hash value.
 * </p>
 *
 * @author jTzipi
 */
public final class Checksums {

    /**
     * Min Buffer Size.
     */
    public static final int MIN_BUF_SIZE = 2048;    // 2^11
    /**
     * Large Buffer Size.
     */
    public static final int LARGE_BUF_SIZE = 2_097_152;     // 2^21
    /**
     * Default MD.
     * - SHA512 -
     */
    public static final MessageDigest DEFAULT_DIGEST = DigestUtils.getSha512Digest();
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( "Checksums" );
    private static final long LARGE_FILE = 4_194_304L;

    private Checksums() {

    }

    private static void checkPath( final Path path ) throws IOException {

        Objects.requireNonNull( path, "path should be != null" );

        // File readable
        if ( !Files.isReadable( path ) ) {
            throw new IOException( "File '" + path + "' is not readable" );
        }
// make not sense
        if ( Files.isDirectory( path ) ) {
            throw new IllegalArgumentException( "Path '" + path + "' seem to be a dir" );
        }

        if ( Files.size( path ) == 0L ) {
            throw new IllegalArgumentException( "Path '" + path + "' size is 0!" );
        }
    }

    private static int calcBuffer( int buffer, long fsize ) {
        assert fsize > 0L;

        if ( fsize >= LARGE_FILE && buffer < LARGE_BUF_SIZE ) {
            buffer = LARGE_BUF_SIZE;
            LOG.info( "Detect large file[={}] and small buffer[={}] Use Large Buffer", fsize, buffer );
        } else {
            buffer = Math.max( buffer, MIN_BUF_SIZE );
            LOG.info( "Detect normal file set buffer to " + buffer );
        }

        return buffer;
    }

    private static int bufferFor( long fileSize ) {

        return fileSize < LARGE_BUF_SIZE ? MIN_BUF_SIZE : LARGE_BUF_SIZE;
    }

    /**
     * Calculate hash value for a file using streaming.
     *
     * @param path path
     * @return hash
     * @throws IOException              file is not readable or stream failed
     * @throws IllegalArgumentException if {@code path} is a dir
     * @throws NullPointerException     if {@code path} is null
     */
    public static String calcHashStreaming( final Path path ) throws IOException {

        return calcHashStreaming( path, DEFAULT_DIGEST );
    }

    /**
     * Calculate hash value for a file using streaming.
     *
     * @param path path
     * @param md   message digest
     * @return hash
     * @throws IOException              {@code path} is not readable
     * @throws NullPointerException     if {@code path} is null
     * @throws IllegalArgumentException if {@code path} is not a file
     */
    public static String calcHashStreaming( final Path path, MessageDigest md ) throws IOException {

        int buffer = bufferFor( Files.size( path ) );
        return calcHashStreaming( path, md, buffer );
    }

    /**
     * s
     * Calculate hash value for path using DigestInputStream.
     *
     * @param path    path to file
     * @param md      message digest
     * @param bufSize buffer size [{@link #MIN_BUF_SIZE} .. ]
     * @return hash value
     * @throws IOException              i/o error or path is not readable
     * @throws IllegalArgumentException if {@code path} is a dir
     * @throws NullPointerException     if {@code path} is
     */
    public static String calcHashStreaming( final Path path, MessageDigest md, int bufSize ) throws IOException {

        checkPath( path );

// default digest
        if ( null == md ) {

            md = DEFAULT_DIGEST;
        }

        bufSize = calcBuffer( bufSize, Files.size( path ) );

        byte[] buffer = new byte[bufSize];
        try ( FileInputStream fis = new FileInputStream( path.toFile() );
              BufferedInputStream buf = new BufferedInputStream( fis );
              DigestInputStream dis = new DigestInputStream( buf, md ) ) {

            // Feed data into digest stream
            while ( dis.read( buffer, 0, bufSize ) >= 0 ) {
                // nothing
            }
            byte[] digest = dis.getMessageDigest().digest();
            return Hex.encodeHexString( digest );
        } catch ( final IOException ioE ) {

            LOG.error( "Failed to calculate digest", ioE );

            throw ioE;
        }


    }

    /**
     * Calculate hash value for file default.
     *
     * @param path path to file
     * @return hash
     * @throws IOException ioE error or path is not readable
     */
    public static String calcHashDefault( final Path path ) throws IOException {

        return calcHashDefault( path, DEFAULT_DIGEST );
    }

    /**
     * Calculate hash value for file default.
     *
     * @param path path to file
     * @param md   md
     * @return hash value
     * @throws IOException I/O error or path is not readable
     */
    public static String calcHashDefault( final Path path, MessageDigest md ) throws IOException {

        int buffer = bufferFor( Files.size( path ) );
        return calcHashDefault( path, md, buffer );
    }

    /**
     * Try to compute hash value of a file.
     *
     * @param path    path to file
     * @param md      message digest
     * @param bufSize buffer size [{@linkplain #MIN_BUF_SIZE} .. ]
     * @return hash value
     * @throws IOException              if {@code path} is not readable or i/o error
     * @throws IllegalArgumentException if {@code path} is a dir
     * @throws NullPointerException     if {@code path} is
     */
    public static String calcHashDefault( final Path path, MessageDigest md, int bufSize ) throws IOException {
        checkPath( path );

        bufSize = Math.max( MIN_BUF_SIZE, bufSize );
// default digest
        if ( null == md ) {

            md = DEFAULT_DIGEST;
        }

        LOG.info( "Calculate Hash using '{}' with buffer size {}", md, bufSize );
        // arm
        try ( FileInputStream fis = new FileInputStream( path.toFile() );
              BufferedInputStream buf = new BufferedInputStream( fis ) ) {
            int read;
            byte[] buffer = new byte[bufSize];
            while ( ( read = buf.read( buffer ) ) > 0 ) {
                md.update( buffer, 0, read );
            }

            byte[] hash = md.digest();
            return Hex.encodeHexString( hash );
        } catch ( IOException ioE ) {
            LOG.error( "Failed to calculate digest", ioE );

            throw ioE;
        }

    }

    /**
     * Try to compute hash value of a file via apache common codec.
     *
     * @param path path
     * @return hash value
     * @throws IOException              {@code path} is not readable
     * @throws NullPointerException     if {@code path} is null
     * @throws IllegalArgumentException if {@code path} is not a file
     */
    public static String calcHashCommonCodec( final Path path ) throws IOException {

        return calcHashCommonCodec( path, DEFAULT_DIGEST );
    }

    /**
     * Try to compute hash value of a file via apache common codec.
     *
     * @param path path to file . Should be no dir
     * @param md   message digest. If {@code null} we use {@link DigestUtils#getSha256Digest()}
     * @return hash value of file
     * @throws IOException              {@code path} is not readable
     * @throws NullPointerException     if {@code path} is null
     * @throws IllegalArgumentException if {@code path} is not a file
     */
    public static String calcHashCommonCodec( final Path path, MessageDigest md ) throws IOException {
        checkPath( path );

        // set default
        if ( null == md ) {

            md = DEFAULT_DIGEST;
        }


        byte[] hash = DigestUtils.digest( md, path.toFile() );
        return Hex.encodeHexString( hash );
    }


}