/*
 *    Copyright (c) 2022-2023 Tim Langhammer
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

package eu.jpangolin.jtzipi.mymod.io.cmd.linux;

import eu.jpangolin.jtzipi.mymod.io.cmd.*;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Wrapper for 'lsblk' command.
 * For details see <a href="https://www.linux.org/docs/man8/lsblk.html">this</a> link.
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
     * Default Option we set for 'lsblk'.
     *
     * -b: byte size
     * -P: pairwise result
     * -i: Ascii Format
     * -o: Option to display(Must be last)
     */
    private static final List<Supplier<String>> ARGS_DEF =  List.of(LsblkCmdOptions.ASCII,
            LsblkCmdOptions.PAIRS,
            LsblkCmdOptions.BINARY,
            LsblkCmdOptions.OUTPUT,
            // IMPORTANT: the order of the columns MUST match the order of the LSBLK Regular expression match groups
            // - see LSBLK_REG
            () -> Stream.of(
                    LsblkColumn.TYPE,
                    LsblkColumn.TRAN,
                    LsblkColumn.FSTYPE,
                    LsblkColumn.FSAVAIL,
                    LsblkColumn.SIZE,
                    LsblkColumn.MOUNT,
                    LsblkColumn.SERIAL,
                    LsblkColumn.NAME,
                    LsblkColumn.LABEL,
                    LsblkColumn.UUID
            ).map(Supplier::get).collect( joining(",")));


    /**
     * All options for 'lsblk'.
     *
     *
     */
    public enum LsblkCmdOptions implements Supplier<String> {


        /**
         * List every device.
         */
        ALL("all"),

        /**
         * Print only ASCII.
         */
        ASCII("ascii"),
        /**
         * Print size in bytes.
         */
        BINARY("bytes"),

        /**
         * Show information about filesystem.
         */
        FILESYSTEM("fs"),
        /**
         * Print Json Formatted.
         */
        JSON("json"),


        /**
         * List of output columns.
         * @see LsblkColumn
         */
        OUTPUT("output"),
        /**
         * Print pairwise.
         */
        PAIRS("pairs");

        private final String opt;

        LsblkCmdOptions(String optStr ) {
    this.opt = "--" +optStr;

        }

        @Override
        public String get() {
           return opt;
        }


    }

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
     * Option for Linux Command 'lsblk' option `-o`.
     * Here are the keys and values.
     * <br/>
     *         NAME  Gerätename
     *        KNAME  interner Kernel-Gerätename
     *         PATH  Pfad zum Geräteknoten
     *      MAJ:MIN  Hauptversion:Nebengerätenummer
     *      FSAVAIL  verfügbare Dateisystemgröße
     *       FSSIZE  Dateisystemgröße
     *       FSTYPE  Dateisystemtyp
     *       FSUSED  belegte Dateisystemgröße
     *       FSUSE%  prozentuale Dateisystembelegung
     *        FSVER  Dateisystemversion
     *   MOUNTPOINT  Einhängeort des Gerätes
     *        LABEL  Dateisystem-BEZEICHNUNG
     *         UUID  Dateisystem-UUID
     *       PTUUID  Partitionstabellenbezeichner (üblicherweise UUID)
     *       PTTYPE  Partitionstabellentyp
     *     PARTTYPE  Partitionstyp-Code oder -UUID
     *  PARTTYPENAME  Partitionstypname
     *    PARTLABEL  Partitions-BEZEICHNUNG
     *     PARTUUID  Partitions-UUID
     *    PARTFLAGS  Partitionsmarkierungen
     *           RA  Read-ahead-Cache des Geräts
     *           RO  Nur-Lese-Gerät
     *           RM  entfernbares Gerät
     *      HOTPLUG  Wechseldatenträger oder Hotplug-Gerät (USB, PCMCIA …)
     *        MODEL  Gerätebezeichner
     *       SERIAL  Festplatten-Seriennummer
     *         SIZE  Größe des Geräts
     *        STATE  Status des Geräts
     *        OWNER  Benutzername
     *        GROUP  Gruppenname
     *         MODE  Geräteknoten-Berechtigungen
     *    ALIGNMENT  Ausrichtungs-Position
     *       MIN-IO  Minimale E/A-Größe
     *       OPT-IO  Optimale E/A-Größe
     *      PHY-SEC  physische Sektorgröße
     *      LOG-SEC  logische Sektorgröße
     *         ROTA  Rotationsgerät
     *        SCHED  Name des E/A-Schedulers
     *      RQ-SIZE  Größe der Warteschlange für Anforderungen
     *         TYPE  Gerätetyp
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
    public record Lsblk(Map<String, Disk> diskMap, List<Rom> romList)  {

    }

    /**
     * Lsblk command with default arguments.
     */
    public LsblkCmd() {
        this(ARGS_DEF.stream().map(Supplier::get).toList());
    }

    /**
     * Lsblk Command.
     * @param cmdArgStr argument
     */
    public LsblkCmd(List<String> cmdArgStr) {
        super(CMD, cmdArgStr);
    }


    @Override
    protected CommandResult<Lsblk> parse(String rawResultStr, Throwable t, Process proc) {

        // if we have no error nor error input we
        // parse raw result

        Lsblk lsblk = null == t  ? parseLsblk(rawResultStr) : null;

        return new CommandResult<>(lsblk, rawResultStr, proc, t);
    }

    private static Lsblk parseLsblk(String rawResultStr) {
        Objects.requireNonNull(rawResultStr);

        // Disk map and Rom list
        final Map<String, Disk> diskMap = new HashMap<>();
        final List<Rom> romList = new ArrayList<>();
        // disk to put partition
        Disk lastDisk = null;


        for (String line : rawResultStr.lines().toList()) {

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
