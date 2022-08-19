package eu.jpangolin.jtzipi.mymod.node.path;

import eu.jpangolin.jtzipi.mymod.io.ModIO;
import eu.jpangolin.jtzipi.mymod.node.INode;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import java.util.function.Predicate;

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
     * File size of path in bytes or {@linkplain ModIO#DIR_LENGTH}.
     * @return length of content of path
     */
    long getFileLength();

    /**
     * Return file creation time.
     * @return  creation time
     */
    FileTime getFileCreationTime();
}
