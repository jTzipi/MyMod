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