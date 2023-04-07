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
import eu.jpangolin.jtzipi.mymod.io.PathInfo;
import eu.jpangolin.jtzipi.mymod.node.INode;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Regular Path Node.
 * <p>
 * This is a wrapper for a 'regular' path.
 * in the file system.
 * <br/>
 *
 * </p>
 *
 * @author jTzipi
 */
public class RegularPathNode implements IPathNode {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( RegularPathNode.class );

    // -- INode prop --
    private final Path path;
    private final IPathNode parentNode;
     List<INode<Path>> subNodeL;
    private int depth;
    // -- IPathNode prop
    private String name;
    private String desc;
    private String type;
    private boolean dir;
    private boolean symLink;
    private boolean hidden;
    private boolean readable;
     boolean created;
    private long fileLen;
    private FileTime ftCreate;


    // -- RegularPathNode prop
    BiFunction<Path, Predicate<? super Path>, List<Path>> createSubNodeF;
    Comparator<? super IPathNode> comp;
    IOException creationError;


     RegularPathNode( final IPathNode parentNode, Path value ) {

        this.parentNode = parentNode;
        this.path = value;
    }

    /**
     * Return a regular path node.
     *
     * @param parentNode parent node or null if root
     * @param path       path
     * @return RegularPathNode
     * @throws NullPointerException if {@code path} is null
     */
    public static RegularPathNode of( IPathNode parentNode, Path path ) {

        Objects.requireNonNull( path );
        RegularPathNode rpn = new RegularPathNode( parentNode, path );

        rpn.init();
        return rpn;
    }

    private static List<INode<Path>> createSub( final Path path, final Predicate<? super Path> pp, final RegularPathNode parent, BiFunction<Path, Predicate<? super Path>, List<Path>> func ) throws IOException {

        List<Path> subPathL = null == func
                ? ModIO.lookupDir( path, pp, false )
                : func.apply( path, pp );

        return subPathL.stream()
                .sorted()
                .map( p -> RegularPathNode.of( parent, p ) )

                .collect( Collectors.toList() );
    }

     void init() {

        this.name = PathInfo.fileSystemName( path );
        this.desc = PathInfo.fileSystemTypeDesc( path );
        this.dir = PathInfo.isDir( path );
        this.type = ModIO.probePathTypeSafe( path );
        this.readable = PathInfo.isReadable( path );
        this.symLink = PathInfo.isLink( path );
        this.hidden = PathInfo.isHidden( path );
        this.depth = path.getNameCount();
        this.fileLen = PathInfo.getLength( path );
        this.ftCreate = ModIO.readFileCreationTimeSafe( path ).orElse( null );
    }

    /**
     * Set node creator.
     *
     * @param func function to create sub node
     */
    public void setSubNodeCreator( final BiFunction<Path, Predicate<? super Path>, List<Path>> func ) {

        this.createSubNodeF = func;
    }

    /**
     * Set a custom comparator.
     * TODO good idea?
     *
     * @param comparator custom comparator
     */
    public void setComparator( final Comparator<? super IPathNode> comparator ) {

        this.comp = comparator;
    }

    @Override
    public INode<Path> getParent() {

        return parentNode;
    }

    @Override
    public Path getValue() {

        return path;
    }

    @Override
    public List<INode<Path>> getSubNodes() {

        return getSubNodes( PREDICATE_ACCEPT_PATH_ALL );
    }

    @Override
    public List<INode<Path>> getSubNodes( Predicate<? super Path> predicate ) {
// we want only dir to have sub nodes
        if ( !isDir() ) {
            LOG.info( "Try to read sub nodes of non dir file[='" + path + "']" );
            return Collections.emptyList();
        }

        // Dir not readable -> return empty list
        if ( isDir() && !isReadable() ) {
            LOG.info( "This[=" + path + "'] dir is not readable!" );
            return Collections.emptyList();
        }
        // create them
        // if there is I/O error save
        if ( !isNodeSubListCreated() ) {
            LOG.debug( "-- sub nodes not created : start creating now" );
            try {
                this.subNodeL = createSub( path, predicate, this, createSubNodeF );
                this.creationError = null;
                LOG.debug( "-- sub nodes created" );
            } catch ( final IOException ioE ) {

                this.subNodeL = Collections.emptyList();
                this.creationError = ioE;
                LOG.info( "Error creating sub nodes", ioE );
            }

            this.created = true;
        }


        return Collections.unmodifiableList( subNodeL );
    }

    @Override
    public int getDepth() {

        return depth;
    }

    @Override
    public boolean isLeaf() {

        return ( !isDir() && !isLink() ) || !isReadable();
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public String getDesc() {

        return desc;
    }

    @Override
    public String getType() {

        return type;
    }

    @Override
    public boolean isNodeSubListCreated() {

        return created;
    }

    @Override
    public boolean isLink() {

        return symLink;
    }

    @Override
    public boolean isDir() {

        return dir;
    }

    @Override
    public boolean isReadable() {

        return readable;
    }

    @Override
    public boolean isHidden() {

        return hidden;
    }

    @Override
    public long getFileLength() {

        return fileLen;
    }

    @Override
    public void requestReload() {

        init();
        if ( isDir() ) {
            this.created = false;
        }

    }

    @Override
    public IOException getNodeCreationError() {

        return creationError;
    }

    @Override
    public FileTime getFileCreationTime() {

        return ftCreate;
    }

    @Override
    public int compareTo( IPathNode o ) {

        return null == comp
                ? DEF_COMP.compare( this, o )
                : comp.compare( this, o );
    }

    @Override
    public int hashCode() {

        return Objects.hash( path );
    }

    @Override
    public boolean equals( final Object object ) {

        if ( this == object ) {
            return true;
        }
        if ( !( object instanceof final IPathNode other ) ) {
            return false;
        }

        final Path thisPath = getValue();
        final Path otherPath = other.getValue();


        return thisPath.normalize().equals( otherPath.normalize() );
    }

    @Override
    public String toString() {

        return "RegularPathNode{" +
                "path='" + path +
                "', parentNode='" + (null == parentNode ? "<null>" : parentNode.getName()) +
                "', depth='" + depth +
                "', name='" + name +
                "', desc='" + desc +
                "', type='" + type +
                "', dir='" + dir +
                "', symLink='" + symLink +
                "', hidden='" + hidden +
                "', readable='" + readable +
                "', sub created='" + created +
                "', fileLen='" + fileLen +
                "', ftCreate='" + ftCreate +
                "', creationError=" + creationError +
                '}';
    }
}