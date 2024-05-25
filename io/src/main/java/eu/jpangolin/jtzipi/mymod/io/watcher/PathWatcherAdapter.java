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
 * Adapter for {@link IFileSystemPathWatchListener}.
 *
 * @author
 */
public class PathWatcherAdapter implements IFileSystemPathWatchListener {
    @Override
    public void onModified( Path parent, Path context, int cnt ) {

    }

    @Override
    public void onCreated( Path parent, Path context, int cnt ) {

    }

    @Override
    public void onDeleted( Path parent, Path context, int cnt ) {

    }

    @Override
    public void onOverflow(Path parent, Object context, int cnt) {

    }


    @Override
    public void onResetFailed( Path path ) {

    }

    @Override
    public void onPathToWatchEmpty( Path lastPath ) {

    }

    @Override
    public void onUnknownWatchable( Watchable wt ) {

    }

    @Override
    public void onFileNotRegistered( Path path ) {

    }


}