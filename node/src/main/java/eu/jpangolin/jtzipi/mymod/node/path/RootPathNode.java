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

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Special root path node.
 * <p>
 * Each OS have one unique system root node.
 * This class model this.
 * </p>
 *
 * @author jTzipi
 */
public final class RootPathNode implements IPathNode {


    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( RootPathNode.class );

    private final List<INode<Path>> subNodeL = new ArrayList<>();
    private final OS os;
    private String rootName;
    private Path rootPath;

    private IPathNode linuxBlockDeviceRoot;
    private IOException creationError;

    public final class LinuxNoBlockDeviceParentNode implements IPathNode {



        private LinuxNoBlockDeviceParentNode() {

        }

        @Override
        public INode<Path> getParent() {
            return RootPathNode.this;
        }

        @Override
        public Path getValue() {
            return null;
        }

        @Override
        public List<INode<Path>> getSubNodes() {
            return getSubNodes(IPathNode.PREDICATE_ACCEPT_PATH_ALL);
        }

        @Override
        public List<INode<Path>> getSubNodes(Predicate<? super Path> predicate) {
            return Collections.emptyList();
        }

        @Override
        public int getDepth() {
            return 1;
        }

        @Override
        public String getName() {
            return "<No Disks>";
        }

        @Override
        public String getDesc() {
            return "No drives mounted";
        }

        @Override
        public String getType() {
            return "virtual disk drive node";
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
            return true;
        }

        @Override
        public long getFileLength() {
            return ModIO.PATH_DIR_LENGTH;
        }

        @Override
        public void requestReload() {

        }

        @Override
        public IOException getNodeCreationError() {
            return null;
        }

        @Override
        public FileTime getFileCreationTime() {
            return ModIO.FILE_TIME_NA;
        }

        @Override
        public int compareTo(IPathNode o) {
            return -1;
        }
    }

    public final class LinuxBlockDeviceParentNode extends RegularPathNode {

        private LinuxBlockDeviceParentNode( IPathNode parentNode, Path parentPath ) {
super(parentNode, parentPath);
init();
        }


        @Override
        public List<INode<Path>> getSubNodes(Predicate<? super Path> predicate) {

            if( !created ) {

                try {
                    subNodeL = Collections.unmodifiableList(Nodes.drives(RootPathNode.this, OS.LINUX));
                } catch (IOException ioE) {
                    subNodeL = Collections.emptyList();
                    creationError = ioE;
                } catch (InterruptedException iE) {
                    subNodeL = Collections.emptyList();
                    creationError = new IOException(iE);
                }

                created = true;
            }

            return subNodeL;
        }

    }

    private RootPathNode( OS os ) {
        this.os = os;
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
     * @throws NullPointerException if {@code os}
     */
    public static RootPathNode of( OS os ) {
        Objects.requireNonNull( os );
        RootPathNode rpt = new RootPathNode( os );
        rpt.init();
        return rpt;
    }

    private void init() {

        switch ( os ) {
            case WINDOWS -> initWin();
            case DOS -> initDos();
            case MAC -> initUnixmac( OS.MAC.getRootPathStr() );
            case LINUX -> initUnixmac( OS.LINUX.getRootPathStr() );
            case SOLARIS -> initUnixmac( OS.SOLARIS.getRootPathStr() );
            case OTHER -> initNa();
        }


    }

    private List<DrivePathNode> blockDevices(OS os) {

        try {
            return Nodes.drives(this, os);
        } catch (IOException ioE) {

            LOG.error("Failed to load block devices IO", ioE);
            creationError = ioE;
            return Collections.emptyList();
        } catch (InterruptedException iE) {

            creationError = new IOException(iE);
            LOG.error("Failed to load block devices IE", iE);
            return Collections.emptyList();
        }

    }

    private void initWin() {

        LOG.info( "-- detected Windows" );
        // set computer name
        rootName = System.getenv( "COMPUTERNAME" );
        rootPath = Paths.get( "C:" );
        List<DrivePathNode> drivePathNodeList = blockDevices(OS.WINDOWS);

        Path userHomePath = OS.getHomeDir();
        RegularPathNode homeDirNode = RegularPathNode.of(this, userHomePath );

        subNodeL.add(homeDirNode);
        subNodeL.addAll(drivePathNodeList);

    }

    private void initUnixmac( String root ) {
        LOG.info( "-- detected Linux/Mac/Unix" );
        this.rootName = "root";
        this.rootPath = Paths.get( root );

        final  RegularPathNode linuxRootNode = RegularPathNode.of(this, rootPath);

        Path userHomePath = OS.getHomeDir();
        RegularPathNode homeDirNode = RegularPathNode.of(this, userHomePath );


        subNodeL.add(linuxRootNode);
        subNodeL.add(homeDirNode);


        List<DrivePathNode> linuxDriveList = blockDevices(OS.LINUX);
        // we have 1 or more drives
        // look for mounted drives
        // and add their parent dir as regular
        if( !linuxDriveList.isEmpty() ) {

            // parent path of drives
            // or ???
            Optional<Path> driveParentOpt = linuxDriveList.stream()
                    .filter(IBlockDeviceNode::isMounted)
                    .map(IPathNode::getParent)
                    .map(INode::getValue)
                    .findAny();


            // if we found mounted
            // drives we add the parent path,
            // or we set the no device parent node
            if( driveParentOpt.isPresent() ) {
                linuxBlockDeviceRoot = new LinuxBlockDeviceParentNode(this, driveParentOpt.get());
            } else {
                linuxBlockDeviceRoot = new LinuxNoBlockDeviceParentNode();
            }


        } else {
            // no block devices found
            linuxBlockDeviceRoot = new LinuxNoBlockDeviceParentNode();
        }

        subNodeL.add(linuxBlockDeviceRoot);

    }

    private void initDos() {
        LOG.info( "-- detected Dos" );

        this.rootName = OS.DOS.getRootPathStr();
        this.rootPath = Paths.get( rootName );


        List<DrivePathNode> dosDriveList = blockDevices(OS.DOS);

        subNodeL.addAll( dosDriveList );

    }

    private void initNa() {

        this.rootName = ModIO.NA;
        this.rootPath = null;

    }

    /**
     * Return the linux parent root for block devices.
     * <p>
     *     This may return {@linkplain LinuxNoBlockDeviceParentNode} if no
     *     devices found and or no devices mounted.
     * </p>
     * @return block device parent node for linux system
     * @throws IllegalStateException if {@code os} != Linux
     */
    public IPathNode getLinuxBlockDeviceRoot() {
        if(!os.equals(OS.LINUX)){
            throw new IllegalStateException("No Linux System!");
        }
        return linuxBlockDeviceRoot;
    }

    /**
     * Return wrapped os.
     *
     * @return os
     */
    public OS getOs() {
        return os;
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

        return getSubNodes( IPathNode.PREDICATE_ACCEPT_PATH_ALL );
    }

    @Override
    public List<INode<Path>> getSubNodes( Predicate<? super Path> predicate ) {

        if ( null == predicate || predicate.equals( PREDICATE_ACCEPT_PATH_ALL ) ) {
            return Collections.unmodifiableList( subNodeL );
        }
        return subNodeL.stream()
                .filter( pathINode -> predicate.test( pathINode.getValue() ) )
                .sorted()
                .toList();
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
    public int hashCode() {

        return Objects.hash( os );
    }

    @Override
    public String toString() {

        return "RootPathNode{" +
                "subNodeL={" + subNodeL.stream()
                .map( INode::getValue )
                .map( Object::toString )
                .collect( Collectors.joining(",") )
                + "}}";
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

        return ModIO.PATH_DIR_LENGTH;
    }

    @Override
    public void requestReload() {

        this.subNodeL.clear();
        init();
    }

    @Override
    public IOException getNodeCreationError() {

        return creationError;
    }

    @Override
    public FileTime getFileCreationTime() {
        // TODO: other ?
        return FileTime.from( Instant.EPOCH );
    }
}