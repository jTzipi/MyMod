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

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Font Awesome SVG library.
 * <p>
 * For more
 * </p>
 */
public enum FontAwesomeProvider implements ISpriteProvider {

    /**
     * Solid style.
     */
    SOLID( "solid" ),
    /**
     * Regular style.
     */
    REGULAR( "regular" ),
    /**
     * Thin style.
     * Hint: this is only available if you buy 'pro'.
     */
    THIN( "thin" );

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( "FontAwesome SVG Provider" );
    final String type; // type
    final Map<String, String> svgMap = new HashMap<>(); // svg lib
    // --
    boolean loaded; // svg lib loaded

    /**
     * Font Awesome Icon Library.
     *
     * @param resourceType type
     */
    FontAwesomeProvider( String resourceType ) {
        this.type = "fontawesome." + resourceType;
    }

    @Override
    public String svgFor( String idStr ) {
        Objects.requireNonNull( idStr );
        if ( !loaded ) {
            init();
        }

        // -- Error during load
        if ( loaded && svgMap.isEmpty() ) {

            LOG.warn( "SVG File loaded but no sprites found?" );
            return SVG_DEFAULT;
        }

        // -- key not known
        if ( !svgMap.containsKey( idStr ) ) {
            LOG.warn( "Key '{}' not known!", idStr );
            return SVG_DEFAULT;
        }
        return svgMap.get( idStr );
    }

    private void init() {
        LOG.info( "init svg library for '{}'", this.name() );
        try {

            Resources.loadMap( type, svgMap );
            LOG.info( "... done" );
        } catch ( IOException ioE ) {

            LOG.error( "... Error during init!", ioE );
        }

        loaded = true;
    }

}
