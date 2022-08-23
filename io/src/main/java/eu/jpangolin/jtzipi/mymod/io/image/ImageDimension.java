/*
 * Copyright (c) 2021 Tim Langhammer
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
public final class ImageDimension implements Comparable<ImageDimension> {

    /**
     * Empty Dimension that is a dimension with no length.
     * <p>
     *     Width and Height are -1.
     * </p>
     */
    public static final ImageDimension EMPTY = new ImageDimension();

    private final int width;
    private final int height;

    /**
     * Image dimension.
     *
     * @param imageWidth width of image
     * @param imageHeight height of image
     */
    ImageDimension( final int imageWidth, final int imageHeight ) {
        this.width = imageWidth;
        this.height = imageHeight;
    }

    /**
     * No Access.
     */
    private ImageDimension() {
this(-1, -1 );
    }

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

    /**
     * Width of image.
     * @return image width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Height of image.
     * @return image height
     */
    public int getHeight() {
        return this.height;
    }

    @Override
    public int hashCode() {
        int result = getWidth();
        result = 31 * result + getHeight();
        return result;
    }

    @Override
    public boolean equals( final Object other ) {
        if ( other == this ) {
            return true;
        }
        if ( !( other instanceof ImageDimension ) ) {
            return false;
        }
        final ImageDimension idim = ( ImageDimension ) other;
        return getWidth() == idim.getWidth() &&
                getHeight() == idim.getHeight();
    }

    @Override
    public String toString() {
        return "ImageDimension{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }

    @Override
    public int compareTo( final ImageDimension imageDimension ) {

        final int area = this.getHeight() * this.getWidth();
        final int other = imageDimension.getHeight() * imageDimension.getWidth();
        return Integer.compare( area, other );
    }
}
