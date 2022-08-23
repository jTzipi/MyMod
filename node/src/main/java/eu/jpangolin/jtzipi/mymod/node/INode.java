/*
 * Copyright (c) 2022 Tim Langhammer
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

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;


/**
 * Tree Node Abstraction.
 * <p>
 *     This API try to
 *     the well known <a href="https://en.wikipedia.org/wiki/Tree_(data_structure)">tree</a> data structure.
 * </p>
 * @param <T> value type
 * @author jTzipi
 */
public interface INode<T> {

    /**
     * Return parent node.
     * @return parent node or {@code null} if root
     */
    INode<T> getParent();

    /**
     * Return data.
     * @return tree node data
     */
    T getValue();

    /**
     * Return sub nodes.
     * @return all sub nodes of this node
     */
    List<INode<T>> getSubNodes();

    /**
     * Return sub nodes filtered.
     * @param predicate filter
     * @return sub nodes of this node filtered
     */
    List<INode<T>> getSubNodes( Predicate<? super T> predicate );

    /**
     * Return the depth of this node.
     * <p>
     *     Each node in a tree has a depth or level.
     *     The root has always depth 0.
     * </p>
     * @return depth of this node
     */
    int getDepth();

    /**
     * Is this tree node a leaf.
     * @return {@code true} if this node is a leaf that is has no sub nodes.
     */
    default boolean isLeaf() {

        return getSubNodes().isEmpty();
    }

    /**
     * NullValue.
     * @param <T> any type
     */
    final class NullNode<T> implements INode<T> {

        private NullNode() {

        }

        /**
         * Return new instance.
         * @return instance
         * @param <T> any type
         */
        public static<T> NullNode<T> instanceOf() {

            return new NullNode<>();
        }

        @Override
        public INode<T> getParent() {

            return null;
        }

        @Override
        public T getValue() {

            return null;
        }

        @Override
        public List<INode<T>> getSubNodes() {

            return Collections.emptyList();
        }

        @Override
        public List<INode<T>> getSubNodes( Predicate<? super T> predicate ) {

            return getSubNodes();
        }

        @Override
        public int getDepth() {

            return -1;
        }
    }
}
