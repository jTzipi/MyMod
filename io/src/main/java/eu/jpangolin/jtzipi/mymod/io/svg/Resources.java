/*
 * Copyright (c) 2022 MBJS Brandenburg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.jpangolin.jtzipi.mymod.io.svg;

import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Resources util class.
 * <p>Provides access to svg and other</p>
 */
final class Resources {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( Resources.class );
    private static final String PROP_SVG = "svg.";
    private static final String PROP_FILE = ".file";
    private static final String PROP_REGEX = ".regex";
    private static final String PROP_DIR = ".dir";
    private static Properties PROP;

    private Resources() {

        throw new AssertionError( "!" );
    }


    private static void loadProp() throws IOException {

        PROP = new Properties();
        try ( InputStream is = Resources.class.getResourceAsStream( "svg.properties" ) ) {

            PROP.load( is );
        }

    }

    private static boolean isNotPropLoaded() {

        return null == PROP;
    }

    private static String getProp( String propKey ) throws IOException {

        if ( isNotPropLoaded() ) {

            loadProp();
        }

        return PROP.getProperty( propKey );
    }

    private static String joinProperty( String prop, String suffix ) {
        return PROP_SVG + prop + suffix;
    }

    private static void parseSvg( String regex, String svgFile, Map<String, String> map ) {
        Matcher matcher = Pattern.compile( regex ).matcher( svgFile );

        //
        // look for id and d value -> SVG Path
        //
        while ( matcher.find() ) {
            String pathId = matcher.group( "id" );
            String d = matcher.group( "d" );

            // LOG.info( "Symbol '{}' found ", pathId );
            map.put( pathId, d );
        }

    }

    private static String readFile( String location ) throws IOException {

        Objects.requireNonNull( location );

        String ret;

        try ( InputStream is = Resources.class.getResourceAsStream( location ); ) {

            if ( null != is ) {

                StringBuilder sb = new StringBuilder();

                try ( InputStreamReader isr = new InputStreamReader( is ); BufferedReader buf = new BufferedReader( isr ) ) {

                    while ( buf.ready() ) {
                        sb.append( buf.readLine() ).append( "\n" );
                    }

                    ret = sb.toString();

                } catch ( final IOException ioE ) {

                    LOG.info( "Error while ", ioE );
                    throw ioE;
                }
            } else {
                LOG.error( "Error file '{}' not readable!", location );
                throw new IllegalStateException( "Failed to locate '" + location + "'" );
            }


        } catch ( IOException ioE ) {
            LOG.warn( "Failed to load '{}'", location, ioE );
            throw ioE;
        }
        return ret;

    }

    static void loadMap( String svgType, Map<String, String> map ) throws IOException {

        if ( isNotPropLoaded() ) {
            loadProp();
        }


        // -- properties
        String propFile = joinProperty( svgType, PROP_FILE );
        String propRegEx = joinProperty( svgType, PROP_REGEX );


        // -- values
        String file = getProp( propFile );
        // String regexId = getProp( propId );
        String regex = getProp( propRegEx );

        LOG.info( "Try to read file '{}' parsing  path '{}'", file, regex );

        if ( null == file || null == regex ) {
            throw new IOException( "Failed to read file=[='" + file + "'] or regex[='" + regex + "'] from properties!" );
        }

        final String svg = readFile( file );
        parseSvg( regex, svg, map );
    }

    static void svgFromDir( String type, String name, Map<String, String> cache ) throws IOException {

        if ( isNotPropLoaded() ) {
            loadProp();
        }

        String keyDir = joinProperty( type, PROP_DIR );
        String keyRegEx = joinProperty( type, PROP_REGEX );
        String dir = getProp( keyDir );
        String regEx = getProp( keyRegEx );

        if ( null == regEx || null == dir ) {
            throw new IOException( "Failed to read dir for key[='" + keyDir + "'] and or regex[='" + keyRegEx + "']from properties!" );
        }

        String file = dir + "/" + name + PROP_SVG;
        LOG.info( "try to load svg = '{}' parsing '{}'", file, regEx );

        String svg = readFile( file );
        LOG.info( "Svg = '{}'", svg );

        parseSvg( regEx, svg, cache );
    }
}
