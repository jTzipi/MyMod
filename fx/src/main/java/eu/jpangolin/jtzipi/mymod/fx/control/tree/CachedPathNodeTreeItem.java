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

package eu.jpangolin.jtzipi.mymod.fx.control.tree;

import eu.jpangolin.jtzipi.mymod.io.async.IPreloadMemoized;
import eu.jpangolin.jtzipi.mymod.node.INode;
import eu.jpangolin.jtzipi.mymod.node.path.IPathNode;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.TreeItem;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Predicate;

/**
 * Tree Item wrapping a {@linkplain IPathNode}.
 * <p>
 * <h2>General purpose file system node tree item</h2>
 * <p>
 * We use some advanced techniques to achieve better ux.
 * <br/>
 * <br/>
 * <u>Most important</u>: {@linkplain IPreloadMemoized}.
 * <br/>
 * This is a cache for computations we need later.
 * <br/>
 * So when we want to use this we start for each new tree item we create
 * the computation of their sub nodes via this preloader and cache the result.
 * So the user may not notice a delay when clicking to expand a node.
 * If we have a lot sub nodes this may freeze the UI. So with this tool we may avoid this.
 * <br/>
 * If we want to make this better we can use {@linkplain eu.jpangolin.jtzipi.mymod.io.async.IPreloadMemoizedTemporal}.
 * This is an advanced version of the preloader cache.
 * <br/>
 * Here we not just only cache the computation but give each entry of the cache a time
 * budget. So if we cache a lot of items this can consume lots of memory.
 * If we remove some of the sub nodes we created we can cope with
 *
 * </p>
 *
 *
 * </p>
 */
public class CachedPathNodeTreeItem extends AbstractPathNodeTreeItem {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( "PathTreeItem" );

    //
    // Executor Service to start async loading of sub nodes
    private static final ExecutorService NLES = Executors.newCachedThreadPool();

    //
    // Preloader to memoize nodes
    // If this is not null we preload every sub node of this node
    // to getter responsiveness
    private static IPreloadMemoized<IPathNode, List<INode<Path>>> PATH_PRELOADER;
    private Task<Void> loadTask;

    /**
     * C.
     *
     * @param pathNode IPathNode to wrap
     */
    public CachedPathNodeTreeItem( final IPathNode pathNode ) {
        super( pathNode );

    }

    /**
     * Return new PathTreeNode.
     * <p>
     * If we have a {@linkplain CachedPathNodeTreeItem#PATH_PRELOADER} != null
     * we load the sub nodes async.
     * </p>
     *
     * @param pathNode pathNode to wrap
     * @return PathTreeNode
     * @throws NullPointerException if {@code pathNode}
     */
    public static CachedPathNodeTreeItem of( IPathNode pathNode ) {

        Objects.requireNonNull( pathNode, "Path Node is null" );

        // If we have PATH_PRELOADER we
        // start the preloading
        if ( null != PATH_PRELOADER ) {
            PATH_PRELOADER.put( pathNode );
        }

        return new CachedPathNodeTreeItem( pathNode );
    }

    /**
     * Set a static path preloader
     *
     * @param pathPreloader sub path preloader. If null you can disable pre loading
     */
    public static void setPathPreloader( IPreloadMemoized<IPathNode, List<INode<Path>>> pathPreloader ) {

        CachedPathNodeTreeItem.PATH_PRELOADER = pathPreloader;
    }


    private static ObservableList<CachedPathNodeTreeItem> wrap( List<INode<Path>> pathNodeList ) {

        List<CachedPathNodeTreeItem> pathTreeL = toStreamFiltered( pathNodeList )
                .map( CachedPathNodeTreeItem::of )
                .toList();
        return FXCollections.observableArrayList( pathTreeL );
    }

    /**
     * Call this method to recreate sub node.
     */
    @Override
    public void reload() {

    }

    @Override
    void cancel() {
        if ( null != loadTask && loadTask.isRunning() ) {

            boolean canceled = loadTask.cancel( true );
            LOG.info( "Task canceled by user?'{}'", canceled );

        }
    }

    @Override
    void computeSubNodes() {
        /*
         * This is a complex part.
         * --------
         * We have to distinguish
         * -- 1. The PRELOADER is null --
         *       -> load the sub nodes in EDT
         * -- 1a) best case
         *       -> the sub nodes are loaded
         *          we have no cost
         * -- 1b) worst case
         *       -> the sub nodes are not loaded, and we have to wait blocking
         *
         * -- 2. We have a PRELOADER --
         *
         * -- 2.1) The sub nodes are memoized
         * -- 2.1a) Sub nodes are loaded
         *       -> only get them
         *          we have no cost
         * -- 2.1b) Sub nodes are loading
         *       -> start a PreLoadTask and wait nonblocking for them
         *
         * -- 2.2) The sub nodes are not memoized
         *       -> start the computation and thereafter a PreLoadTask waiting for the result nonblocking
         */

        //  NEVER BE NULL
        final IPathNode pathNode = getValue();


        // Case 1.
        // load the sub nodes via EDT
        if ( null == PATH_PRELOADER ) {

            LOG.info( "We have no PATH_PRELOADER. Load sub nodes sync." );
            postLoadSub();
            // -- end loading
        } else {

            // Case 2. load the sub nodes with a preloader
            //
            if ( PATH_PRELOADER.isMemoized( pathNode ) ) {
                // 2.1) Sub nodes memoized
                LOG.info( "PathNode '{}' load sub nodes preloaded", pathNode );
                // future
                Future<List<INode<Path>>> future = PATH_PRELOADER.get( pathNode );

                if ( future.isDone() ) {
// 2.1a) Attempt to get the result if ready
// this should never throw an exception

                    try {
                        future.get();
                        postLoadSub();
                    } catch ( CancellationException | ExecutionException | InterruptedException polyE ) {
                        LOG.warn( "Error during future.get()", polyE );
                        setNodeState( LoadState.NOT_LOADED );
                    }


                } else {


                    // 2.1b) sub nodes are loading
                    //

                    // Wait nonblocking for the result
                    startTask( future );
                }
            } else {
                // 2.2) the sub nodes are not memoized!
                //

                LOG.info( "We have a Preloader but sub nodes are note memoized! Start preloading!" );

                // Path Preloader is not null but path node is not preloaded
                //
                startTask( PATH_PRELOADER.start( pathNode ) );
            }

        }

    }

    private void startTask( Future<List<INode<Path>>> futureTask ) {
        if ( null != loadTask && loadTask.isRunning() ) {
            throw new IllegalStateException( "Warn! Try to start a new task while task!" );
        }
        setNodeState( LoadState.LOADING );
        loadTask = new JoinPathLoadTask( futureTask );
        loadTask.setOnCancelled( wse -> onTaskCancelled( wse, futureTask ) );
        loadTask.setOnFailed( wse -> onTaskFailed( wse, futureTask ) );
        loadTask.setOnSucceeded( this::onTaskDone );
        NLES.submit( loadTask );
    }

    private void onTaskCancelled( WorkerStateEvent wse, Future<List<INode<Path>>> future ) {

        LOG.info( "Load Task cancelled!" );
        setNodeState( LoadState.CANCELED );
        boolean taskRem = PATH_PRELOADER.remove( getValue(), future );
        future.cancel( true );
        LOG.info( "Cache removed ?  {}", taskRem );
    }

    private void onTaskFailed( WorkerStateEvent wse, Future<List<INode<Path>>> future ) {

        LOG.info( "Load Task Failed!", loadTask.getException() );
        boolean taskRem = PATH_PRELOADER.remove( getValue(), future );
        future.cancel( true );
        setNodeState( LoadState.NOT_LOADED );
        LOG.info( "Cache removed ? {}", taskRem );
    }

    private void onTaskDone( WorkerStateEvent wse ) {

        postLoadSub();

    }


    private void postLoadSub() {

        //
        // This is the step we call after the computation of sub nodes
        // --
        // Either sync or async.
        // 1. Set the node state
        // 2. Set task null if was used
        // 3. Set sub nodes


        // 1.)
        if ( null == getValue().getNodeCreationError() ) {
            setNodeState( LoadState.LOADED );
        } else {
            setNodeState( LoadState.LOADED_WITH_IO_ERROR );
        }

        // 2.)
        // set task to null
        // to free resources
        if ( null != loadTask ) {
            if ( loadTask.isRunning() ) {

                LOG.error( "Error! Calling postLoadSub() but task is running!" );
            } else {
                loadTask = null;
            }
        }

        // 3.)
        List<INode<Path>> subNodeL = getValue().getSubNodes();
        super.getChildren().setAll( wrap( subNodeL ) );
    }

    /**
     * The purpose of this thread task is to wait for the result of
     * the creation of the sub nodes from a {@linkplain IPathNode}.
     * <p>
     * So we don't need to worry much about the exceptions throws by the
     * {@link java.util.Map#get(Object)} method.
     * Since we are in control of the code.
     * <p>
     * Every state of this task is handled here too.
     * So we don't need listeners.
     */
    private static class JoinPathLoadTask extends Task<Void> {

        private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( "JoinPathLoadTask" );
        private final Future<List<INode<Path>>> f;

        private JoinPathLoadTask( Future<List<INode<Path>>> future ) {

            this.f = future;
        }

        @Override
        protected Void call() throws InterruptedException, ExecutionException {

            LOG.info( "PathLoadTask:: Wait for computation..." );

            //
            // HINT: This computation is always the call to
            // IPathNode.getSubNodes()
            // This can never throw a checked exception, so we don't
            // need to handle those exceptions here.
            // We can handle this via the methods : cancel() and failed()
            //
            f.get();
            return null;
        }

    }
}