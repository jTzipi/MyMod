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
