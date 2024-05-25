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

import java.util.function.Supplier;

public enum PathTypeRegEx implements Supplier<String> {

    /**
     * Any file not matching other regular exp.
     */
    PLAIN(""),

    TEXT("^.*\\.(txt|dat|rtf)$"),
    /**
     * Java file.
     */
;
private final String regEx;
    PathTypeRegEx(final String regExStr) {
this.regEx = regExStr;
    }

    @Override
    public String get() {
        return regEx;
    }

}
