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

package eu.jpangolin.jtzipi.mymod.utils;

/**
 * Regular Expression.
 *
 * @author jTzipi
 */
public enum RegEx {

    /**
     * Find 'rgb(..,..,..)' color code.
     * Group name are
     * <ul>
     *     <li>red: red</li>
     *     <li>green: green</li>
     *     <li>blue: blue</li>
     * </ul>
     */
    COLOR_STYLE_RGB_INT( RegEx.RGB_STYLE_INT ),

    /**
     * Find 'rgb( ..,..,.. )' color code.
     */
    COLOR_STYLE_RGB_FLOAT( RegEx.RGB_STYLE_FLOAT ),
    ;


    /**
     * parsing 1-3 digits lazy.
     */
    static final String DIGITS_MIN_1_MAX_3_LAZY = "\\d{1,3}?";
    static final String DIGITS_0_TO_1_FLOAT_LAZY = "0?|0\\.[0-9]*?|1\\.0?|1?";
    static final String RGB_STYLE_FLOAT = "^rgb\\((?<red>" + RegEx.DIGITS_0_TO_1_FLOAT_LAZY + "),(?<green>" + RegEx.DIGITS_0_TO_1_FLOAT_LAZY + "),(?<blue>" + RegEx.DIGITS_0_TO_1_FLOAT_LAZY + ")\\)$";
    static final String RGB_STYLE_INT = "^rgb\\((?<red>" + RegEx.DIGITS_MIN_1_MAX_3_LAZY + "),(?<green>" + RegEx.DIGITS_MIN_1_MAX_3_LAZY + "),(?<blue>" + RegEx.DIGITS_MIN_1_MAX_3_LAZY + ")\\)$";
    private final String regEx;

    RegEx( String regStr ) {
        this.regEx = regStr;
    }

    public String getRegEx() {
        return regEx;
    }
}