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