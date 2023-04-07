package eu.jpangolin.jtzipi.mymod.node.path;

import eu.jpangolin.jtzipi.mymod.io.ModIO;
import eu.jpangolin.jtzipi.mymod.io.PathInfo;
import eu.jpangolin.jtzipi.mymod.node.INode;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is a path node wrapping a <i>drive</i> , that is a hard disk or
 * USB or any other block device.
 * <p>
 *     On Linux we need to create them via the 'lsblk' command.
 *     <br/>
 *     On Windows those are the top level nodes we can obtain via <code>{@code FileSystems.getDefault().getRootDirectories()}</code>
 * </p>
 * @author jTzipi
 */
public final class DrivePathNode implements IBlockDeviceNode {


    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger("DrivePathNode");

    private static final Comparator<IBlockDeviceNode> COMP = Comparator.comparing(IBlockDeviceNode::isMounted).thenComparing(IPathNode::getName);

    // -- INode Prop
    private final IPathNode parent;
    private List<INode<Path>> subNodeL;
    private final Path path;
    // -- IPathNode prop
    private final String name;

    private final String type;
    private final int depth;
    private final long fs;
    private boolean created;
    private final boolean hidden;
    private final boolean readable;
    private IOException creationError;


    // -- IBlockDeviceNode Prop
    private final boolean mounted;
    private final long sizeAvail;
    private final String fsType;

    private final PhysicalDisk pd;
    private final LogicalType ld;

    DrivePathNode(final IPathNode parentPathNode,
                  final Path drivePath,
                  final String nameStr,
                  final String labelStr,
                  final LogicalType logicalType,
                  final PhysicalDisk physicalDisk,
                  final String fsFormatTypeStr,
                  final long fsByte,
                  final long fsAvailableByte,
                  final boolean mounted) {
        this.parent = parentPathNode;
        this.path = drivePath;
        this.name = nameStr + "(" + labelStr + ")";
        this.type = logicalType.get() + ":"+ physicalDisk.get() + ":" + fsFormatTypeStr;
        this.fs = fsByte;
        this.depth = drivePath.getNameCount();
        this.readable = mounted && PathInfo.isReadable(drivePath);
        this.hidden = PathInfo.isHidden(drivePath);

        this.mounted = mounted;
        this.sizeAvail = fsAvailableByte;
        this.fsType = fsFormatTypeStr;
        this.ld = logicalType;
        this.pd = physicalDisk;
    }


    @Override
    public INode<Path> getParent() {
        return parent;
    }

    @Override
    public Path getValue() {
        return path;
    }

    @Override
    public List<INode<Path>> getSubNodes() {
        return getSubNodes(IPathNode.PREDICATE_ACCEPT_PATH_ALL);
    }

    @Override
    public List<INode<Path>> getSubNodes(Predicate<? super Path> predicate) {

        if ( !isNodeSubListCreated() ) {
            LOG.debug( "-- disk drive sub nodes not created : start creating now" );
            try {
                this.subNodeL = ModIO.lookupDir(path, predicate)
                        .stream()
                        .map( path -> RegularPathNode.of(this, path))
                        .collect(Collectors.toList());
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
        return !isMounted();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDesc(){
        return "Device["+ getType() +"]"
               + (isMounted()
                ? "mounted:" + getValue()
                : "not mounted");

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
        return false;
    }

    @Override
    public boolean isDir() {
        return true;
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
        return fs;
    }

    @Override
    public void requestReload() {

    }

    @Override
    public IOException getNodeCreationError() {
        return creationError;
    }

    @Override
    public FileTime getFileCreationTime() {
        return ModIO.FILE_TIME_NA;
    }

    @Override
    public int compareTo(IPathNode o) {

        if( o instanceof IBlockDeviceNode ) {
            return COMP.compare(this, (IBlockDeviceNode) o);
        } else {
            return IPathNode.DEF_COMP.compare(this, o);
        }


    }


    @Override
    public boolean isMounted() {
        return mounted;
    }

    @Override
    public LogicalType getLogicalType() {
        return ld;
    }

    @Override
    public PhysicalDisk getPhysicalDisk() {
        return pd;
    }


    @Override
    public long getSpace() {
        return fs;
    }

    @Override
    public long getUsedSpace() {
        return getSpace() - getAvailableSpace();
    }

    @Override
    public long getAvailableSpace() {
        return sizeAvail;
    }

    @Override
    public String getFileSystemType() {
        return fsType;
    }
}
