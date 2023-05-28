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

import eu.jpangolin.jtzipi.mymod.utils.ModUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

class ChecksumsTest {


    private static final Logger LG = LoggerFactory.getLogger( "ChecksumTest" );
    private static final String PATH_DIGEST = "c37fadce08e49988721f2fa8cba2f3c0e2b695a2801e4cd1c8cd41e5bf9da25b936fcd351c87c3adfdf316e6b8fecc5dee9f3514b15a559fdbe653842494b947";

    static {
        ModUtils.registerBouncyCastleProvider();
    }

    private Path path;
    private MessageDigest medi;

    @BeforeEach
    void setUp() {


        path = OS.getHomeDir().resolve( "Gadi/kenya.jpg" );
        try {
            medi = MessageDigest.getInstance( "SHA512", "BC" );
        } catch ( NoSuchAlgorithmException | NoSuchProviderException e ) {
            throw new RuntimeException( e );
        }
    }

    @Test
    void calcHashStreaming() {

        try {
            String hash = Checksums.calcHashStreaming( path, medi, Checksums.MIN_BUF_SIZE );

            LG.info( "hash stream = '{}", hash );
            Assertions.assertEquals( PATH_DIGEST, hash );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    @Test
    void calcHashDefault() {

        try {
            String hash = Checksums.calcHashDefault( path, medi, Checksums.MIN_BUF_SIZE );
            Assertions.assertEquals( PATH_DIGEST, hash );
            LG.info( "hash calc default = {}", hash );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }


    @Test
    void calcHashCommonCodec() {


        String hash = null;
        try {
            hash = Checksums.calcHashCommonCodec( path, medi );
            Assertions.assertEquals( PATH_DIGEST, hash );
            LoggerFactory.getLogger( "ChecksumTest" ).info( "CommonCodecHash={}", hash );

        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }

    }


}