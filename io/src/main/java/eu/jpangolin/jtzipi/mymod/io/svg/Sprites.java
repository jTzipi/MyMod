package eu.jpangolin.jtzipi.mymod.io.svg;

import eu.jpangolin.jtzipi.mymod.io.ModIO;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sprites.
 * <p>
 *     Here we can obtain svg 'sprites' from some providers like
 *     <a href="https://fontawesome.com" alt="Font Awesome" >Font Awesome</a>.
 * </p>
 */
public final class Sprites {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( Sprites.class );
    private static final String PROP_PROVIDER_NAME = "provider.properties";


    // Contains for each Sprite
    private static final EnumMap<SpritesProvider, Map<String, String>> SPRITE_LIB_MAP = new EnumMap<>( SpritesProvider.class );
    private static Properties SVG_PROP;


    private Sprites() {

    }

    /**
     * Return svg path from a sprite provider.
     *
     * @param sp    sprite provider
     * @param idStr id
     * @return svg path if found or {@link OwnSprites#SPRITE_NOT_FOUND}
     * @throws IOException          if the properties failed to load or the sprites can't be parsed
     * @throws NullPointerException if {@code sp}|{@code idStr} is nul
     */
    public String getPathFromProvider( SpritesProvider sp, String idStr ) throws IOException {
        Objects.requireNonNull( sp );
        if ( null == SVG_PROP ) {
            SVG_PROP = ModIO.loadPropertiesFromResource( Sprites.class, PROP_PROVIDER_NAME );
        }
        // Load the sprites
        if ( !SPRITE_LIB_MAP.containsKey( sp ) ) {

            parseSpriteLib( sp );
        }

        return SPRITE_LIB_MAP.get( sp ).getOrDefault( idStr, OwnSprites.SPRITE_NOT_FOUND.get() );
    }

    private static void parseSpriteLib( SpritesProvider sp ) throws IOException {
        LOG.info( "Load SVG Sprites for " + sp + "" );
        assert null != sp : "SpriteProvider must != null";

        // load file name
        String fileName = SVG_PROP.getProperty( sp.getFileName() );
        String regPath = SVG_PROP.getProperty( sp.getRegPath() );

        if ( null == fileName || null == regPath ) {
            throw new IllegalStateException( "Filename[="+fileName+"] or PathReg[='"+regPath+"'] not found!" );
        }
        String svgContent = ModIO.loadResource( Sprites.class, fileName, true ).toString();

        // pattern matcher
        Pattern regIdAndPath = Pattern.compile( regPath );
        Matcher matcher = regIdAndPath.matcher( svgContent );

        Map<String, String> spriteMap = new HashMap<>();

        while ( matcher.find() ) {

            String id = matcher.group( SpritesProvider.ID_PLACEHOLDER );
            String path = matcher.group( SpritesProvider.PATH_PLACEHOLDER );

            spriteMap.put( id, path );

        }
        SPRITE_LIB_MAP.put( sp, spriteMap );

    }


}