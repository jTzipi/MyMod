package eu.jpangolin.jtzipi.mymod.io.cmd.linux;

import eu.jpangolin.jtzipi.mymod.io.cmd.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper for 'lsblk' command.
 */
public final class LsblkCmd extends AbstractInstantCommand<LsblkCmd.Lsblk> {


    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger("LsblkCmd");
    /**
     * Regular Expression to parse the columns of the 'lsblk' command.
     */
    private static final String LSBLK_REG =
            "^TYPE=\"(?<type>[a-z]+?)\"\\s" +
                    "TRAN=\"(?<tran>[a-z]*?)\"\\s" +
                    "FSTYPE=\"(?<fstype>[a-z0-9]*?)\"\\s" +
                    "FSAVAIL=\"(?<fsavail>[0-9]*?)\"\\s" +
                    "SIZE=\"(?<size>[0-9]*?)\"\\s" +
                    "MOUNTPOINT=\"(?<mountpoint>[^\"]*?)\"\\s" +
                    "SERIAL=\"(?<serial>[0-9A-Z]*?)\"\\s" +
                    "NAME=\"(?<name>[a-z0-9]*?)\"\\s" +
                    "LABEL=\"(?<label>[a-zA-Z0-9 ]*?)\"\\s" +
                    "UUID=\"(?<uuid>[a-zA-Z0-9-]*?)\"$";

    private static final Pattern LSBLK_COLUMN_PATTERN = Pattern.compile(LSBLK_REG);

    private static final String CMD = "lsblk";  // the command name

    /**
     * Default Option we set for 'lsblk'
     *
     * -b: byte size
     * -P: pairwise result
     * -i: Ascii Format
     * -o: Option to display(Must be last)
     */
    public static final String OPT_DEF = "-bPio"; // the option we use default

    /**
     * Default argument array.
     */
    public static final String[] ARGS_DEF = {
            //
            // -- command array
            //    1.) 'lsblk'
            //    2.) bytes(b) Pairwise(P) with ASCII only(i) option(o)
            //    3.-10.) Column
            CMD,
            OPT_DEF,
            String.join(",",
                    LsblkColumn.TYPE.opt,
                    LsblkColumn.TRAN.opt,
                    LsblkColumn.SIZE.opt,
                    LsblkColumn.FSTYPE.opt,
                    LsblkColumn.FSAVAIL.opt,
                    LsblkColumn.MOUNT.opt,
                    LsblkColumn.SERIAL.opt,
                    LsblkColumn.NAME.opt,
                    LsblkColumn.LABEL.opt,
                    LsblkColumn.UUID.opt
            )
    };

    /**
     * A lsblk entry describing a Read Only Memory block device.
     *
     * @param nameStr     name
     * @param tranTypeStr tran
     * @param fsTypeStr   file system type
     * @param sizeStr     size
     * @param fsAvailStr  available system size
     * @param mountStr    mount point
     * @param serialStr   serial num
     * @param labelStr    label
     * @param uuidStr     UUID
     */
    public record Rom(String nameStr, String tranTypeStr, String fsTypeStr, String sizeStr, String fsAvailStr,
                      String mountStr, String serialStr, String labelStr, String uuidStr) {
    }

    /**
     * A 'lsblk' entry describing a whole disk.
     *
     * @param nameStr     name
     * @param tranTypeStr
     * @param sizeStr     size
     * @param fsAvailStr
     * @param serialStr
     * @param partList
     */
    public record Disk(String nameStr, String tranTypeStr, String sizeStr, String fsAvailStr, String serialStr,
                       List<Partition> partList) {
    }

    /**
     * A Partition of a {@link Disk}.
     * @param fsTypeStr File System type
     * @param sizeStr size
     * @param fsAvailStr free size
     * @param mountpointStr mount point
     * @param uuidStr UUID
     * @param labelStr label
     * @param nameStr name
     */
    public record Partition(String fsTypeStr, String sizeStr, String fsAvailStr, String mountpointStr, String uuidStr, String labelStr, String nameStr) {
    }

    /**
     * Option for Linux Command 'lsblk'.
     */
    public enum LsblkColumn implements Supplier<String> {

        /**
         * Type of block device.
         */
        TYPE("TYPE", 0),
        /**
         *
         */
        TRAN("TRAN", 1),
        /**
         * File System Type.
         */
        FSTYPE("FSTYPE", 2),
        /**
         * Mount point.
         */
        MOUNT("MOUNTPOINT", 3),
        /**
         * Serial num.
         */
        SERIAL("SERIAL", 4),
        /**
         * Name of block device.
         */
        NAME("NAME", 5),
        /**
         * Label of block device.
         */
        LABEL("LABEL", 6),
        /**
         * UUID of block device.
         */
        UUID("UUID", 7),
        /**
         * Size of block device.
         */
        SIZE("SIZE", 8),
        /**
         * File System available bytes.
         */
        FSAVAIL("FSAVAIL", 9);

        private final String opt;
        private final int pos;

        LsblkColumn(String optionStr, int posi) {
            this.opt = optionStr;
            this.pos = posi;
        }

        public int pos() {
            return pos;
        }

        @Override
        public String get() {
            return opt;
        }

    }

    /**
     * Result of the linux command 'lsblk'.
     * For details see <a href="https://www.linux.org/docs/man8/lsblk.html">this</a> link.
     *
     * @param diskMap list of disks
     * @param romList list of roms
     */
    public record Lsblk(Map<String, Disk> diskMap, List<Rom> romList) {
    }



    /**
     * Lsblk Command.
     * @param argList argument
     */
    public LsblkCmd(List<String> argList) {
        super(CMD, argList);
    }


    @Override
    protected Optional<Lsblk> parse(ICommandResult commandResult) {
    Objects.requireNonNull(commandResult);
        // if we have no error nor error input we
        // parse raw result
        if( null == commandResult.getError() && !ICommandResult.RAW_RESULT_ERROR.equals(commandResult.getRawResult())) {

            return Optional.of(parseLsblk(commandResult.getRawResult()));
        }
        return Optional.empty();
    }

    private static Lsblk parseLsblk(String rawResult) {


        // Disk map and Rom list
        final Map<String, Disk> diskMap = new HashMap<>();
        final List<Rom> romList = new ArrayList<>();
        // disk to put partition
        Disk lastDisk = null;


        for (String line : rawResult.lines().toList()) {

            // parse raw line
            EnumMap<LsblkColumn, String> rowMap = parseLsblkRow(line);
            // switch type - must be nonnull!
            String type = rowMap.get(LsblkColumn.TYPE);

            if (null == type) {

                LOG.error("Lsblk Type is null!");
                continue;
            }


            //
            // -- lsblk prints always the disk first
            //    ,so we MUST have a valid disk before parsing partition
            //    .ROM seem to be always one line
            switch (type) {
                case "disk" -> {

                    Disk disk = parseDisk(rowMap);
                    lastDisk = disk;

                    diskMap.put(disk.nameStr(), disk);
                }
                case "rom" -> romList.add(parseRom(rowMap));
                case "part" -> {
                    Partition part = parsePartition(rowMap);
                    if (null == lastDisk) {
                        throw new IllegalStateException("'Lsblk' Last disk is null!");
                    }

                    diskMap.get(lastDisk.nameStr()).partList().add(part);
                }
                default -> throw new IllegalStateException("'Lsblk-Type' unknown '" + type);


            }
        }

        return new Lsblk(diskMap, romList);
    }

    /**
     * Split the raw 'lsblk' answer row vise into parts and put them into
     * the enum map.
     * <p>
     * Format should be
     * <code>TYPE="disk" TRAN="usb" FSTYPE="" MOUNTPOINT="" SERIAL="575844314536334D4C544A37" LABEL="" NAME="sdb" UUID=""</code>
     * </p>
     *
     * @param row raw row
     * @return map of LsblkColumn and Value
     */
    private static EnumMap<LsblkColumn, String> parseLsblkRow(String row) {


        // LOG.info( "Try to parse row = {}", row );
        Matcher matcher = LSBLK_COLUMN_PATTERN.matcher(row);
        boolean found = matcher.find();
        LOG.info("Found Lsblk option? = " + found);
        EnumMap<LsblkColumn, String> ret = new EnumMap<>(LsblkColumn.class);

        for (LsblkColumn lsblkColumn : LsblkColumn.values()) {
            String cgrp = lsblkColumn.get().toLowerCase();
            // LOG.info( "Suche nach Grp {}", cgrp );

            ret.put(lsblkColumn, matcher.group(cgrp));
        }
        return ret;

    }

    private static Partition parsePartition(EnumMap<LsblkColumn, String> map) {

        Partition part = new Partition(map.get(LsblkColumn.FSTYPE),
                map.get(LsblkColumn.SIZE),
                map.get(LsblkColumn.FSAVAIL),
                map.get(LsblkColumn.MOUNT),
                map.get(LsblkColumn.UUID),
                map.get(LsblkColumn.LABEL),
                map.get(LsblkColumn.NAME));

        LOG.info("parse Partition = {}", part);

        return part;
    }

    private static Disk parseDisk(EnumMap<LsblkColumn, String> map) {

        Disk disk = new Disk(map.get(LsblkColumn.NAME),
                map.get(LsblkColumn.TRAN),
                map.get(LsblkColumn.SIZE),
                map.get(LsblkColumn.FSAVAIL),
                map.get(LsblkColumn.SERIAL),

                new ArrayList<>());
        LOG.info("Parse Disk {}", disk);

        return disk;
    }

    private static Rom parseRom(EnumMap<LsblkColumn, String> map) {

        Rom rom = new Rom(map.get(LsblkColumn.NAME),
                map.get(LsblkColumn.TRAN),
                map.get(LsblkColumn.FSTYPE),
                map.get(LsblkColumn.SIZE),
                map.get(LsblkColumn.FSAVAIL),
                map.get(LsblkColumn.MOUNT),
                map.get(LsblkColumn.SERIAL),
                map.get(LsblkColumn.LABEL),
                map.get(LsblkColumn.UUID));

        LOG.info("Parse ROM {}", rom);
        return rom;
    }


}
