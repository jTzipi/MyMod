/*
 * Copyright (c) 2022-2024. Tim Langhammer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package eu.jpangolin.jtzipi.mymod.io.svg;

/**
 * Provider for SVG 'Sprites'.
 */
public interface ISpriteProvider {


    /**
     * Default SVG.
     * <p>This is a question mark in a circle</p>
     */
    String SVG_DEFAULT = "M31.783-.158C14.671-.158.756 13.76.756 30.872c0 17.112 13.915 31.027 31.027 31.027 17.113 0 31.03-13.915 31.03-31.028 0-17.112-13.917-31.029-31.03-31.029zm0 4c14.951 0 27.03 12.079 27.03 27.03 0 14.95-12.079 27.027-27.03 27.027-14.95 0-27.027-12.077-27.027-27.028 0-14.95 12.076-27.029 27.027-27.029zm6.91 11.824q1.363 1.438 2.008 3.249.67 1.786.67 3.894 0 4.59-2.927 7.491-2.927 2.902-8.037 3.498v5.482h-2.282v-9.401q1.563-.124 3.1-.497 1.539-.396 2.754-1.265 1.265-.917 1.96-2.207.72-1.315.72-3.646 0-3.721-1.588-5.706-1.563-1.984-4.59-1.984-1.14 0-2.008.347-.844.323-1.24.62.123.596.322 1.712.223 1.116.223 1.712 0 .917-.794 1.686-.793.744-2.307.744-1.339 0-1.91-.917-.57-.918-.57-2.134 0-.992.546-1.934.545-.968 1.711-1.86.992-.794 2.68-1.365 1.71-.57 3.546-.57 2.63 0 4.663.843 2.034.818 3.35 2.208zm-5.954 30.112q0 1.39-1.017 2.382-.992.967-2.381.967-1.39 0-2.406-.967-.993-.993-.993-2.382 0-1.389.993-2.38 1.017-1.018 2.406-1.018 1.389 0 2.381 1.017 1.017.992 1.017 2.381zM160.5 1.207 238.793 79.5H172c-6.351 0-11.5-5.149-11.5-11.5V1.207Z";

    /**
     * Return 'SVG path' for id.
     *
     * @param idStr id
     * @return path of svg
     * @throws NullPointerException if {@code idStr} is null
     */
    String svgFor( String idStr );

    /**
     * Return svg with prefix 'path://'.
     *
     * @param idStr id of svg
     * @return svg with prefix 'path://'
     * @throws NullPointerException if {@code idStr} is null
     */
    default String svgPathFor( String idStr ) {
        return "path://" + svgFor( idStr );
    }
}
