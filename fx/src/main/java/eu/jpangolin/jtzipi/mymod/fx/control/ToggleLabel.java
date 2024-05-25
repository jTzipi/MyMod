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


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.Effect;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 * A label with ability to toggle.
 *
 * @author jTzipi
 */
public class ToggleLabel extends Label {

    private static final CornerRadii CORNER_RADII_DEF = new CornerRadii( 5D );
    private final BooleanProperty fxToggledProp = new SimpleBooleanProperty( this, "FX_TOGGLED_PROP", false );
    // -- colors --
    // TODO: setter
    private final Color colorToggle = Color.rgb( 157, 157, 254 );
    private final Color colorHover = Color.rgb( 197, 197, 254 );
    private final Background origBG;


    private final Background toggleBG = new Background( new BackgroundFill( colorToggle, CornerRadii.EMPTY, Insets.EMPTY ) );
    private final Background hoverBG = new Background( new BackgroundFill( colorHover, CornerRadii.EMPTY, Insets.EMPTY ) );
    /**
     * Background Hover property. Lazy init.
     */
    private ObjectProperty<Background> fxBGHoverProp;
    private ObjectProperty<Background> fxBackgroundToggledProp;
    private ObjectProperty<Border> fxBorderHoverProp;
    private ObjectProperty<Border> fxBorderToggledProp;

    private Effect toggleEffect;


    /**
     * Delegating to Label.
     *
     * @param textStr text
     */
    public ToggleLabel( String textStr ) {

        this( textStr, null );
    }

    /**
     * Toggle Label C.
     *
     * @param textStr text
     * @param graphics graphics
     */
    public ToggleLabel( String textStr, Node graphics ) {

        super( textStr, graphics );
        this.origBG = getBackground();

        this.setTextAlignment( TextAlignment.CENTER );
        this.setAlignment( Pos.CENTER );
        // setBorder( roundedBorder );
        initListener();
    }


    /**
     * Ret toggle property.
     *
     * @return toggled property
     */
    public final BooleanProperty getFXToggleProp() {

        return fxToggledProp;
    }

    /**
     * Is this label toggled.
     *
     * @return toggled or not
     */
    public final boolean isToggled() {

        return getFXToggleProp().get();
    }

    /**
     * Set toggle state.
     *
     * @param toggled toggle
     */
    public final void setToggled( final boolean toggled ) {

        getFXToggleProp().setValue( toggled );
    }

    /**
     * Return toggle label background prop.
     *
     * @return toggle background prop
     */
    public final ObjectProperty<Background> getFXBackgroundHoverProp() {

        if ( null == fxBGHoverProp ) {
            this.fxBGHoverProp = new SimpleObjectProperty<>( this, "FX_BACKGROUND_PROP", Background.EMPTY );
        }
        return this.fxBGHoverProp;
    }

    /**
     * Return background for hover.
     * This init the lazy fx property if null.
     *
     * @return background hover
     */
    public final Background getBackgroundHover() {

        return null == fxBGHoverProp ?
                Background.EMPTY : getFXBackgroundHoverProp().getValue();
    }

    /**
     * Set background for hover.
     *
     * @param bg background
     */
    public final void setBackgroundHover( final Background bg ) {

        getFXBackgroundHoverProp().setValue( bg );
    }

    private void initListener() {

        hoverProperty().addListener( this::onHoverChanged );
        //
        setOnMouseClicked( this::onMouseClicked );
        //
        fxToggledProp.addListener( this::onToggleChanged );
    }

    private void onHoverChanged( ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal ) {

        if ( null == newVal || newVal == oldVal ) {
            return;
        }

        if ( !isToggled() ) {
            setBackground( newVal ? hoverBG : origBG );
        }
    }

    private void onToggleChanged( ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal ) {

        if ( null == newVal || newVal == oldVal ) {
            return;
        }

        setBackground( newVal ? toggleBG : origBG );
    }

    private void onMouseClicked( MouseEvent me ) {

        getFXToggleProp().set( !isToggled() );
    }
}