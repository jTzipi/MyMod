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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * FX Task to search for path.
 *
 * @author jTzipi
 */
public class SearchPathTask extends Task<List<Path>> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( "TaskPathSearch" );

    // The root dir to start the search
    private final Path dir;
    // The predicate to filter
    private final Predicate<? super Path> pp;
    // Found path
    private final List<Path> foundPathL = new ArrayList<>();
    // Dirs/Files not readable
    private final List<Path> pathNotReadableL = new ArrayList<>();
    // dirs scanned
    private long dirc = 0L;


    private SearchPathTask( final Path dirPath, final Predicate<? super Path> predicate ) {

        this.dir = dirPath;
        this.pp = predicate;
    }

    /**
     * Create a new search path task.
     *
     * @param rootPath root path
     * @param pathPred predicate
     * @return Task Search
     * @throws NullPointerException     if {@code rootPath} is null
     * @throws IllegalArgumentException if {@code rootPath} is not a dir
     */
    public static SearchPathTask of( final Path rootPath, Predicate<? super Path> pathPred ) {

        Objects.requireNonNull( rootPath );
        if ( !Files.isDirectory( rootPath ) ) {
            throw new IllegalArgumentException( "rootPath[='" + rootPath + "'] is not a dir" );
        }

        if ( null == pathPred ) {
            pathPred = ModIO.ACCEPT_ANY_PATH;
        }
        return new SearchPathTask( rootPath, pathPred );
    }

    @Override
    protected List<Path> call() {


        // if root is not readable or is no dir we
        // return an empty list
        if ( !Files.isReadable( dir ) ) {

            LOG.info( "Warn dir[='" + dir + "'] is not readable" );
            pathNotReadableL.add( dir );
            updateProgress( 1L, 1L );
            return Collections.emptyList();
        }

        LOG.debug( "Start FX Search for dir[='" + dir + "']" );

        List<Path> dirL = ModIO.getSubDirsOf( dir );
        dirc = dirL.size();
        LOG.debug( "Try to test " + dirc + " dirs" );

        // start search recursive with root dir
        search( dir, 1L );
        LOG.debug( "Search done. Found " + foundPathL.size() + " path" );
        return foundPathL;
    }

    /**
     * Return num of dirs we saw.
     *
     * @return dirs traversed
     */
    public long getDirsTested() {

        return dirc;
    }

    /**
     * Return dirs and files which seem to be not readable.
     *
     * @return list of not readable path
     */
    public List<Path> getPathNotReadable() {

        return pathNotReadableL;
    }

    /**
     * Search for.
     * <p>
     * We check whether the path is readable.
     * If no we add to the list of
     * Otherwise we test the predicate and if pass we add the path to the found list.
     * If the path is a dir we search the sub paths.
     * </p>
     *
     * @param searchPath path to test
     */
    private void search( final Path searchPath, long dir ) {

        // We enter a dir and update progress
        updateProgress( dir, dirc );
        // either a non-readable dir or non-readable file end this
        if ( !Files.isReadable( searchPath ) ) {

            pathNotReadableL.add( searchPath );

            return;
        }
        // match
        // update the UI.
        if ( pp.test( searchPath ) ) {
            foundPathL.add( searchPath );
            updateValue( foundPathL );

        }
        // recursive search sub dir
        if ( Files.isDirectory( searchPath ) ) {
            dir++;
            try {
                for ( Path path : ModIO.lookupDir( searchPath ) ) {

                    search( path, dir );

                }
            } catch ( IOException ioE ) {

                LOG.info( "Failed to read dir[='" + searchPath + "']", ioE );
                pathNotReadableL.add( searchPath );
            }
        }

    }
}