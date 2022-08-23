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

public class RegularPathNode implements IPathNode {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( RegularPathNode.class);

    private static final Comparator<? super IPathNode> DEF_COMP = Comparator.comparing( IPathNode::isDir )
            .thenComparing( (p1, p2) -> Collator.getInstance().compare( p1.getName(), p2.getName() ) )
            .thenComparing( IPathNode::isReadable )
            .reversed();

    // -- INode prop --
    private final Path path;
    private final IPathNode parentNode;
    private List<INode<Path>> subNodeL;
    private int depth;
    // -- IPathNode prop
    private String name;
    private String desc;
    private String type;
    private boolean dir;
    private boolean symLink;
    private boolean hidden;
    private boolean readable;
    private boolean created;
    private long fileLen;
    private FileTime ftCreate;


    // -- RegularPathNode prop
    private BiFunction<Path, Predicate<? super Path>, List<Path>> createSubNodeF;
    private Comparator<? super IPathNode> comp;
    private String creationError;


    private RegularPathNode( final IPathNode parentNode, Path value ) {

        this.parentNode = parentNode;
        this.path = value;
    }

    /**
     * Return a regular path node.
     * @param parentNode parent node or null if root
     * @param path path
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
                .collect( Collectors.toList());
    }

    private void init(  ) {

        this.name = PathInfo.fileSystemName( path );
        this.desc = PathInfo.fileSystemTypeDesc( path );
        this.dir = PathInfo.isDir(path);
        this.type = ModIO.probePathTypeSafe( path );
        this.readable = PathInfo.isReadable( path );
        this.symLink = PathInfo.isLink( path );
        this.hidden = PathInfo.isHidden( path );
        this.depth = path.getNameCount();
        this.fileLen =  PathInfo.getLength( path );
        this.ftCreate = ModIO.readFileCreationTimeSafe( path );
    }

    /**
     * Set node creator.
     * @param func function to create sub node
     */
    public void setSubNodeCreator( final BiFunction<Path, Predicate<? super Path>, List<Path>> func ) {

        this.createSubNodeF = func;
    }

    /**
     * Set a custom comparator.
     * TODO good idea?
     * @param comparator custom comparator
     */
    public void setComparator( final Comparator<? super IPathNode> comparator ) {

        this.comp = comparator;
    }

    /**
     * Return error information if creation of sub nodes fail.
     * @return error info or null if creation succeeded
     */
    public String getCreationError() {

        return creationError;
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

        return getSubNodes(PREDICATE_ACCEPT_PATH_ALL);
    }

    @Override
    public List<INode<Path>> getSubNodes( Predicate<? super Path> predicate ) {
// we want only dir to have sub nodes
        if( !isDir() ) {
            LOG.info( "Try to read sub nodes of non dir file[='"+path+"']" );
            return Collections.emptyList();
        }

        // Dir not readable -> return empty list
        if( isDir() && !isReadable() ) {
            LOG.info( "This[=" + path + "'] dir is not readable!" );
            return Collections.emptyList();
        }
        // create them
        // if there is I/O error save
        if( !isNodeSubListCreated() ) {
            LOG.debug( "-- sub nodes not created : start creating now" );
            try {
                this.subNodeL = createSub( path, predicate, this, createSubNodeF );
                this.creationError = null;
                LOG.debug( "-- sub nodes created" );
            } catch ( final IOException ioE ) {

                this.subNodeL = Collections.emptyList();
                this.creationError = ioE.getLocalizedMessage();
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

        return (!isDir() && !isLink()) || !isReadable();
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
    public boolean equals( final Object object ) {

        if ( this == object ) {
            return true;
        }
        if ( !( object instanceof IPathNode ) ) {
            return false;
        }

        final IPathNode other = ( IPathNode ) object;
        final Path thisPath = getValue();
        final Path otherPath = other.getValue();


        return thisPath.normalize().equals( otherPath.normalize() );
    }
}
