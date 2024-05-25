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

import eu.jpangolin.jtzipi.mymod.utils.ModUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

class ChecksumsTest {


    private static final Logger LG = LoggerFactory.getLogger( "ChecksumTest" );
    private static final String PATH_DIGEST = "524e3fa9810e3ddbc7463f91efdc3a7da2ea30684c0c6b71222d41eeced897198efbb21c2f259da1c0f4b69bd7fd13271e2a0475981bfaf731097fc459eeea20";


    static {
        ModUtils.registerBouncyCastleProvider();
    }

    private Path path;
    private MessageDigest medi;

    @BeforeEach
    void setUp() {


        URL resUrl = ChecksumsTest.class.getResource("ImgSheep.jpg");
        if(null == resUrl) {
            throw new RuntimeException( new FileNotFoundException("path to image 'ImgSheep.jpg' not found"));
        }



        path = Paths.get(resUrl.getPath());

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