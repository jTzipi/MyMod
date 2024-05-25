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

package eu.jpangolin.jtzipi.mymod.fx.control;

import eu.jpangolin.jtzipi.mymod.utils.ModUtils;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A Pane which align it's nodes vertical rotated.
 * <p>
 * TODO: VBox?
 */
public class VerticalPane extends Pane {

    /**
     * Rotate item counter-clockwise.
     */
    public static final double ROTATE_CCW = -90D;
    /**
     * Rate item clockwise.
     */
    public static final double ROTATE_CW = 90D;
    public static final double PANE_MIN_WIDTH = 9D;
    public static final double PANE_MAX_WIDTH = 547D;
    /**
     * Preferred width for nodes.
     */
    public static final double NODE_PREF_WIDTH = 27D;
    /**
     * Preferred height for nodes.
     */
    public static final double NODE_PREF_HEIGHT = 71D;
    static final double MIN_OFFSET = 0D;
    static final double MAX_OFFSET_X = 50D;
    static final double MAX_OFFSET_Y = 500D;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( VerticalPane.class );
    private double rot = ROTATE_CCW;
    private double xOff = 2D;
    private double yOff = 2D;
    private double yGap = 2D;
    private double maxWidth;
    private double nodePrefWidth;
    private double nodePrefHeight;
    private double maxW;

    /**
     * Default c.
     */
    public VerticalPane() {

        this( NODE_PREF_WIDTH, NODE_PREF_HEIGHT );
    }

    /**
     * Vertical Pane.
     *
     * @param width  width of pane
     * @param height height of pane
     */
    public VerticalPane( final double width, final double height ) {

        this.nodePrefHeight = ModUtils.clamp( height, USE_COMPUTED_SIZE, Double.MAX_VALUE );
        this.nodePrefWidth = ModUtils.clamp( width, USE_COMPUTED_SIZE, PANE_MAX_WIDTH );

        setPrefWidth( nodePrefWidth + xOff * 2D );
    }

    /**
     * Add a node to this pane.
     *
     * @param node region
     * @throws NullPointerException if {@code node}
     */
    public void addNode( final Region node ) {

        Objects.requireNonNull( node );

        // We need this label to put a group rotated
        // node
        // This way we can achieve layout as we want
        Label wrapper = new Label();
        wrapper.setGraphic( new Group( node ) );


        // Caution:
        // we need pref width to set to pref height because of rotating
        node.setRotate( rot );
        node.setPrefHeight( nodePrefWidth );
        node.setPrefWidth( nodePrefHeight );

        getChildren().add( wrapper );

        // set layout x and y
        relayout();
    }

    /**
     * Set offset x.
     *
     * @param offset offset clamped [
     */
    public void setOffsetX( final double offset ) {

        this.xOff = ModUtils.clamp( offset, MIN_OFFSET, MAX_OFFSET_X );

    }

    /**
     * Set offset y.
     *
     * @param offset offset clamped [
     */
    public void setOffsetY( final double offset ) {

        this.yOff = ModUtils.clamp( offset, MIN_OFFSET, MAX_OFFSET_Y );
    }

    private void relayout() {

        int size = getChildren().size() - 1;
        // no child
        if ( 0 > size ) {
            return;
        }
        // this is a label since we put this
        Label label = ( Label ) getChildren().get( size );

        double x = xOff;
        double y = yOff + yOff * size + nodePrefHeight * size;
        LOG.error( "x " + x + " y " + y );

        label.relocate( x, y );
    }

    private void relayoutAll() {


        for ( int i = 0; i < getChildren().size(); i++ ) {

            Group r = ( Group ) getChildren().get( i );
            // double nw = nc.getParent().prefWidth( USE_COMPUTED_SIZE );
            // double nh = nc.getParent().prefHeight( USE_COMPUTED_SIZE );


            double x = xOff;
            double y = yOff + yOff * i + NODE_PREF_HEIGHT * i;
            r.relocate( x, y );
        }

    }

}