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

package eu.jpangolin.jtzipi.mymod.io;

import eu.jpangolin.jtzipi.mymod.utils.ModUtils;

public class Chars {

    public static final int MIN = 0;
    public static final int UNICODE_MAX = 1114111;
    public static final int UNICODE_LEVEL_ZERO_MAX = 65518;


    public enum UnicodeRange {

        NUMBER_CIRCLED(9312, 20),
        ROMAN_LETTERS(545, 23),
        ;

        UnicodeRange( int offset, int max ) {

        }
    }

    public static String toUrlEncode( int codepoint ) {
        codepoint = ModUtils.clamp( codepoint, MIN, UNICODE_MAX );
        return "%"+ Integer.toHexString(codepoint);
    }

    public static String toHtmlHex( int codepoint ) {
        codepoint = ModUtils.clamp(codepoint, MIN, UNICODE_MAX);
        return "&#x" + Integer.toHexString(codepoint) + ";";
    }
    /**
     * Return hexadecimal html code for a codepoint.
     * @param codepoint codepoint [{} .. {}]
     * @return hex html code point
     */
    public static String toHtmlDec( int codepoint ) {
        codepoint = ModUtils.clamp(codepoint, MIN, UNICODE_MAX);
        return "&#" + codepoint + ";";
    }
}
