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

package eu.jpangolin.jtzipi.mymod.io.watcher;


import java.nio.file.Path;
import java.nio.file.Watchable;

/**
 * Listener for File System Path events.
 *
 * @author jTzipi
 */
public interface IFileSystemPathWatchListener {
    /**
     * Path was modified.
     *
     * @param parent parent path
     * @param path   path modified
     * @param cnt    how often
     */
    void onModified( Path parent, Path path, int cnt );

    /**
     * Path was created.
     *
     * @param parent parent dir
     * @param path   path created
     * @param cnt    how often
     */
    void onCreated( Path parent, Path path, int cnt );

    /**
     * Path deleted.
     *
     * @param parent parent dir
     * @param path   path deleted
     * @param cnt    how often
     */
    void onDeleted( Path parent, Path path, int cnt );

    /**
     * Path events overflow.
     * That is some events are lost.
     *
     * @param parent parent dir
     * @param context  object
     * @param cnt    how often
     */
    void onOverflow( Path parent, Object context, int cnt );

    /**
     * Reset of watch key failed.
     *
     * @param path path
     */
    void onResetFailed( final Path path );

    /**
     * There are no more watch keys.
     *
     * @param lastPath last path
     */
    void onPathToWatchEmpty( final Path lastPath );

    /**
     * There was an unknown watchable found.
     *
     * @param wt watchable
     */
    void onUnknownWatchable( Watchable wt );

    /**
     * Failed to register a path for watching.
     * @param path path that failed to register for watching
     */
    void onFileNotRegistered( final Path path );
}