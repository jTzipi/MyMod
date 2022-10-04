package eu.jpangolin.jtzipi.mymod.io.svg;

/**
 * Svg sprite provider enum.
 * This store the information of the file and the regular exp. to find the svg d path
 */
public enum SpritesProvider {

    FONT_AWESOME_SOLID("svg.fontawesome.solid.file", "svg.fontawesome.solid.regex.path"),;

    private final String fileName;

    private final String regPath;

    /**
     * SpritesProvider.
     * @param propFileNameStr resource file name
     *
     * @param propRegExPath regular exp to find svg path
     */
    SpritesProvider( final String propFileNameStr,  String propRegExPath ) {
        this.fileName = propFileNameStr;

        this.regPath = propRegExPath;
    }

    /**
     * Return sprite file name relative to resources/svg path.
     * @return file name
     */
    public String getFileName(){
        return fileName;
    }

    /**
     * Return regular exp to find 'id' and path.
     * @return regular exp
     */
    public String getRegPath() {

        return regPath;
    }

    public static final String ID_PLACEHOLDER = "~";
    public static final String PATH_PLACEHOLDER = "d";
}