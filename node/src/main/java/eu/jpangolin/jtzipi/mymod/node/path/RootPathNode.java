package eu.jpangolin.jtzipi.mymod.node.path;

import eu.jpangolin.jtzipi.mymod.io.ModIO;
import eu.jpangolin.jtzipi.mymod.io.OS;
import eu.jpangolin.jtzipi.mymod.node.INode;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
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
 *     Each OS have one unique system root node.
 *     This class model this.
 * </p>
 */
public final class RootPathNode implements IPathNode{

    private static final RootPathNode SINGLETON = new RootPathNode();

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( RootPathNode.class );

    private final List<INode<Path>> subNodeL = new ArrayList<>();
    private String rootName;
    private Path rootPath;

    private RootPathNode() {

    }
    /**
     * Return the root node of user system.
     * @return root node
     */
    public static RootPathNode create() throws IOException {
        OS os = OS.getSystemOS();

        return of( os );
    }

    /**
     * Return root node for OS.
     * @param os os
     * @return root node for OS
     */
    public static RootPathNode of( OS os  ) throws IOException {

        Objects.requireNonNull( os );
        SINGLETON.init( os );

        return SINGLETON;
    }

    private void init( OS os ) throws IOException {

        switch ( os ) {
            case WINDOWS: initWin();break;
            case DOS: initDos();break;
            case MAC: initUnixmac( OS.MAC.getRootPathStr() ); break;
            case LINUX: initUnixmac( OS.LINUX.getRootPathStr() ); break;
            case SOLARIS: initUnixmac( OS.SOLARIS.getRootPathStr() ) ;break;
            case OTHER: initNa();break;
        }


    }

    private void initWin() throws IOException {

        LOG.info( "-- detected Windows" );
        // set computer name
        rootName = System.getenv( "COMPUTERNAME" );
        rootPath = Paths.get( "/" );


        final FileSystem fs = FileSystems.getDefault();

        if ( null == fs ) {
            throw new IOException( "No FileSystem!" );
        }
        final Iterable<Path> userRoot = fs.getRootDirectories();


        appendNodes( userRoot );




    }

    private void initUnixmac(String root) throws IOException {

        this.rootName = root;
        this.rootPath = Paths.get( root );
        appendNodes( ModIO.lookupDir( rootPath ) );

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
    public long getFileLength() {

        return ModIO.DIR_LENGTH;
    }

    @Override
    public FileTime getFileCreationTime() {

        return ModIO.FILE_TIME_NA;
    }
}
