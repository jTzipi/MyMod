package eu.jpangolin.jtzipi.mymod.fx.service;

import eu.jpangolin.jtzipi.mymod.io.Checksums;
import javafx.concurrent.Task;

import java.nio.file.Path;
import java.security.MessageDigest;

public class HashFileTask extends Task<String> {

    private MessageDigest digest;

    private Path path;

    HashFileTask() {

    }

    @Override
    protected String call() throws Exception {

return Checksums.calcHashDefault( path,  digest );
    }
}