package eu.jpangolin.jtzipi.mymod.io.cmd;

import eu.jpangolin.jtzipi.mymod.io.OS;
import eu.jpangolin.jtzipi.mymod.utils.IBuilder;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;

/**
 * Some helper tools using linux native command.
 *
 * @author jTzipi
 */
final class Commands {

    private static final org.slf4j.Logger LOG  = LoggerFactory.getLogger( "" );

    private Commands() {

    }

    /**
     * Simple reader for a OS process line by line.
     */
    private static final class StreamGobbler implements Callable<String> {


        private final InputStream is;

        private StreamGobbler( final InputStream inputStream ) {
            this.is = inputStream;
        }


        @Override
        public String call()  {

    StringBuilder sb = new StringBuilder();
            try( Scanner scanner = new Scanner( is ) ) {

                while(scanner.hasNextLine()){

                    String line = scanner.nextLine();
                    LOG.info( "Scanne > {}", line );
                    sb.append( line ).append( "\n" );
                }
            }


            return sb.toString();
        }
    }


    public record ProcWrapper( Process p, Future<String> resultFuture ) {

    }
    static ProcWrapper cmd( String... args ) throws IOException {


        LOG.info( "Try to make the following cmd '{}'" , Arrays.toString(args));

        Process p = new ProcessBuilder()
                .command(args)
                .start();

        Future<String> f = Executors.newSingleThreadExecutor().submit( new StreamGobbler( p.getInputStream() ) );

        return new ProcWrapper( p, f );
    }



}
