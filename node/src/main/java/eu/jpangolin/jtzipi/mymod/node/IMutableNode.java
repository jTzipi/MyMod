package eu.jpangolin.jtzipi.mymod.node;

public interface IMutableNode<T> extends INode<T> {

    /**
     * Set value.
     * @param value value
     */
    void setValue( T value );

    /**
     * Add node to nodes sub nodes.
     * @param node node
     */
    void addNode( INode<T> node );

    /**
     * Add node to sub nodes for position.
     * @param node node
     * @param index index ({@code index} &ge; 0 and sub nodes size &gt; {@code index}
     */
    void addNode( INode<T> node, int index);

    /**
     * Remove node from sub nodes.
     * @param node node to remove
     */
    void removeNode( INode<T> node );

    /**
     * Remove node for position.
     * @param index index [0 .. sub nodes size -1]
     * @return node for position
     * @throws ArrayIndexOutOfBoundsException if {@code index} &lt; 0 || this sub nodes size &le; {@code index}
     */
    INode<T> removeNode( int index );
}
