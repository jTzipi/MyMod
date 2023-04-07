package eu.jpangolin.jtzipi.mymod.fx.control.tree;

import eu.jpangolin.jtzipi.mymod.io.ModIO;
import eu.jpangolin.jtzipi.mymod.node.INode;
import eu.jpangolin.jtzipi.mymod.node.path.IPathNode;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static eu.jpangolin.jtzipi.mymod.node.path.IPathNode.PREDICATE_ACCEPT_PATH_ALL;

/**
 * Base class for {@linkplain IPathNode} wrapping tree node.
 * <p>
 * <u>Abstract base for tree node items wrapping a {@link IPathNode}.</u>
 * <br/>
 * <br/>
 * We have two global static variables.
 * <ul>
 *     <li>{@linkplain TreeItemNotifier}</li>
 *     <li>{@code FX_GLOBAL_FILTER_PROP}</li>
 *
 * </ul>
 * The TreeItemNotifier is used to observe changes of the wrapping {@code IPathNode} which wraps
 * a {@link Path}.
 * <br/>
 * So we have the ability to react on those changes.
 * <br/
 * <br/>
 * The Filter prop is used to filter the sub nodes.
 * <br/>
 * Important for the sub classes is to override
 * {@code createSubNodes()}
 * and {@code cancel()}.
 * <br/>
 * The latter is used to cancel a task creating sub nodes. In case of loading the sub nodes synchronous this
 * can be empty.
 * The main idea behind this class on the other hand was to build responsive tree nodes which do <u>not</u>
 * block on creating sub nodes.
 * <br/>
 * Indicating the <i>loading</i> state of this node we use the {@code LoadState} enum.
 * </p>
 */
public abstract class AbstractPathNodeTreeItem extends TreeItem<IPathNode> {


    /**
     * Predicate to filter dir.
     */
    public static final Predicate<? super IPathNode> PREDICATE_DIR_ONLY = IPathNode::isDir;


    static final org.slf4j.Logger BASE_LOG = LoggerFactory.getLogger( AbstractPathNodeTreeItem.class );
    static final ObjectProperty<Predicate<? super IPathNode>> FX_GLOBAL_FILTER_PROP = new SimpleObjectProperty<>( pathNode -> true );

    static TreeItemNotifier TINO;
    /**
     * The load state.
     */
    protected LoadState loadState;
    /**
     * Should this node observable.
     */
    protected boolean observable;
    private ReadOnlyObjectWrapper<LoadState> FX_NODE_STATE_PROP_RO;

    protected AbstractPathNodeTreeItem( final IPathNode pathNode ) {
        this( pathNode, false );
    }


    protected AbstractPathNodeTreeItem( final IPathNode pathNode, boolean observable ) {
        super( pathNode );
        this.observable = observable;
        // in case of leaf we do not need to load sub nodes
        // -> see isLeaf
        this.loadState = pathNode.isLeaf() ? LoadState.LOADED : LoadState.NOT_LOADED;

        FX_GLOBAL_FILTER_PROP.addListener( this::onFilterChanged );
        // we want to observe this node
        if ( observable ) {
            if ( null != TINO ) {
                TINO.observe( this );
            } else {

                BASE_LOG.warn( "Try to observe this node but TINO is null!" );
            }
        }
    }

    /**
     * Bind a filter for {@link IPathNode} to this global filter.
     * setting to null unbind the current used filter.
     *
     * @param globalFilterProp filter or null to unbind
     */
    public static void bindGlobalDirFilter( ObjectProperty<Predicate<? super IPathNode>> globalFilterProp ) {

        if ( FX_GLOBAL_FILTER_PROP.isBound() ) {
            FX_GLOBAL_FILTER_PROP.unbind();
        }
        if ( null != globalFilterProp ) {
            FX_GLOBAL_FILTER_PROP.bind( globalFilterProp );

        }
    }

    /**
     * Set global File System Watcher.
     *
     * @param treeItemNotifier tree item notifier
     * @throws NullPointerException if {@code treeItemNotifier} is null
     */
    public static void setTreeItemNotifier( TreeItemNotifier treeItemNotifier ) {
        if ( null != TINO ) {
            AbstractPathNodeTreeItem.BASE_LOG.warn( " you try to re-assign tree notifier" );
        }
        AbstractPathNodeTreeItem.TINO = Objects.requireNonNull( treeItemNotifier );
    }

    static Stream<IPathNode> toStreamFiltered( List<INode<Path>> subNodeList ) {
        return subNodeList.stream()
                .map( no -> ( IPathNode ) no )
                .filter( FX_GLOBAL_FILTER_PROP.getValue() );
    }

    protected void init() {

    }

    @Override
    public boolean isLeaf() {
        return getValue().isLeaf();
    }

    @Override
    public ObservableList<TreeItem<IPathNode>> getChildren() {

        // BASE_LOG.info( "@getChildren() not loaded? {}", isNotLoaded() );

        if ( isNotLoaded() ) {
            BASE_LOG.info( "... create Sub Nodes for {} ...", getValue() );
            computeSubNodes();
        }

        return super.getChildren();
    }

    /**
     * Return read only property of this node state.
     *
     * @return node state prop
     */
    public final ReadOnlyObjectProperty<LoadState> nodeStatePropFX() {

        if ( null == FX_NODE_STATE_PROP_RO ) {
            this.FX_NODE_STATE_PROP_RO = new ReadOnlyObjectWrapper<>( this, "FX_NODE_STATE_PROP", loadState );
        }

        return FX_NODE_STATE_PROP_RO.getReadOnlyProperty();
    }

    /**
     * Reload this node.
     */
    public abstract void reload();

    /**
     * Return the node loading state.
     *
     * @return state of node
     */
    public final LoadState getNodeState() {
        return loadState;
    }

    /**
     * Set node state.
     *
     * @param loadState node state
     */
    void setNodeState( LoadState loadState ) {
        assert null != loadState : "Assume we have valid state";
        this.loadState = loadState;
    }

    /**
     * If this node's sub nodes are not loaded.
     * <p>This makes only sense for nodes which can have sub nodes like dirs</p>
     *
     * @return {@code true}  if the sub nodes of this node are not loaded
     */
    boolean isNotLoaded() {
        return !isLeaf() && ( LoadState.NOT_LOADED == getNodeState() || LoadState.CANCELED == getNodeState() );
    }

    abstract void cancel();

    /**
     * Compute sub nodes.
     */
    abstract void computeSubNodes();

    private void onFilterChanged( ObservableValue<? extends Predicate<? super IPathNode>> observableValue, Predicate<? super IPathNode> predOld, Predicate<? super IPathNode> predNew ) {

        if ( null == predNew || predNew.equals( predOld ) ) {
            return;
        }

        // TODO:
        // Wenn node geladen wird???
        // we need to filter sub nodes
        List<TreeItem<IPathNode>> treeItemList = getChildren().stream().filter( treeItem -> predNew.test( treeItem.getValue() ) ).toList();
        super.getChildren().setAll( treeItemList );
    }

    /**
     * State of tree node sub nodes.
     */
    public enum LoadState {

        /**
         * Sub Nodes not loaded.
         */
        NOT_LOADED( 0 ),
        /**
         * Sub Nodes loaded.
         */
        LOADED( 7 ),
        /**
         * Sub node loading error.
         * <p>Because of </p>
         */
        LOADED_WITH_IO_ERROR( 5 ),
        /**
         * Sub nodes loading canceled.
         * <p>
         * This can be happened because of thread interruption
         * or user cancel request.
         */
        CANCELED( 2 ),
        /**
         * Sub Nodes loading.
         */
        LOADING( 1 );

        private final int state;

        LoadState( int step ) {
            this.state = step;
        }

        /**
         * Get state.
         *
         * @return state
         */
        public int getState() {
            return state;
        }
    }
}
