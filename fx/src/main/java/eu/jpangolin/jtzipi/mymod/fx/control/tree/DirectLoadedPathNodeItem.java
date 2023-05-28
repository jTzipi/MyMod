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

import eu.jpangolin.jtzipi.mymod.node.INode;
import eu.jpangolin.jtzipi.mymod.node.path.IPathNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;


/**
 * PathNodeTreeItem with direct sub node loading.
 * <p>
 *
 *     We use the {@linkplain CompletableFuture} framework here to
 *     load the sub nodes and place them into the scene graph
 *     via {@link Platform#runLater(Runnable)}.
 * </p>
 * @author jTzipi
 */
public class DirectLoadedPathNodeItem extends AbstractPathNodeTreeItem {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( "DirectLoadPathNodeItem" );
    private CompletableFuture<?> loadTask;


    protected DirectLoadedPathNodeItem( final IPathNode node ) {
        this( node, false );
    }

    protected DirectLoadedPathNodeItem( final IPathNode node, boolean observable ) {
        super( node, observable );
    }

    /**
     * Create a direct loaded tree path node item that maybe observed.
     * @param pathNode path node
     * @param obs should this path be observed
     * @return direct loaded tree path node item
     * @throws NullPointerException if {@code pathNode}
     */
    public static DirectLoadedPathNodeItem of( IPathNode pathNode, boolean obs ) {
        return new DirectLoadedPathNodeItem( Objects.requireNonNull( pathNode, "node is null!" ), obs );
    }

    /**
     * Create a direct loaded tree path node item that is not observed.
     * @param pathNode path node
     * @return direct loaded tree item path node
     */
    public static DirectLoadedPathNodeItem of( IPathNode pathNode ) {
        return of( pathNode, false );
    }

    private static ObservableList<DirectLoadedPathNodeItem> wrap( List<INode<Path>> subNodeList ) {

        return FXCollections.observableArrayList( toStreamFiltered( subNodeList )
                .map( DirectLoadedPathNodeItem::of )
                .toList() );
    }

    @Override
    public void reload() {

    }

    void cancel() {
        if ( null != loadTask ) {
            if ( !loadTask.isDone() ) {
                loadTask.cancel( true );
                setNodeState( LoadState.CANCELED );
            } else {
                LOG.info( "Try to cancel load task but was done" );
            }
        } else {

            LOG.info( "Try to cancel not running task" );
        }
    }

    @Override
    void computeSubNodes() {
        setNodeState( LoadState.LOADING );
        LOG.warn( "Start async task" );
        loadTask = CompletableFuture.supplyAsync( () -> getValue().getSubNodes() ).thenAccept( subNode ->


                Platform.runLater( () -> {
                            LOG.info( "Pre super.getChild" );
                            super.getChildren().setAll( wrap( subNode ) );

                            if ( null != getValue().getNodeCreationError() ) {
                                setNodeState( LoadState.LOADED_WITH_IO_ERROR );
                            } else {
                                setNodeState( LoadState.LOADED );
                            }
                        }
                )
        );
    }


}
