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

import eu.jpangolin.jtzipi.mymod.io.ModIO;
import javafx.concurrent.Task;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * Try to look for files in dirs using a 'balanced' .
 * <p>
 * We first search for all directories and put them
 * in several lists of threads. So that each thread can
 * consume his list of dirs. Searching for files. And if the
 * list is empty maybe steal from an others threads list.
 *
 *
 * </p>
 *
 * @author jTzipi
 */
public final class SearchPathTaskParallel extends Task<Set<Path>> {


    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( SearchPathTaskParallel.class );
    private static final int N_CPUS = Runtime.getRuntime().availableProcessors();


    private final Path root;
    private final Predicate<? super Path> pp;
    private final ExecutorService executorService;
    private final AtomicLong dirCnt = new AtomicLong();
    private final int threads = N_CPUS + 1;
    private final List<DirSearcher> dirSearcherList = new ArrayList<>( threads );
    private final Set<Path> dirS = new TreeSet<>();

    private SearchPathTaskParallel( final Path root, final Predicate<? super Path> predicate, final ExecutorService pathExec ) {

        this.root = root;
        this.pp = predicate;
        this.executorService = pathExec;
    }

    /**
     * Construct a new path searcher.
     *
     * @param rootPathDir     root dir
     * @param pp              path predicate
     * @param executorService executor service
     * @return task to search for file parallel
     */
    public static SearchPathTaskParallel of( final Path rootPathDir, Predicate<? super Path> pp, ExecutorService executorService ) {

        Objects.requireNonNull( rootPathDir );

        if ( null == pp ) {
            pp = ModIO.ACCEPT_ANY_PATH;
        }
        if ( null == executorService ) {
            executorService = Executors.newCachedThreadPool();
        }

        return new SearchPathTaskParallel( rootPathDir, pp, executorService );
    }

    private void init() {

        List<Path> dirL = ModIO.getSubDirsOf( root, LinkOption.NOFOLLOW_LINKS );
        LOG.info( "Searching " + dirL.size() + " dir" );
        dirS.addAll( dirL );


        // per thread init dir searcher
        for ( int i = 0; i < threads; i++ ) {
            DirSearcher ds = new DirSearcher( pp );
            dirSearcherList.add( ds );
        }

        // put each dir searcher balanced the dirs
        for ( int i = 0; i < dirL.size(); i++ ) {

            int dsi = i % threads;

            dirSearcherList.get( dsi ).addDir( dirL.get( i ) );

        }


    }

    @Override
    protected Set<Path> call() throws InterruptedException, ExecutionException {

        init();
        Set<Path> foundS = new TreeSet<>();
        LOG.info( "Start of " + threads + " dir searcher..." );
        List<Future<List<Path>>> resultL = executorService.invokeAll( dirSearcherList );

        for ( Future<List<Path>> f : resultL ) {

            foundS.addAll( f.get() );
        }


        return foundS;
    }

    private final class DirSearcher implements Callable<List<Path>> {

        private final org.slf4j.Logger LOG = LoggerFactory.getLogger( "DirSearcher" );
        private final List<Path> dirL = new ArrayList<>();
        private final Predicate<? super Path> pp;

        private DirSearcher( Predicate<? super Path> predicate ) {

            this.pp = predicate;
        }

        private void addDir( final Path dir ) {

            dirL.add( dir );
        }

        @Override
        public List<Path> call() {

            List<Path> foundPathL = new ArrayList<>();
            for ( Path dir : dirL ) {


                updateTitle( "Searching dir '" + dir.toString() + "'..." );
                updateProgress( dirCnt.incrementAndGet(), dirS.size() );

                try ( DirectoryStream<Path> ds = Files.newDirectoryStream( dir, pp::test ) ) {

                    for ( Path p : ds ) {

                        foundPathL.add( p );
                        updateValue( Collections.singleton( p ) );
                    }

                } catch ( final IOException ioE ) {

                    // should not be because we check dir to be readable when we search for them
                    LOG.warn( "Failed to read dir[='" + dir + "']", ioE );
                }

            }
            return foundPathL;
        }
    }
}