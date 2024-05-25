/*
 * Copyright (c) 2024. Tim Langhammer
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

package eu.jpangolin.jtzipi.mymod.io.image;

public enum ExifTagOrientation {
    /**
     * Default-Normal.
     */
    HORIZONTAL( 1 ),
    /**
     * Spiegelt an der X Achse.
     */
    MIRROR_HORIZONTAL( 2 ),
    /**
     * Rotiert um 180°.
     */
    ROTATE_180( 3 ),
    /**
     * Spiegelt an der Y Achse.
     */
    MIRROR_VERTICAL( 4 ),
    /**
     * Spiegelt an der X Achse und dreht 90° Uhrzeiger.
     */
    MIRROR_HORIZONTAL_ROTATE_90_CW( 5 ),
    /**
     * Dreht um 90° Uhrzeiger.
     */
    ROTATE_90_CW( 6 ),
    /**
     * Spiegelt an der X Achse und dreht 270° Uhrzeiger.
     */
    MIRROR_HORIZONTAL_ROTATE_270_CW( 7 ),
    /**
     * Dreht um 270° Uhrzeiger.
     */
    ROTATE_270_CW( 8 );

    private final int val;

    ExifTagOrientation( int tagValue ) {

        this.val = tagValue;
    }

    /**
     * Return ExifTag for value.
     * @param tagValue type
     * @return the wanted Tag
     * @throws IllegalArgumentException if {@code tagValue} is not known
     */
    public static ExifTagOrientation of( int tagValue ) {

        for ( ExifTagOrientation tag : values() ) {
            if ( tag.getTagValue() == tagValue ) {
                return tag;
            }
        }


        throw new IllegalArgumentException( "Tag Value ist nicht gueltig (muss > 0 und < 9 sein )" );

    }

    /**
     * Return tag specification value.
     * @return tag value
     */
    public int getTagValue() {

        return val;
    }
}
