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

import eu.jpangolin.jtzipi.mymod.io.ModIO;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * ImageUtils.
 * <p>
 * This class borrow most functions from the {@code GraphicsUtilities} class written by Romain Guy.
 * All credits belong to him.
 * I only add null checks
 * </p>
 * <p><u>From the doc of GraphicsUtilities</u></p>
 * <p><code>GraphicsUtilities</code> contains a set of tools to perform
 * common graphics operations easily.
 * These operations are divided into
 * several themes, listed below.</p>
 * <p>
 * Compatible Images
 *
 * <p>Compatible images can, and should, be used to increase drawing
 * performance. This class provides a number of methods to load compatible
 * images directly from files or to convert existing images to compatibles
 * images.</p>
 * Creating Thumbnails
 * <p>This class provides a number of methods to easily scale down images.
 * Some of these methods offer a trade-off between speed and result quality and
 * should be used all the time. They also offer the advantage of producing
 * compatible images, thus automatically resulting into better runtime
 * performance.</p>
 * <p>All these methodes are both faster than
 * {@link java.awt.Image#getScaledInstance(int, int, int)} and produce
 * better-looking results than the various <code>drawImage()</code> methods
 * in {@link Graphics}, which can be used for image scaling.</p>
 * Image Manipulation
 * <p>This class provides two methods to get and set pixels in a buffered image.
 * These methods try to avoid unmanaging the image in order to keep good
 * performance.</p>
 *
 * @author Romain Guy <romain.guy@mac.com>
 * @author jTzipi
 */
public final class ImageUtils {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( ImageUtils.class );

    // cache for image reader
    private static final WeakHashMap<String, ImageReader> IMG_READER_MAP = new WeakHashMap<>();
    // Default device GraphicsConfiguration
    private static final GraphicsConfiguration CONFIGURATION =
            GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().getDefaultConfiguration();

    private ImageUtils() {

    }

    /**
     * Try to read the image dimension of an image.
     *
     * @param path path to image
     * @return image dimension or {@link }
     * @throws IOException          if {@code path} is not readable or no image reader can be found
     * @throws NullPointerException if {@code path} is null
     */
    public static ImageDimension getImageDimensionOf( final Path path ) throws IOException {

        Objects.requireNonNull( path );
        if ( !Files.isReadable( path ) ) {

            throw new IOException( "Path[='" + path + "'] not readable" );
        }

        final String sfx = ModIO.getFileNameSuffixSafe( path );

        LOG.debug( "... try to get image reader for '" + sfx + "' image type" );

        if ( !IMG_READER_MAP.containsKey( sfx ) ) {

            LOG.debug( "...No reader cached..." );
            Iterator<ImageReader> iri = ImageIO.getImageReadersBySuffix( sfx );

            if ( iri.hasNext() ) {
                IMG_READER_MAP.put( sfx, iri.next() );
                LOG.debug( "... found reader" );
            } else {

                throw new IOException( "No image reader found for image[='" + path + "']" );
            }
        }


        ImageReader ir = IMG_READER_MAP.get( sfx );

        return tryReadDim( path.toFile(), ir );
    }

    /**
     * Create a buffered image from swing icon.
     *
     * @param icon icon
     * @return buffered image
     */
    public static BufferedImage iconToBufferedImage( final javax.swing.Icon icon ) {

        Objects.requireNonNull( icon );
        final BufferedImage bufImg = createTranslucentCompatibleImage( icon.getIconWidth(), icon.getIconHeight() );
        icon.paintIcon( null, bufImg.createGraphics(), 0, 0 );

        return bufImg;
    }


    /**
     * <p>Returns a new <code>BufferedImage</code> using the same color model
     * as the image passed as a parameter. The returned image is only compatible
     * with the image passed as a parameter. This does not mean the returned
     * image is compatible with the hardware.</p>
     *
     * @param image the reference image from which the color model of the new
     *              image is obtained
     * @return a new <code>BufferedImage</code>, compatible with the color model
     * of <code>image</code>
     */
    public static BufferedImage createColorModelCompatibleImage( final BufferedImage image ) {

        Objects.requireNonNull( image );
        final ColorModel cm = image.getColorModel();
        return new BufferedImage( cm,
                cm.createCompatibleWritableRaster( image.getWidth(),
                        image.getHeight() ),
                cm.isAlphaPremultiplied(), null );
    }

    /**
     * <p>Returns a new compatible image with the same width, height and
     * transparency as the image specified as a parameter.</p>
     *
     * @param image the reference image from which the dimension and the
     *              transparency of the new image are obtained
     * @return a new compatible <code>BufferedImage</code> with the same
     * dimension and transparency as <code>image</code>
     * @see Transparency
     * @see #createCompatibleImage(int, int)
     * @see #createCompatibleImage(BufferedImage, int, int)
     * @see #createTranslucentCompatibleImage(int, int)
     * @see #loadCompatibleImage(URL)
     * @see #toCompatibleImage(BufferedImage)
     */
    public static BufferedImage createCompatibleImage( final BufferedImage image ) {

        return createCompatibleImage( image, image.getWidth(), image.getHeight() );
    }

    /**
     * <p>Returns a new compatible image of the specified width and height, and
     * the same transparency setting as the image specified as a parameter.</p>
     *
     * @param width  the width of the new image
     * @param height the height of the new image
     * @param image  the reference image from which the transparency of the new
     *               image is obtained
     * @return a new compatible <code>BufferedImage</code> with the same
     * transparency as <code>image</code> and the specified dimension
     * @see Transparency
     * @see #createCompatibleImage(BufferedImage)
     * @see #createCompatibleImage(int, int)
     * @see #createTranslucentCompatibleImage(int, int)
     * @see #loadCompatibleImage(URL)
     * @see #toCompatibleImage(BufferedImage)
     */
    public static BufferedImage createCompatibleImage( final BufferedImage image,
                                                       final int width, final int height ) {

        return CONFIGURATION.createCompatibleImage( width, height,
                image.getTransparency() );
    }

    /**
     * <p>Returns a new opaque compatible image of the specified width and
     * height.</p>
     *
     * @param width  the width of the new image
     * @param height the height of the new image
     * @return a new opaque compatible <code>BufferedImage</code> of the
     * specified width and height
     * @see #createCompatibleImage(BufferedImage)
     * @see #createCompatibleImage(BufferedImage, int, int)
     * @see #createTranslucentCompatibleImage(int, int)
     * @see #loadCompatibleImage(URL)
     * @see #toCompatibleImage(BufferedImage)
     */
    public static BufferedImage createCompatibleImage( final int width, final int height ) {


        return CONFIGURATION.createCompatibleImage( width, height );
    }

    /**
     * <p>Returns a new translucent compatible image of the specified width
     * and height.</p>
     *
     * @param width  the width of the new image
     * @param height the height of the new image
     * @return a new translucent compatible <code>BufferedImage</code> of the
     * specified width and height
     * @see #createCompatibleImage(BufferedImage)
     * @see #createCompatibleImage(BufferedImage, int, int)
     * @see #createCompatibleImage(int, int)
     * @see #loadCompatibleImage(URL)
     * @see #toCompatibleImage(BufferedImage)
     */
    public static BufferedImage createTranslucentCompatibleImage( final int width,
                                                                  final int height ) {

        return CONFIGURATION.createCompatibleImage( width, height,
                Transparency.TRANSLUCENT );
    }

    /**
     * <p>Returns a new compatible image from a URL. The image is loaded from the
     * specified location and then turned, if necessary into a compatible
     * image.</p>
     *
     * @param resource the URL of the picture to load as a compatible image
     * @return a new translucent compatible <code>BufferedImage</code> of the
     * specified width and height
     * @throws IOException if the image cannot be read or loaded
     * @see #createCompatibleImage(BufferedImage)
     * @see #createCompatibleImage(BufferedImage, int, int)
     * @see #createCompatibleImage(int, int)
     * @see #createTranslucentCompatibleImage(int, int)
     * @see #toCompatibleImage(BufferedImage)
     */
    public static BufferedImage loadCompatibleImage( final URL resource )
            throws IOException {

        final BufferedImage image = ImageIO.read( resource );
        return toCompatibleImage( image );
    }

    /**
     * <p>Return a new compatible image that contains a copy of the specified
     * image. This method ensures an image is compatible with the hardware,
     * and therefore optimized for fast blitting operations.</p>
     *
     * @param image the image to copy into a new compatible image
     * @return a new compatible copy, with the
     * same width and height and transparency and content, of <code>image</code>
     * @see #createCompatibleImage(BufferedImage)
     * @see #createCompatibleImage(BufferedImage, int, int)
     * @see #createCompatibleImage(int, int)
     * @see #createTranslucentCompatibleImage(int, int)
     * @see #loadCompatibleImage(URL)
     */
    public static BufferedImage toCompatibleImage( final BufferedImage image ) {

        if ( image.getColorModel().equals( CONFIGURATION.getColorModel() ) ) {
            return image;
        }

        final BufferedImage compatibleImage = CONFIGURATION.createCompatibleImage(
                image.getWidth(), image.getHeight(), image.getTransparency() );
        final Graphics g = compatibleImage.getGraphics();
        g.drawImage( image, 0, 0, null );
        g.dispose();

        return compatibleImage;
    }

    /**
     * <p>Returns a thumbnail of a source image. {@code newSize} defines
     * the length of the longest dimension of the thumbnail. The other
     * dimension is then computed according to the dimensions ratio of the
     * original picture.</p>
     * <p>This method favors speed over quality. When the new size is less than
     * half the longest dimension of the source image,
     * {@link #createThumbnail(BufferedImage, int)} or
     * {@link #createThumbnail(BufferedImage, int, int)} should be used instead
     * to ensure the quality of the result without sacrificing too much
     * performance.</p>
     *
     * @param image   the source image
     * @param newSize the length of the largest dimension of the thumbnail
     * @return a new compatible <code>BufferedImage</code> containing a
     * thumbnail of <code>image</code>
     * @throws IllegalArgumentException if <code>newSize</code> is larger than
     *                                  the largest dimension of <code>image</code> or &lt;= 0
     * @see #createThumbnailFast(BufferedImage, int, int)
     * @see #createThumbnail(BufferedImage, int)
     * @see #createThumbnail(BufferedImage, int, int)
     */
    public static BufferedImage createThumbnailFast( final BufferedImage image,
                                                     final int newSize ) {

        final float ratio;
        int width = image.getWidth();
        int height = image.getHeight();

        if ( width > height ) {
            if ( newSize >= width ) {
                throw new IllegalArgumentException( "newSize must be lower than" +
                        " the image width" );
            } else if ( newSize <= 0 ) {
                throw new IllegalArgumentException( "newSize must" +
                        " be greater than 0" );
            }

            ratio = ( float ) width / height;
            width = newSize;
            height = ( int ) ( newSize / ratio );
        } else {
            if ( newSize >= height ) {
                throw new IllegalArgumentException( "newSize must be lower than" +
                        " the image height" );
            } else if ( newSize <= 0 ) {
                throw new IllegalArgumentException( "newSize must" +
                        " be greater than 0" );
            }

            ratio = ( float ) height / width;
            height = newSize;
            width = ( int ) ( newSize / ratio );
        }

        final BufferedImage temp = createCompatibleImage( image, width, height );
        final Graphics2D g2 = temp.createGraphics();
        g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR );
        g2.drawImage( image, 0, 0, temp.getWidth(), temp.getHeight(), null );
        g2.dispose();

        return temp;
    }

    /**
     * <p>Returns a thumbnail of a source image.</p>
     * <p>This method favors speed over quality. When the new size is less than
     * half the longest dimension of the source image,
     * {@link #createThumbnail(BufferedImage, int)} or
     * {@link #createThumbnail(BufferedImage, int, int)} should be used instead
     * to ensure the quality of the result without sacrificing too much
     * performance.</p>
     *
     * @param image     the source image
     * @param newWidth  the width of the thumbnail
     * @param newHeight the height of the thumbnail
     * @return a new compatible <code>BufferedImage</code> containing a
     * thumbnail of <code>image</code>
     * @throws IllegalArgumentException if <code>newWidth</code> is larger than
     *                                  the width of <code>image</code> or if <code>newHeight</code> is larger
     *                                  than the height of <code>image</code> or if one of the dimensions
     *                                  is &lt;= 0
     * @see #createThumbnailFast(BufferedImage, int)
     * @see #createThumbnail(BufferedImage, int)
     * @see #createThumbnail(BufferedImage, int, int)
     */
    public static BufferedImage createThumbnailFast( final BufferedImage image,
                                                     final int newWidth, final int newHeight ) {

        if ( newWidth >= image.getWidth() ||
                newHeight >= image.getHeight() ) {
            throw new IllegalArgumentException( "newWidth and newHeight cannot" +
                    " be greater than the image" +
                    " dimensions" );
        } else if ( newWidth <= 0 || newHeight <= 0 ) {
            throw new IllegalArgumentException( "newWidth and newHeight must" +
                    " be greater than 0" );
        }

        final BufferedImage temp = createCompatibleImage( image, newWidth, newHeight );
        final Graphics2D g2 = temp.createGraphics();
        g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR );
        g2.drawImage( image, 0, 0, temp.getWidth(), temp.getHeight(), null );
        g2.dispose();

        return temp;
    }

    /**
     * <p>Returns a thumbnail of a source image. <code>newSize</code> defines
     * the length of the longest dimension of the thumbnail. The other
     * dimension is then computed according to the dimensions ratio of the
     * original picture.</p>
     * <p>This method offers a good trade-off between speed and quality.
     * The result looks better than
     * {@link #createThumbnailFast(BufferedImage, int)} when
     * the new size is less than half the longest dimension of the source
     * image, yet the rendering speed is almost similar.</p>
     *
     * @param image   the source image
     * @param newSize the length of the largest dimension of the thumbnail
     * @return a new compatible <code>BufferedImage</code> containing a
     * thumbnail of <code>image</code>
     * @throws IllegalArgumentException if <code>newSize</code> is larger than
     *                                  the largest dimension of <code>image</code> or &lt;= 0
     * @see #createThumbnailFast(BufferedImage, int, int)
     * @see #createThumbnailFast(BufferedImage, int)
     * @see #createThumbnail(BufferedImage, int, int)
     */
    public static BufferedImage createThumbnail( final BufferedImage image,
                                                 final int newSize ) {

        int width = image.getWidth();
        int height = image.getHeight();

        final boolean isWidthGreater = width > height;

        if ( isWidthGreater ) {
            if ( newSize >= width ) {
                throw new IllegalArgumentException( "newSize must be lower than" +
                        " the image width" );
            }
        } else if ( newSize >= height ) {
            throw new IllegalArgumentException( "newSize must be lower than" +
                    " the image height" );
        }

        if ( newSize <= 0 ) {
            throw new IllegalArgumentException( "newSize must" +
                    " be greater than 0" );
        }

        final float ratioWH = ( float ) width / height;
        final float ratioHW = ( float ) height / width;

        BufferedImage thumb = image;

        do {
            if ( isWidthGreater ) {
                width /= 2;
                if ( width < newSize ) {
                    width = newSize;
                }
                height = ( int ) ( width / ratioWH );
            } else {
                height /= 2;
                if ( height < newSize ) {
                    height = newSize;
                }
                width = ( int ) ( height / ratioHW );
            }


            final BufferedImage temp = createCompatibleImage( image, width, height );
            final Graphics2D g2 = temp.createGraphics();
            g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR );
            g2.drawImage( thumb, 0, 0, temp.getWidth(), temp.getHeight(), null );
            g2.dispose();

            thumb = temp;
        } while ( newSize != ( isWidthGreater ? width : height ) );

        return thumb;
    }

    /**
     * <p>Returns a thumbnail of a source image.</p>
     * <p>This method offers a good trade-off between speed and quality.
     * The result looks better than
     * {@link #createThumbnailFast(BufferedImage, int)} when
     * the new size is less than half the longest dimension of the source
     * image, yet the rendering speed is almost similar.</p>
     *
     * @param image     the source image
     * @param newWidth  the width of the thumbnail
     * @param newHeight the height of the thumbnail
     * @return a new compatible <code>BufferedImage</code> containing a
     * thumbnail of <code>image</code>
     * @throws IllegalArgumentException if <code>newWidth</code> is larger than
     *                                  the width of <code>image</code> or if <code>newHeight</code> is larger
     *                                  than the height of <code>image or if one the dimensions is not &gt; 0</code>
     * @see #createThumbnailFast(BufferedImage, int)
     * @see #createThumbnailFast(BufferedImage, int, int)
     * @see #createThumbnail(BufferedImage, int)
     */
    public static BufferedImage createThumbnail( final BufferedImage image,
                                                 final int newWidth, final int newHeight ) {

        int width = image.getWidth();
        int height = image.getHeight();

        if ( newWidth >= width || newHeight >= height ) {
            throw new IllegalArgumentException( "newWidth and newHeight cannot" +
                    " be greater than the image" +
                    " dimensions" );
        } else if ( newWidth <= 0 || newHeight <= 0 ) {
            throw new IllegalArgumentException( "newWidth and newHeight must" +
                    " be greater than 0" );
        }

        BufferedImage thumb = image;

        do {
            if ( width > newWidth ) {
                width /= 2;
                if ( width < newWidth ) {
                    width = newWidth;
                }
            }

            if ( height > newHeight ) {
                height /= 2;
                if ( height < newHeight ) {
                    height = newHeight;
                }
            }

            final BufferedImage temp = createCompatibleImage( image, width, height );
            final Graphics2D g2 = temp.createGraphics();
            g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR );
            g2.drawImage( thumb, 0, 0, temp.getWidth(), temp.getHeight(), null );
            g2.dispose();

            thumb = temp;
        } while ( width != newWidth || height != newHeight );

        return thumb;
    }

    /**
     * <p>Returns an array of pixels, stored as integers, from a
     * <code>BufferedImage</code>. The pixels are grabbed from a rectangular
     * area defined by a location and two dimensions. Calling this method on
     * an image of type different from <code>BufferedImage.TYPE_INT_ARGB</code>
     * and <code>BufferedImage.TYPE_INT_RGB</code> will unmanage the image.</p>
     *
     * @param img    the source image
     * @param x      the x location at which to start grabbing pixels
     * @param y      the y location at which to start grabbing pixels
     * @param w      the width of the rectangle of pixels to grab
     * @param h      the height of the rectangle of pixels to grab
     * @param pixels a pre-allocated array of pixels of size w*h; can be null
     * @return <code>pixels</code> if non-null, a new array of integers
     * otherwise
     * @throws IllegalArgumentException is <code>pixels</code> is non-null and
     *                                  of length &lt; w*h
     */
    public static int[] getPixels( final BufferedImage img,
                                   final int x, final int y, final int w, final int h, int[] pixels ) {

        if ( w == 0 || h == 0 ) {
            return new int[0];
        }

        if ( pixels == null ) {
            pixels = new int[w * h];
        } else if ( pixels.length < w * h ) {
            throw new IllegalArgumentException( "pixels array must have a length" +
                    " >= w*h" );
        }

        final int imageType = img.getType();
        if ( imageType == BufferedImage.TYPE_INT_ARGB ||
                imageType == BufferedImage.TYPE_INT_RGB ) {
            final Raster raster = img.getRaster();
            return ( int[] ) raster.getDataElements( x, y, w, h, pixels );
        }

        // Unmanages the image
        return img.getRGB( x, y, w, h, pixels, 0, w );
    }

    /**
     * <p>Writes a rectangular area of pixels in the destination
     * <code>BufferedImage</code>. Calling this method on
     * an image of type different from <code>BufferedImage.TYPE_INT_ARGB</code>
     * and <code>BufferedImage.TYPE_INT_RGB</code> will unmanage the image.</p>
     *
     * @param img    the destination image
     * @param x      the x location at which to start storing pixels
     * @param y      the y location at which to start storing pixels
     * @param w      the width of the rectangle of pixels to store
     * @param h      the height of the rectangle of pixels to store
     * @param pixels an array of pixels, stored as integers
     * @throws IllegalArgumentException is <code>pixels</code> is non-null and
     *                                  of length &lt; w*h
     */
    public static void setPixels( final BufferedImage img,
                                  final int x, final int y, final int w, final int h, final int[] pixels ) {

        if ( pixels == null || w == 0 || h == 0 ) {
            return;
        } else if ( pixels.length < w * h ) {
            throw new IllegalArgumentException( "pixels array must have a length" +
                    " >= w*h" );
        }

        final int imageType = img.getType();
        if ( imageType == BufferedImage.TYPE_INT_ARGB ||
                imageType == BufferedImage.TYPE_INT_RGB ) {
            final WritableRaster raster = img.getRaster();
            raster.setDataElements( x, y, w, h, pixels );
        } else {
            // Unmanages the image
            img.setRGB( x, y, w, h, pixels, 0, w );
        }
    }

    private static ImageDimension tryReadDim( final File file, final ImageReader imgRead ) {

        try ( final ImageInputStream iis = new FileImageInputStream( file ) ) {


            imgRead.setInput( iis );
            final int minIdx = imgRead.getMinIndex();
            final int width = imgRead.getWidth( minIdx );
            final int height = imgRead.getHeight( minIdx );


            return ImageDimension.of( width, height );
        } catch ( final IOException ioE ) {
            LOG.error( "Failed to read dimension ", ioE );
            return ImageDimension.EMPTY;
        } finally {
            // dispose resource
            imgRead.dispose();
        }

    }
}