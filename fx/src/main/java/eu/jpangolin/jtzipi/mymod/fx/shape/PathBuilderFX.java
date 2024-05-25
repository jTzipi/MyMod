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

import eu.jpangolin.jtzipi.mymod.utils.IBuilder;

import javafx.scene.shape.*;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * PathBuilder create a Shape from {@link PathElement}.
 * <p>
 * We cover the SVG path commands.
 * <br/>
 *  <ul>
 *      <li>Move To</li>
 *      <li>Line To</li>
 *      <li>Arc To</li>
 *      <li>Quad Curve To</li>
 *      <li>Cubic Curve To</li>
 *      <li>Close Path</li>
 *  </ul>
 *
 *  For more info about SVG path look <a href="https://www.w3.org/TR/SVG11/paths.html">this</a> page.
 * </p>
 * @author jTzipi
 */
public class PathBuilderFX implements IBuilder<javafx.scene.shape.Path> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PathBuilderFX.class);
    /**
     * Default Start Position.
     */
    public static final float DEF_START_POS = 0F;
    private final List<PathElement> pelementL = new ArrayList<>();
    private float lastX;
    private float lastY;

    private PathSetupFX setup;

    PathBuilderFX(final float xStart, final float yStart) {
        this.lastX = xStart;
        this.lastY = yStart;
        this.pelementL.add(new MoveTo(lastX, lastY));
    }

    PathBuilderFX() {
        this(  DEF_START_POS, DEF_START_POS);
    }



    /**
     * Create a PathBuilder with start position.
     *
     * @param xStart x start pos
     * @param yStart y start pos
     * @return PathBuilder
     */
    public static PathBuilderFX of(final float xStart, final float yStart) {
        return new PathBuilderFX(  xStart, yStart);
    }

    /**
     * Create a PathBuilder with default start position([{@link #DEF_START_POS},{@link #DEF_START_POS}].
     *
     * @return PathBuilder
     */
    public static PathBuilderFX create() {
        return new PathBuilderFX();
    }

    /**
     * Set a path setup object.
     * @param pathSetupFX path setup
     * @return {@code this}
     */
    public PathBuilderFX setup( PathSetupFX pathSetupFX ) {
        this.setup =pathSetupFX;
        return this;
    }

    /**
     * Create Line to x.
     * <p>Here we use the old y pos</p>
     *
     * @param x x
     * @return {@code this}
     */
    public PathBuilderFX lx(final float x) {
        this.pelementL.add(new LineTo(x, lastY));
        this.lastX = x;
        return this;
    }

    /**
     * Create a line from the last position to [old_x, y].
     *
     * @param y new y position
     * @return {@code this}
     */
    public PathBuilderFX ly(final float y) {
        this.pelementL.add(new LineTo(lastX, y));
        this.lastY = y;
        return this;
    }

    /**
     * Append a line to [x,y].
     *
     * @param x x pos
     * @param y y pos
     * @return {@code this}
     */
    public PathBuilderFX lxy(final float x, final float y) {
        this.pelementL.add(new LineTo(x, y));
        this.lastX = x;
        this.lastY = y;
        return this;
    }

    /**
     * Move the position to x.
     * Staying with the old y.
     *
     * @param x position x
     * @return {@code this}
     */
    public PathBuilderFX mx(final float x) {
        this.pelementL.add(new MoveTo(x, lastY));
        this.lastX = x;
        return this;
    }

    /**
     * Move the position to y.
     * Staying with the old x.
     *
     * @param y position to move y
     * @return {@code this}
     */
    public PathBuilderFX my(final float y) {
        this.pelementL.add(new MoveTo(lastX, y));
        this.lastY = y;
        return this;
    }

    /**
     * Move the pen to [x,y].
     *
     * @param x x pos
     * @param y y pos
     * @return {@code this}
     */
    public PathBuilderFX mxy(final float x, final float y) {
        this.pelementL.add(new MoveTo(x, y));
        this.lastX = x;
        this.lastY = y;

        return this;
    }

    /**
     * Append close path.
     * <p>This should be the last path segment</p>
     *
     * @return {@code this}
     */
    public PathBuilderFX close() {
        this.pelementL.add(new ClosePath());
        return this;
    }

    /**
     * Append an arc.
     *
     * @param circleX     circle x
     * @param circleY     circle y
     * @param xAxisRotDeg x-axis rotation
     * @param x           x pos
     * @param y           y pos
     * @param large       large
     * @param sweep       sweep
     * @return {@code this}
     */
    public PathBuilderFX arcTo(final float circleX, final float circleY, final float xAxisRotDeg, final float x, final float y, boolean large, boolean sweep) {
        this.pelementL.add(new ArcTo(circleX, circleY, xAxisRotDeg, x, y, large, sweep));
        this.lastX = x;
        this.lastY = y;
        return this;
    }

    /**
     * Append a cubic curve.
     *
     * @param cx1 control point x1
     * @param cy1 control point y1
     * @param cx2 control point x2
     * @param cy2 control point y2
     * @param x   x
     * @param y   y
     * @return {@code this}
     */
    public PathBuilderFX cubicTo(final float cx1, final float cy1, final float cx2, final float cy2, final float x, final float y) {
        this.pelementL.add(new CubicCurveTo(cx1, cy1, cx2, cy2, x, y));
        this.lastX = x;
        this.lastY = y;
        return this;
    }

    /**
     * Append a quad curve.
     *
     * @param x    x pos
     * @param y    y pos
     * @param ctrX control x
     * @param ctrY control y
     * @return {@code this}
     */
    public PathBuilderFX quadTo(final float x, final float y, final float ctrX, final float ctrY) {
        this.pelementL.add(new QuadCurveTo(ctrX, ctrY, x, y));
        this.lastY = y;
        this.lastX = x;
        return this;
    }

    @Override
    public Path build() {

        // put all path element
        Path path = new Path(pelementL);
        LOG.info("Path Pre Setup '{}'",  path );
        // and if set make path setup
        if (null != setup) {
            setup.apply(path);
            LOG.info("Path post setup '{}'", path);
        }
        return path;
    }
}
