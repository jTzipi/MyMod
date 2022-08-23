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

package eu.jpangolin.jtzipi.mymod.io.image;

import eu.jpangolin.jtzipi.mymod.io.ModIO;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * Some image
 */
public final class ImageUtils {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( ImageUtils.class );
    // cache for image reader
    private static final WeakHashMap<String, ImageReader> IMG_READER_MAP = new WeakHashMap<>();
    private ImageUtils() {

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
