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

package eu.jpangolin.jtzipi.mymod.fx.shape;

import eu.jpangolin.jtzipi.mymod.utils.IBuilder;
import eu.jpangolin.jtzipi.mymod.utils.ModUtils;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;

import java.util.List;
import java.util.Objects;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 *
 */
public final class PathSetupFX implements UnaryOperator<Path> {

    public static final float MAX_MITER_LIMIT = 1000F;
    public static final float MIN_MITER_LIMIT = 1F;
    public static final float DEF_MITER_LIMIT = 10F;
    /**
     * Default Stroke Line Join .
     */
    public static final StrokeLineJoin DEF_STROKE_LINE_JOIN = StrokeLineJoin.ROUND;
    public static final StrokeType DEF_STROKE_TYPE = StrokeType.CENTERED;

    public static final StrokeLineCap DEF_STROKE_LINE_CAP = StrokeLineCap.ROUND;
    public static final FillRule DEF_FILL_RULE = FillRule.EVEN_ODD;

    public static final Paint DEF_FILL_PAINT = Color.gray(0.7D);

    /**
     * Default Stroke Paint.
     */
    public static final Paint DEF_STROKE_PAINT = Color.BLACK;
    public static final float DEF_STROKE_WIDTH = 1F;
    public static final float MIN_STROKE_WIDTH = 0.01F;
    public static final float MAX_STROKE_WIDTH = 1000F;
    private float miterLimit = DEF_MITER_LIMIT;
    private StrokeLineJoin strokeLineJoin = DEF_STROKE_LINE_JOIN;    // Stroke line join
    private StrokeType strokeType = DEF_STROKE_TYPE;         // type
    private StrokeLineCap strokeCap = DEF_STROKE_LINE_CAP;   // line cap
    private Paint fill = Color.gray(0.7D);                // fill property
    private Paint strokeFill = DEF_STROKE_PAINT;
    private FillRule fillRule = DEF_FILL_RULE;
    private boolean smooth = true;                          // smooth property
    private float sw = DEF_STROKE_WIDTH;                                 // stroke width
    private List<Double> strokeDashL = null;

    PathSetupFX() {

    }
    /**
     * Set the fill rule.
     *
     * @param fillRule rule
     * @return {@code this}
     */
    public PathSetupFX fillRule(final FillRule fillRule) {
        this.fillRule = null == fillRule ? DEF_FILL_RULE : fillRule;

        return this;
    }

    /**
     * Set stroke with.
     *
     * @param strokeWidth stroke width [{@link #MIN_STROKE_WIDTH} .. {@link #MAX_STROKE_WIDTH}
     * @return {@code this}
     */
    public PathSetupFX strokeWidth(final float strokeWidth) {
        this.sw = ModUtils.clamp(strokeWidth, MIN_STROKE_WIDTH, MAX_STROKE_WIDTH);

        return this;
    }

    /**
     * Set stroke type.
     *
     * @param strokeType type (if {@code null} we set
     * @return {@code this}
     */
    public PathSetupFX strokeType(final StrokeType strokeType) {
        this.strokeType = null == strokeType ? DEF_STROKE_TYPE : strokeType;
        return this;
    }

    /**
     * Stroke line join.
     *
     * @param join stroke join
     * @return {@code this}
     */
    public PathSetupFX strokeJoin(final StrokeLineJoin join) {
        this.strokeLineJoin = null == join ? DEF_STROKE_LINE_JOIN : join;
        return this;
    }

    /**
     * Set stroke line cap.
     *
     * @param lineCap line cap
     * @return {@code this}
     */
    public PathSetupFX strokeCap(final StrokeLineCap lineCap) {
        this.strokeCap = null == lineCap ? DEF_STROKE_LINE_CAP : lineCap;
        return this;
    }

    /**
     * Set miter limit.
     *
     * @param miterLimit miter limit
     * @return {@code this}
     */
    public PathSetupFX miterLimit(final float miterLimit) {
        this.miterLimit = ModUtils.clamp(miterLimit, MIN_MITER_LIMIT, MAX_MITER_LIMIT);
        return this;
    }

    /**
     * Set stroke dash array.
     *
     * @param dash dash spot
     * @return {@code this}
     */
    public PathSetupFX strokeDashArray(Double... dash) {
        strokeDashL = null == dash ? List.of() : Stream.of(dash).filter(Objects::nonNull).toList();
        return this;
    }

    /**
     * Set stroke fill paint.
     *
     * @param strokeFill paint for stroke
     * @return {@code this}
     */
    public PathSetupFX strokeFill(final Paint strokeFill) {
        this.strokeFill = null == strokeFill ? DEF_STROKE_PAINT : strokeFill;
        return this;
    }

    /**
     * Set the fill of the path.
     *
     * @param fill fill paint
     * @return {@code this}
     */
    public PathSetupFX fill(final Paint fill) {
        this.fill = null == fill ? DEF_FILL_PAINT : fill;
        return this;
    }

    public PathSetupFX smooth(boolean prop ) {
        this.smooth = prop;

        return this;
    }
    /**
     * Reset this builder.
     * @return {@code this}
     */
    public PathSetupFX reset() {

        return this;
    }


    @Override
    public Path apply( Path path ) {
        path.setStrokeType(strokeType);
        path.setStrokeLineJoin(strokeLineJoin);
        path.setStrokeWidth(sw);
        path.setStroke(strokeFill);
        path.setFillRule(fillRule);
        path.setSmooth(smooth);
        path.setFill(fill);
        path.setStrokeLineCap(strokeCap);

        if (null == strokeDashL) {
            path.getStrokeDashArray().setAll(strokeDashL);
        }

        return path;
    }
}