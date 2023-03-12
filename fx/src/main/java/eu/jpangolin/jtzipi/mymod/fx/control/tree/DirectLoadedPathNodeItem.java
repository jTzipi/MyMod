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
     * @param pathNode
     * @return
     */
    public static DirectLoadedPathNodeItem of( IPathNode pathNode, boolean obs ) {
        return new DirectLoadedPathNodeItem( Objects.requireNonNull( pathNode, "node is null!" ), obs );
    }

    /**
     * @param pathNode
     * @return
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

                // TODO: klappt das?
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
