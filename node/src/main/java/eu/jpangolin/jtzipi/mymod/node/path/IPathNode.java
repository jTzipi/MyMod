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
import eu.jpangolin.jtzipi.mymod.node.INode;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.function.Predicate;

/**
 * This models a node of a file system with the {@link java.nio.file.Path} API.
 * <p>
 * A path node have <u>always</u> one parent unless it is the root.
 * The root have no parent. For the root node we use the special {@link RootPathNode} .
 * <br/>
 * Each path node have attributes. These are
 *     <ul>
 *         <li>Path name. That is the last path components name</li>
 *         <li>Path description. This is the os specific path description </li>
 *         <li>Path type. This is the MIME type of a file if can be determined</li>
 *         <li>Path length. The file length in bytes in case of a file or {@link ModIO#PATH_DIR_LENGTH} for dir. If we can not determine the length we return {@link ModIO#LENGTH_PATH_NA}</li>
 *
 *     </ul>
 * </p>
 *
 *
 * @author jTzipi
 */
public interface IPathNode extends INode<Path>, Comparable<IPathNode> {

    /**
     * Predicate that accept all.
     */
    Predicate<Path> PREDICATE_ACCEPT_PATH_ALL = path -> true;

    /**
     * Name of path.
     * <p>
     *     That is the name of last path component.
     * </p>
     *
     * @return name
     */
    String getName();

    /**
     * Path description.
     * System dependent description of file.
     * @return description
     */
    String getDesc();

    /**
     * Path type.
     * <p>
     *     For example 'Image/png'.
     * </p>
     *
     * @return type
     */
    String getType();

    /**
     * Is the sub node list created.
     * @return {@code true} if node has created sub nodes
     */
    boolean isNodeSubListCreated();

    /**
     * Path is a link to another path.
     * @return is this path a link
     */
    boolean isLink();

    /**
     * Path is a directory.
     * @return is this path a directory
     */
    boolean isDir();

    /**
     * Path is readable by Java.
     * @return {@code true} if path is regular readable
     */
    boolean isReadable();

    /**
     * Path is hidden.
     * @return path is hidden
     */
    boolean isHidden();

    /**
     * File size of path in bytes or {@linkplain ModIO#PATH_DIR_LENGTH}.
     *
     * @return length of content of path
     */
    long getFileLength();

    /**
     * Set the created flag to false.
     * <p>
     * If you need a re creation of sub nodes or re init the path attributes call this.
     * </p>
     */
    void requestReload();

    /**
     * In case of an error creating sub nodes we can get this.
     *
     * @return creation error
     */
    IOException getNodeCreationError();

    /**
     * Return file creation time.
     *
     * @return creation time
     */
    FileTime getFileCreationTime();

}