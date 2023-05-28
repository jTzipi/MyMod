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

package eu.jpangolin.jtzipi.mymod.utils;


/**
 * Simple number range.
 * @param min min value
 * @param max max value
 * @param inclusive max inclusive
 * @param <T> sub
 */
public record Range<T extends Number & Comparable<? super T>>( T min, T max, boolean inclusive ) {

    /**
     * Range Con.
     * @param min min value
     * @param max max value
     * @param inclusive max inclusive
     */
    public Range {
        if (min.compareTo(max) >= 0) {
            throw new IllegalArgumentException("min[''] is < or <= max['']");
        }
    }
}
