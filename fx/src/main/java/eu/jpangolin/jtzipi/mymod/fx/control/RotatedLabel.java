package eu.jpangolin.jtzipi.mymod.fx.control;


import eu.jpangolin.jtzipi.mymod.utils.ModUtils;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.util.Objects;

/**
 * Node wrapping other nodes(parent) which can be rotated.
 * <p>
 *     The idea is to wrap a node with a group to preserve the
 *     rotation in a layout manager.
 *     Details of the idea can be found <a href="">link</a>
 * </p>
 * @author jTzipi
 */
public class RotatedLabel extends Label {

    /**
     * Default rotation.
     */
    public static final double DEFAULT_ROTATION = -90D;

    private static final String FX_ROTATION_PROP = "FX_GRAPHIC_ROTATION_PROP";
    /**
     * Half lazy rotation property.
     */
    private DoubleProperty fxGraphicRotationProp;
    private double _rotation = DEFAULT_ROTATION;

    public RotatedLabel() {

    }

    /**
     * Set the node as a wrapped (rotated) label graphic.
     * @param node node
     * @param rotation rotation [{@linkplain ModUtils#MIN_ROTATION} .. {@linkplain ModUtils#MAX_ROTATION}]
     * @throws NullPointerException if {@code node}
     */
    public void putRotatedNode(final Node node, double rotation ) {
        Objects.requireNonNull(node, "You can not add null");

        // set value clamped
        setGraphicRotation(rotation);
        // set custom graphic rotation
        node.setRotate(rotation);
        // set as graphic
        setGraphic(new Group(node));
    }

    /**
     * Get rotation of wrapped node.
     * @return rotation
     */
    public final double getGraphicRotation() {
        double ret;
        if( null == fxGraphicRotationProp) {
            ret = _rotation;
        } else {
            ret = fxGraphicRotationProp.get();
        }
        return ret;
    }

    /**
     * Set rotation for the wrapped node.
     * @param rotation rotation [{@linkplain ModUtils#MIN_ROTATION} .. {@linkplain ModUtils#MAX_ROTATION}]
     */
    public final void setGraphicRotation(double rotation ) {
        _rotation = ModUtils.clamp(rotation, ModUtils.MIN_ROTATION, ModUtils.MAX_ROTATION);

        Node graphic = getGraphic();
        // if we already have wrapped node
        // unpack this node and rotate
        if( null != graphic ) {

            Group group = (Group)graphic;
            group.getChildren().get(0).setRotate(_rotation);
        }

        if( null != fxGraphicRotationProp ) {

            fxGraphicRotationProp.set(_rotation);
        }
    }

    /**
     * Get rotation property.
     * this is lazy
     * @return double property rotation
     */
    public DoubleProperty fxRotationProp() {
        if( null == fxGraphicRotationProp) {
            fxGraphicRotationProp = new SimpleDoubleProperty(this, FX_ROTATION_PROP, DEFAULT_ROTATION);
        }
        return fxGraphicRotationProp;
    }
}
