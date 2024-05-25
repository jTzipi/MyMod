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

package eu.jpangolin.jtzipi.mymod.node.path;

import eu.jpangolin.jtzipi.mymod.io.ModIO;
import eu.jpangolin.jtzipi.mymod.io.OS;
import eu.jpangolin.jtzipi.mymod.io.PathInfo;
import eu.jpangolin.jtzipi.mymod.io.cmd.CommandResult;
import eu.jpangolin.jtzipi.mymod.io.cmd.LinuxCmds;
import eu.jpangolin.jtzipi.mymod.io.cmd.linux.LsblkCmd;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Class to create specific types of Nodes.
 *
 * @author jTzipi
 */
public final class Nodes {

    private static final org.slf4j.Logger LOG  = LoggerFactory.getLogger("Nodes");

    private Nodes() {

    }


    /**
     * Try to create list of block devices.
     *
     * @param systemRoot system parent virtual root
     * @param os System
     * @return list of block devices
     * @throws IOException IO
     * @throws InterruptedException IE
     * @throws NullPointerException if {@code systemRoot}|{@code os}
     */
    public static List<DrivePathNode> drives(IPathNode systemRoot, OS os) throws IOException, InterruptedException {
        Objects.requireNonNull(systemRoot);
        Objects.requireNonNull(os);
        return switch (os) {
            case SOLARIS, LINUX -> linuxDrives(systemRoot);
            case DOS, WINDOWS -> windowsDrives(systemRoot);
            default -> Collections.emptyList();
        };

    }

    private static List<DrivePathNode> linuxDrives(IPathNode sysRoot) throws IOException, InterruptedException {



        CommandResult<LsblkCmd.Lsblk> result = LinuxCmds.lsblk().launch();


        List<DrivePathNode> driveList = new ArrayList<>();
        // Did not find anything or error
        if(result.isError())  {

            LOG.warn("No drives found");
            return driveList;
        }

        LsblkCmd.Lsblk lsblk = result.object();
        String userName = OS.getUser();

        // parent path where all linux media devices are mount
        IPathNode driveParentNode = null;
        //
        // for each disk there are one or more partition
        // some of them mounted
        //
        for( LsblkCmd.Disk disk : lsblk.diskMap().values() ) {

            String tranType = disk.tranTypeStr();
            String nameDisk = disk.nameStr();
            long sizeDisk = Long.parseLong( disk.sizeStr() );
            long availableDisk = Long.parseLong(disk.fsAvailStr());
            DrivePathNode.PhysicalDisk pd = DrivePathNode.PhysicalDisk.of(tranType);

            //
            // Partition
            for ( LsblkCmd.Partition part : disk.partList() ) {
                String mountPoint = part.mountpointStr();
                String fsType = part.fsTypeStr();
                String label = part.labelStr();
                String namePart = part.nameStr();
                boolean mounted = !mountPoint.isEmpty();
                long partSize = Long.parseLong( part.sizeStr() );
                long availablePart = Long.parseLong(part.fsAvailStr());


                // if not mounted we have maybe a path with is mountable but
                // not et mounted
                if( !mounted  ) {

                    // per se mountable disk drive
                    if(  null != tranType ) {

                        DrivePathNode notMountedDrive = new DrivePathNode(sysRoot,
                                ModIO.PATH_LINUX_NOT_FOUND,
                                nameDisk,
                                label,
                                DrivePathNode.LogicalType.DISK,
                                pd,
                                fsType,
                                partSize,
                                availableDisk,
                                false
                                );

                        driveList.add(notMountedDrive);
                    }

                }
                else {

                    // scan for parent path
                    // that we may use as a parent for all drives
                    // we want to show to the user.
                    // that is removable disks or other USB/CD/??? drives
                    Path parentPath = lookForLinuxParent(mountPoint,userName);

                    if( null != parentPath ) {

                        // we have a regular parent path
                        // and drive parent was not set
                        // we use this path as the global parent path
                        // for drives
                        if(null == driveParentNode) {
                            driveParentNode = RegularPathNode.of(sysRoot, parentPath);
                        } else {
                            // other path is parent path!?


                        }

                    } else {
                        // we have a mounted drive, but
                        // we don't think that this is a drive
                        // parent
                        // like [SWAP] / oder /home

                        continue;
                    }

                    Path mountPath = Paths.get(mountPoint);
                    DrivePathNode partNode = new DrivePathNode( driveParentNode,
                            mountPath,
                            nameDisk + "[" + namePart + "]",
                            label,

                            DrivePathNode.LogicalType.PART,
                            pd,
                            fsType,
                            partSize,
                            availablePart,
                            true);

                    driveList.add(partNode);
                }

            } // -- end partition


        } // -- end disk

        for ( LsblkCmd.Rom rom : lsblk.romList() ) {

            String tranType = rom.tranTypeStr();
            long availableRom = Long.parseLong(rom.fsAvailStr());
            DrivePathNode.PhysicalDisk pd = DrivePathNode.PhysicalDisk.of(tranType);
            String mountPoint = rom.mountStr();
            String fsType = rom.fsTypeStr();
            String label = rom.labelStr();
            String nameRom = rom.nameStr();
            long sizeRom = Long.parseLong(rom.sizeStr());
            boolean mounted = !mountPoint.isEmpty();


            //
            // a not mounted rom
            // add
            if(!mounted) {


                DrivePathNode notMountedRom = new DrivePathNode(sysRoot,
                        ModIO.PATH_LINUX_NOT_FOUND,
                        nameRom,
                        label,

                        DrivePathNode.LogicalType.ROM,
                        pd,
                        fsType,
                        sizeRom,
                        availableRom,
                        false
                        );

                    driveList.add(notMountedRom);

            } else {

                // drive parent not assigned yet
                if (null == driveParentNode) {

                    Path parentPath = lookForLinuxParent(mountPoint, userName);
                    if (null == parentPath) {

                        LOG.error("Found ROM(name='{}') without mount point usable (was '{}')", nameRom, mountPoint);
                    } else {
                        driveParentNode = RegularPathNode.of(sysRoot, parentPath);
                    }
                } else {

                    // we know drive parent path


                }

                Path mountPath = Paths.get(mountPoint);
                DrivePathNode romNode  = new DrivePathNode(driveParentNode,
                        mountPath,
                        nameRom,
                        label,

                        DrivePathNode.LogicalType.ROM,
                        pd,
                        fsType,
                        sizeRom,
                        availableRom,
                        mounted
                );

                driveList.add(romNode);
            } // -- end mounted
        } // -- end rom

        return driveList;
    }



    private static List<DrivePathNode> windowsDrives(IPathNode sysRoot) {



        List<DrivePathNode> rootDriveL = new ArrayList<>();
        Iterable<Path> rootDriveI = FileSystems.getDefault().getRootDirectories();

        while (rootDriveI.iterator().hasNext()) {

            Path drive = rootDriveI.iterator().next();

            String fsType = PathInfo.fileSystemTypeDesc(drive);
            String name  = drive.getFileName().toString();
            IBlockDeviceNode.PhysicalDisk pd = IBlockDeviceNode.PhysicalDisk.OTHER;
            IBlockDeviceNode.LogicalType lt = IBlockDeviceNode.LogicalType.DISK;
            long size = PathInfo.getLength(drive);
            long available = drive.toFile().getFreeSpace();

            DrivePathNode driveNode = new DrivePathNode(sysRoot, drive, name, name, lt, pd, fsType, size, available, true);

        rootDriveL.add(driveNode);
        }


        return rootDriveL;
    }

    private static Path lookForLinuxParent(String mountPointStr, String userNameStr ) {
        //
        // some special partition have non -usable format
        // like swap [SWAP]
        if( !mountPointStr.startsWith( "/" )) {

            // skip
            return null;
        }
        // Path to drive
        //
        Path mountPath = Paths.get(mountPointStr);

        if(null == mountPath.getParent()){
            // path is maybe root

            return null;
        }
        if( !mountPointStr.contains( userNameStr ) ) {

            // - not username path -
            // skip
            return null;
        }


        return mountPath.getParent();
    }

}