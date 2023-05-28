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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ModUtils Test.
 *
 * @author jTzipi
 */
class ModUtilsTest {


    @DisplayName("Check the upper bound is honored")
    @Test
    void clampValidAboveUpperBound() {

        double test = 22.2212D;
        double min = 0D;
        double max = 21D;

        double clamp = ModUtils.clamp( test, min, max );
        assertEquals( max, clamp, 0D );

    }

    @DisplayName("Check the lower bound is honored")
    @Test
    void clampValidAboveLowerBound() {

        double test = -12D;
        double min = 0D;
        double max = 21D;

        double clamp = ModUtils.clamp( test, min, max );
        assertEquals( min, clamp, 0D );

    }

    @DisplayName("Check data is valid range")
    @Test
    void clampValid() {

        double test = 12D;
        double min = 0D;
        double max = 21D;

        double clamp = ModUtils.clamp( test, min, max );
        assertEquals( test, clamp, 0D );

    }
}