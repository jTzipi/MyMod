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

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * Checksum Utils.
 *
 * @author jTzipi
 */
public final class Checksums {

    private Checksums() {

    }

    /**
     * Try to compute hash value of a file.
     *
     * @param path path to file . Should be no dir
     * @param md   message digest. If {@code null} we use {@link DigestUtils#getSha256Digest()}
     * @return hash value of file
     * @throws IOException {@code path} is not readable
     * @throws NullPointerException     if {@code path} is null
     * @throws IllegalArgumentException if {@code path} is not a file
     */
    public static String calcHashCommonCodec( final Path path, MessageDigest md ) throws IOException {

        Objects.requireNonNull( path );
        if ( null == md ) {

            md = DigestUtils.getSha256Digest();
        }
        // File readable
        if ( !Files.isReadable( path ) ) {
            throw new IOException( "File '" + path + "' is not readable" );
        }
        // make not sense
        if ( Files.isDirectory( path ) ) {
            throw new IllegalArgumentException( "Path '" + path + "' seem to be a dir" );
        }

        byte[] hash = DigestUtils.digest( md, path.toFile() );
        return Hex.encodeHexString( hash );
    }
}
