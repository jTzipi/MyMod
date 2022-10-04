package eu.jpangolin.jtzipi.mymod.node.path;

import eu.jpangolin.jtzipi.mymod.node.INode;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Nodes {

    private Nodes() {

    }

    public static List<IPathNode> ofNodeList( final IPathNode parentNode, List<INode<Path>> nodeList ) {

        Objects.requireNonNull( nodeList );
        return nodeList.stream()
                .map( pn -> pn instanceof IPathNode
                        ? (IPathNode)pn
                        : RegularPathNode.of( parentNode, pn.getValue() ) )
                .collect( Collectors.toList() );
    }
}