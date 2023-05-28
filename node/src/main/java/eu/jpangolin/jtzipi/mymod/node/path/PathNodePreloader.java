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

package eu.jpangolin.jtzipi.mymod.node.path;

import eu.jpangolin.jtzipi.mymod.io.async.AbstractPreloadMemo;
import eu.jpangolin.jtzipi.mymod.node.INode;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Path Node Preloader.
 * <p>
 * For a given {@linkplain IPathNode} we compute and cache the sub nodes.
 * </p>
 *
 * @author jTzipi
 */
public final class PathNodePreloader extends AbstractPreloadMemo<IPathNode, List<INode<Path>>> {

    public static final org.slf4j.Logger LOG = LoggerFactory.getLogger( PathNodePreloader.class );
    private static final PathNodePreloader SINGLETON = new PathNodePreloader();

    private PathNodePreloader() {

    }

    /**
     * Return single instance.
     *
     * @return singleton
     */
    public static PathNodePreloader instance() {
        return SINGLETON;
    }


    @Override
    protected List<INode<Path>> compute( IPathNode iPathNode ) {

        Objects.requireNonNull( iPathNode, "PathNode must != null" );
        LOG.info( "Compute via memoizer '{}'", iPathNode );
        return iPathNode.getSubNodes();
    }
}
