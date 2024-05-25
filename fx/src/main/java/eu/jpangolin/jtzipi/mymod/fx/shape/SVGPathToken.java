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

package eu.jpangolin.jtzipi.mymod.fx.shape;

import eu.jpangolin.jtzipi.mymod.utils.ModUtils;
import javafx.scene.shape.*;


import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * SVG Path Token Enum.
 * <p>
 *     This is an enum of all known SVG(up to 1.2) path type acronym.
 * </p>
 * @author jTzipi
 */
public enum SVGPathToken implements Supplier<String> {

    /**
     * Move To Abs.
     */
    T_M("M" ),
    /**
     * Move To Rel.
     */
    T_m("m"),
    /**
     * Line To Abs.
     */
    T_L("L"),
    /**
     * Line To Rel.
     */
    T_l("l"),

    /**
     * Arc To Abs.
     */
    T_A("A"),
    /**
     * Atc To Rel.
     */
    T_a("a"),
    /**
     * Quad Curve To Abs.
     */
    T_Q( "Q"),
    /**
     * Quad Curve To Rel.
     */
    T_q( "q"),
    /**
     *
     */
    T_T("T"),

    T_t("t"),

    /**
     * Cubic Curve To Abs.
     */
    T_C("C"),

    /**
     * Cubic Curve To Rel.
     */
    T_c("c"),
    /**
     *
     */
    T_S("S"),

    T_s("s"),

    /**
     * Close Path.
     */
    T_Z("Z"),
    /**
     * Horizontal Line Abs.
     */
    T_H("H"),

    /**
     *
     */
    T_h("h"),

    T_V( "V"),

    T_v("v")
    ;


    private final String tok;
    SVGPathToken(String tokenStr) {
this.tok = tokenStr;
    }

    @Override
    public String get() {
        return tok;
    }


    /**
     * Create the Token and Attributes for all {@link PathElement}'s of path.
     * @param path path
     * @return SVG Token for path (like M10 10 L20 50 L50 50 L10 50 Z)
     * @see SVGPathToken#createSvgPathOf(Path, int)
     * @throws NullPointerException if {@code path}
     */
    public static String createSvgPathOf( javafx.scene.shape.Path path ) {
        return SVGPathToken.createSvgPathOf(path, DECIMAL_POINT_DEF);
    }

    /**
     * Create ab SVG path of the given path's {@link PathElement}'.
     * @param path path
     * @param decPoints decimal points rounded [-1 .. ] where &lt; 0 is unchanged
     * @return  SVG Token for path (like M10 10 L20 50 L50 50 L10 50 Z)
     * @see SVGPathToken#createSvgPathOf(Path)
     * @throws NullPointerException if {@code path}
     */
    public static String createSvgPathOf( javafx.scene.shape.Path path, int decPoints ) {
        return SVGPathToken.createSvgPathOf(Objects.requireNonNull(path).getElements(), decPoints);
    }

    /**
     * Create SVG Path for path elements.
     * @param pathElements path elements
     * @param decPoints rounding to {@code decPoints} [-1 .. ]. {@code decPoints} &lt 0 is unchanged format
     *
     * @return SVG Path or
     * @throws NullPointerException if {@code pathElements}
     * @throws IllegalArgumentException if {@code pathElements}[0] {@literal !instanceof MoveTo}.
     */
    public static String createSvgPathOf (List<PathElement> pathElements, int decPoints ) {
        Objects.requireNonNull(pathElements);
        if( pathElements.isEmpty() ) {

            return "M 0 0 Z";

        }
        if( !(pathElements.get(0) instanceof MoveTo )) {
            throw new IllegalArgumentException("The first token must be move to!");
        }

        decPoints = ModUtils.clamp(decPoints, -1, 5);
        StringBuilder sb = new StringBuilder();



        for( PathElement pa : pathElements ) {
            if( null == pa ) {

                continue;
            }
            if( pa instanceof MoveTo m ) {
                moveToPath(sb, decPoints, m);
            } else if (pa instanceof LineTo l) {
                lineToPath(sb, decPoints, l);
            } else if( pa instanceof ClosePath  ) {
closeToPath(sb);
            }
            else if (pa instanceof ArcTo a) {
                arcToPath(sb, decPoints, a);
            }           else if (pa instanceof  QuadCurveTo qc ) {
        quadCurveToPath(sb, decPoints, qc);
            } else if( pa instanceof CubicCurveTo cc ) {
                cubicCurveToPath(sb, decPoints, cc);
            } else {
                throw new IllegalArgumentException("Path Element[class='"+pa.getClass()+"'] not known!");
            }
        }



        return sb.toString();
    }

    private static void moveToPath( final StringBuilder sb, int nc, final MoveTo mt ) {

        double x = mt.getX();
        double y = mt.getX();
        if( nc >= 0) {
            x = ModUtils.round(x, nc);
            y = ModUtils.round(y, nc);
        }

        appendStrSup(sb, T_M);
        appendDouble(sb, x);
        appendDouble(sb, y);
    }

    private static void lineToPath( final StringBuilder sb, int nc, final LineTo mt ) {

        double x = mt.getX();
        double y = mt.getX();
        if( nc >= 0) {
            x = ModUtils.round(x, nc);
            y = ModUtils.round(y, nc);
        }

        appendStrSup(sb, T_L);
        appendDouble(sb, x);
        appendDouble(sb, y);
    }

    private static void closeToPath(StringBuilder sb ) {
        sb.append(T_Z.get()).append(DELIM);
    }

    private static void arcToPath( StringBuilder sb, int nc, ArcTo a ) {


        double radX = a.getRadiusX();
        double radY = a.getRadiusY();
        double xAxisR = a.getXAxisRotation();
        double x = a.getX();
        double y = a.getY();
        int sweep = a.isSweepFlag() ? 1 : 0;
        int large = a.isLargeArcFlag() ?1 : 0;

        if( nc >= 0 ) {

            radX = ModUtils.round(radX, nc);
            radY = ModUtils.round(radY, nc);
            xAxisR = ModUtils.round(xAxisR, nc);
            x = ModUtils.round(x, nc);
            y = ModUtils.round(y, nc);
        }

        appendStrSup(sb, T_A);
        appendDouble(sb, radX);
        appendDouble(sb, radY);
        appendDouble(sb, xAxisR);
        appendInt(sb, large);
        appendInt(sb, sweep);
        appendDouble(sb, x);
        appendDouble(sb, y);
    }

    private static void quadCurveToPath( StringBuilder sb, int nc, QuadCurveTo quadCurveTo ) {

        double x = quadCurveTo.getX();
        double y = quadCurveTo.getY();
        double cx = quadCurveTo.getControlX();
        double cy = quadCurveTo.getControlY();
        if( nc >= 0 ) {
            x = ModUtils.round(x, nc);
            y = ModUtils.round(y, nc);
            cx = ModUtils.round(cx, nc);
            cy = ModUtils.round(cy, nc);
        }

        appendStrSup(sb, T_C);
        appendDouble(sb, cx);
        appendDouble(sb, cy);
        appendDouble(sb, x);
        appendDouble(sb, y);
    }


    private static void cubicCurveToPath( StringBuilder sb, int nc, CubicCurveTo cubicCurveTo ) {

        double x = cubicCurveTo.getX();
        double y = cubicCurveTo.getY();
        double cx = cubicCurveTo.getControlX1();
        double cy = cubicCurveTo.getControlY1();
        double cx2 = cubicCurveTo.getControlX2();
        double cy2 = cubicCurveTo.getControlY2();
        if( nc >= 0 ) {
            x = ModUtils.round(x, nc);
            y = ModUtils.round(y, nc);
            cx = ModUtils.round(cx, nc);
            cy = ModUtils.round(cy, nc);
            cx2 = ModUtils.round(cx2, nc);
            cy2 = ModUtils.round(cy2, nc);
        }


        appendStrSup(sb,T_Q);
        appendDouble(sb, cx);
        appendDouble(sb, cy);
        appendDouble(sb, cx2);
        appendDouble(sb, cy2);
        appendDouble(sb, x);
        appendDouble(sb, y);
    }
    private static void appendDouble( StringBuilder sb, double d ) {
        sb.append(d).append(DELIM);
    }

    private static void appendInt ( StringBuilder sb, int i ) {
        sb.append(i).append(DELIM);
    }

    private static void appendStrSup( StringBuilder sb, Supplier<String>  supplier ) {
        sb.append(supplier.get());
    }

    public static final int DECIMAL_POINT_DEF = 2;
    private static final String DELIM = " ";
}
