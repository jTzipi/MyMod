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

package eu.jpangolin.jtzipi.mymod.fx.service;

import eu.jpangolin.jtzipi.mymod.io.Checksums;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * Task to calculate a hash value of a file.
 * This class is immutable.
 *
 * @author jTzipi
 */
public class HashFileTask extends Task<String> {

    private final HashCalcType type;
    private final MessageDigest digest;
    private final Path path;
    private final int buffer;

    HashFileTask( final Path path, final MessageDigest messageDigest, final HashCalcType hashCalcType, int bufferSize ) {

        this.path = path;
        this.digest = messageDigest;
        this.type = hashCalcType;
        this.buffer = bufferSize;
    }

    /**
     * Return hash file task.
     *
     * @param path         path to file
     * @param md           digest
     * @param hashCalcType type of hash method
     * @param bufSize      buffer
     * @return task
     * @throws NullPointerException if {@code path}
     * @throws IOException          if {@code path} is not readable
     */
    public static HashFileTask of( Path path, MessageDigest md, HashCalcType hashCalcType, int bufSize ) throws IOException {

        Objects.requireNonNull( path, "Path is null" );
        if ( !Files.isReadable( path ) ) {
            throw new IOException( "File [=++] is not readable!" );
        }
        if ( null == md ) {
            md = Checksums.DEFAULT_DIGEST;
        }
        if ( null == hashCalcType ) {
            hashCalcType = HashCalcType.DEFAULT;
        }

        return new HashFileTask( path, md, hashCalcType, bufSize );
    }

    @Override
    protected String call() throws Exception {

        String hash;
        switch ( type ) {

            case STREAMING:
                hash = Checksums.calcHashStreaming( path, digest, buffer );
                break;
            case APACHE_COMMONS:
                hash = Checksums.calcHashCommonCodec( path, digest );
                break;
            case DEFAULT:
            default:
                hash = Checksums.calcHashDefault( path, digest, buffer );
        }

        return hash;
    }

    public enum HashCalcType {
        DEFAULT,
        APACHE_COMMONS,
        STREAMING,
        ;
    }
}