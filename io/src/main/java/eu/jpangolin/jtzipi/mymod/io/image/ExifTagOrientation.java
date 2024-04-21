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
