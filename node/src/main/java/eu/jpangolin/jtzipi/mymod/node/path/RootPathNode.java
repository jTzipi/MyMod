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

package eu.jpangolin.jtzipi.mymod.node.path;

import eu.jpangolin.jtzipi.mymod.io.ModIO;
import eu.jpangolin.jtzipi.mymod.io.OS;
import eu.jpangolin.jtzipi.mymod.node.INode;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Special root path node.
 * <p>
 * Each OS have one unique system root node.
 * This class model this.
 * </p>
 *
 * @author jTzipi
 */
public final class RootPathNode implements IPathNode{

    private static final RootPathNode SINGLETON = new RootPathNode();

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( RootPathNode.class );

    private final List<INode<Path>> subNodeL = new ArrayList<>();
    private String rootName;
    private Path rootPath;
    private IOException creationError;

    private RootPathNode() {

    }

    /**
     * Return the root node of user system.
     *
     * @return root node
     */
    public static RootPathNode create() {

        OS os = OS.getSystemOS();

        return of( os );
    }

    /**
     * Return root node for OS.
     *
     * @param os os
     * @return root node for OS
     */
    public static RootPathNode of( OS os ) {

        Objects.requireNonNull( os );
        SINGLETON.init( os );

        return SINGLETON;
    }

    private void init( OS os ) {

        switch ( os ) {
            case WINDOWS:
                initWin();
                break;
            case DOS:
                initDos();
                break;
            case MAC:
                initUnixmac( OS.MAC.getRootPathStr() );
                break;
            case LINUX:
                initUnixmac( OS.LINUX.getRootPathStr() );
                break;
            case SOLARIS:
                initUnixmac( OS.SOLARIS.getRootPathStr() );
                break;
            case OTHER:
                initNa();
                break;
        }


    }

    private void initWin() {

        LOG.info( "-- detected Windows" );
        // set computer name
        rootName = System.getenv( "COMPUTERNAME" );
        rootPath = Paths.get( "/" );


        final FileSystem fs = FileSystems.getDefault();
        final Iterable<Path> userRoot;
        if ( null == fs ) {
            this.creationError = new IOException( "No FileSystem!" );
            userRoot = Collections.emptyList();
        } else {
            userRoot = fs.getRootDirectories();
        }

        appendNodes( userRoot );


    }

    private void initUnixmac( String root ) {
        LOG.info( "-- detected Linux/Mac/Unix" );
        this.rootName = root;
        this.rootPath = Paths.get( root );

        Iterable<Path> rootIt;

        try {
            rootIt = ModIO.lookupDir( rootPath );
        } catch ( final IOException ioE ) {
            rootIt = Collections.emptyList();
            this.creationError = ioE;
        }
        appendNodes( rootIt );

    }

    private void initDos() {


        this.rootName = OS.DOS.getRootPathStr();
        this.rootPath = Paths.get( rootName );

        File[] roots = File.listRoots();

        appendNodes( Stream.of(roots).map( File::toPath ).collect( Collectors.toList()) );

    }

    private void initNa() {

        this.rootName = ModIO.NA;
        this.rootPath = null;

    }

    private void appendNodes( Iterable<Path> path ) {

        for( Path rootSubPath : path ) {
            subNodeL.add( RegularPathNode.of( this, rootSubPath ) );
        }
// set home dir of user
        subNodeL.add( RegularPathNode.of( this, OS.getHomeDir() ) );
    }

    @Override
    public INode<Path> getParent() {

        return null;
    }

    @Override
    public Path getValue() {

        return rootPath;
    }

    @Override
    public List<INode<Path>> getSubNodes() {

        return getSubNodes(IPathNode.PREDICATE_ACCEPT_PATH_ALL);
    }

    @Override
    public List<INode<Path>> getSubNodes( Predicate<? super Path> predicate ) {

        if( null == predicate || predicate.equals( PREDICATE_ACCEPT_PATH_ALL ) ) {
            return Collections.unmodifiableList( subNodeL );
        }
        return subNodeL.stream()
                .filter( pathINode -> predicate.test( pathINode.getValue() ) )
                .sorted()
                .collect( Collectors.toUnmodifiableList() );
    }

    @Override
    public int getDepth() {

        return 0;
    }

    @Override
    public boolean isLeaf() {

        return false;
    }

    @Override
    public int compareTo( IPathNode o ) {

        return 17;
    }

    @Override
    public String getName() {

        return rootName;
    }

    @Override
    public String getDesc() {

        return "Computer Root";
    }

    @Override
    public String getType() {

        return "File System Root";
    }

    @Override
    public boolean isNodeSubListCreated() {

        return true;
    }

    @Override
    public boolean isLink() {

        return false;
    }

    @Override
    public boolean isDir() {

        return true;
    }

    @Override
    public boolean isReadable() {

        return true;
    }

    @Override
    public boolean isHidden() {

        return false;
    }

    @Override
    public String toString() {

        return "RootPathNode{" +
                "subNodeL={" + subNodeL
                + "}, readable='" + isReadable()
                + "', hidden='" + isHidden()
                + "', depth='" + getDepth()
                +'}';
    }

    @Override
    public long getFileLength() {

        return ModIO.PATH_DIR_LENGTH;
    }

    @Override
    public void requestReload() {

        this.subNodeL.clear();
        init( OS.getSystemOS() );
    }

    @Override
    public IOException getNodeCreationError() {

        return creationError;
    }

    @Override
    public FileTime getFileCreationTime() {

        return ModIO.FILE_TIME_NA;
    }
}