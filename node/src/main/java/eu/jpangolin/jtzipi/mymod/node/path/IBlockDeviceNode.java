package eu.jpangolin.jtzipi.mymod.node.path;

import eu.jpangolin.jtzipi.mymod.io.OS;

import java.util.function.Supplier;

/**
 * Special Node wrapping a block device.
 * <p>
 *     This is particular useful under linux/mac/solaris
 *     systems.
 *     <br/>
 *     Under windows we have not the ability to get most
 *     of the info we need.
 *      <br/>
 *      The block device nodes should be direct sub nodes
 *      of the virtual system root node.
 *      <br/>
 *      To create block device nodes use {@linkplain Nodes#drives(IPathNode, OS)}.
 * </p>
 * @author jTzipi
 */
public interface IBlockDeviceNode extends IPathNode {

    /**
     * Logical type of  block device.
     */
    enum LogicalType implements Supplier<String> {
        /**
         * A complete disk.
         */
        DISK("hard disk"),
        /**
         * A partition of a disk.
         */
        PART("partition"),
        /**
         * A read only block device.
         */
        ROM("Read Only Disk"),
        ;

        private final String name;
        LogicalType( String gadiStr ) {
this.name = gadiStr;
        }

        @Override
        public String get() {
            return name;
        }
    }
    /**
     * Physical Disk type.
     * this is known as
     * 'transport type' by 'lsblk'.
     */
    enum PhysicalDisk implements Supplier<String> {
        /**
         * Nonvolatile memory express.
         */
        NVME("nvme"),
        /**
         * USB.
         */
        USB("usb"),
        /**
         * Serial ATA.
         */
        SATA("sata"),
        /**
         * (Parallel) ATA.
         */
        ATA("ata"),
        /**
         * Unknown.
         */
        OTHER("<?>");

        private final String type;

        PhysicalDisk(String typeStr) {

            type = typeStr;
        }

        @Override
        public String get() {
            return type;
        }

        /**
         * Create the enum for the given type.
         * @param tranTypeStr transport type
         * @return the enum
         */
        public static PhysicalDisk of(String tranTypeStr) {

            for (PhysicalDisk pd : values()) {
                if (pd.get().equalsIgnoreCase(tranTypeStr)) {
                    return pd;
                }
            }

            return PhysicalDisk.OTHER;
        }
    }

    /**
     * Is this device mounted.
     * @return {@code true} if the device is mounted
     */
    boolean isMounted();

    /**
     * Logical type of device.
     * @return logical device type
     */
    LogicalType getLogicalType();

    /**
     * Physical device type.
     * @return physical type
     */
    PhysicalDisk getPhysicalDisk();

    /**
     * Return space in bytes of this block device.
     * @return space in bytes
     */
    long getSpace();

    /**
     * Return used space in bytes of this block device.
     * @return used space in bytes
     */
    long getUsedSpace();

    /**
     * Return available space in bytes if this block device.
     * @return available space in bytes
     */
    long getAvailableSpace();

    /**
     * Return file system type of partition or rom.
     * @return type of file system like 'ntsf' or 'btrfs'
     */
    String getFileSystemType();
}
