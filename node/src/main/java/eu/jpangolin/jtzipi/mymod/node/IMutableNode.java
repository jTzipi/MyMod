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

package eu.jpangolin.jtzipi.mymod.node;

/**
 * Mutable Node.
 * <p>
 * Added setter methods.
 * </p>
 *
 * @param <T> type of node
 * @author jTzipi
 */
public interface IMutableNode<T> extends INode<T> {

    /**
     * Set value.
     *
     * @param value value
     */
    void setValue( T value );

    /**
     * Add node to nodes sub nodes.
     *
     * @param node node
     */
    void addNode( INode<T> node );

    /**
     * Add node to sub nodes for position.
     *
     * @param node  node
     * @param index index ({@code index} &ge; 0 and sub nodes size &gt; {@code index}
     */
    void addNode( INode<T> node, int index );

    /**
     * Remove node from sub nodes.
     *
     * @param node node to remove
     */
    void removeNode( INode<T> node );

    /**
     * Remove node for position.
     *
     * @param index index [0 .. sub nodes size -1]
     * @return node for position
     * @throws ArrayIndexOutOfBoundsException if {@code index} &lt; 0 || this sub nodes size &le; {@code index}
     */
    INode<T> removeNode( int index );
}