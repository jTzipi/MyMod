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

package eu.jpangolin.jtzipi.mymod.io.image;

/**
 * Simple Value Object for image dimension.
 * <p>
 * Since there is no simple image dimension class  we create a simple value object for this.
 * <br>
 * This class is immutable. So thread safe.
 * </p>
 *
 * @author jTzipi
 */
public record ImageDimension(int width, int height) implements Comparable<ImageDimension> {

    /**
     * Empty Dimension that is a dimension with no length.
     * <p>
     * Width and Height are -1.
     * </p>
     */
    public static final ImageDimension EMPTY = new ImageDimension( -1, -1 );


    /**
     * Build new instance.
     *
     * @param width  width &gt; 0
     * @param height height &gt; 0
     * @return ImageDimension of specified size
     * @throws IllegalArgumentException if {@code width} or {@code height} are &lt; 0
     */
    public static ImageDimension of( final int width, final int height ) {
        if ( 0 >= width || 0 >= height ) {
            throw new IllegalArgumentException( "width[=" + width + "] or height[=" + height + "] are <= 0" );
        }

        return new ImageDimension( width, height );
    }


    @Override
    public int compareTo( final ImageDimension imageDimension ) {

        final int area = height * width;
        final int other = imageDimension.width * imageDimension.height;
        return EMPTY == this
                ? -1
                : EMPTY == imageDimension
                ? 1
                : Integer.compare( area, other );
    }
}
