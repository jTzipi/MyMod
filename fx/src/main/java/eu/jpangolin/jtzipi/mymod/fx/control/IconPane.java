package eu.jpangolin.jtzipi.mymod.fx.control;

import eu.jpangolin.jtzipi.mymod.io.image.ImageDimension;
import eu.jpangolin.jtzipi.mymod.utils.ModUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.slf4j.LoggerFactory;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;


/**
 * Icon Pane.
 * <p>
 *     This is a panel
 * </p>
 * @author jTzipi
 */
public class IconPane extends StackPane {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger("IconPane");

    private static final String GAP_BEAN_PROP_FX = "GAP_PROP_FX";
    private static final String PREF_SIZE_OOB_TMPL = "pref size = {} {} {} = {}";
    public static final double MIN_SIZE = 24D;
    public static final double MAX_SIZE = 512D;
    public static final double GAP_MIN = 0D;
    public static final double GAP_DEF = 14D;
    public static final double GAP_MAX = 64D;


    /**
     * Store icons on position.
     */
    private final EnumMap<Pos, ImageView> ivPosEMap = new EnumMap<>(Pos.class);
    private ImageView baseIV;
    private Pane iconLPane;

    private final DoubleBinding centerXBind = Bindings.divide(prefWidthProperty(), 2D);
    private final DoubleBinding centerYBind = Bindings.divide(prefHeightProperty(), 2D);
    /**
     * Gap Property with lazy init.
     */
    private DoubleProperty fxGapProp;

    /**
     * Gap for preferred size.
     * if gap is 14 then 7 pixel gap is added
     * to all 4 sides.
     */
    private double _gap = GAP_DEF;
    IconPane( double prefWidth, double prefHeight ) {
        setBaseSize(prefWidth, prefHeight);
        createIconPane();
    }

    /**
     * Create a new icon pane with a base image.
     * The size of the pane  from the image.
     * @param image base image
     *
     * @return icon pane
     * @throws NullPointerException if {@code image} is null
     */
    public static IconPane ofImage( Image image ) {
        Objects.requireNonNull(image, "null image");

        IconPane ip = new IconPane(image.getWidth(), image.getHeight());
        ip.setBaseImage(image);
        return ip;
    }

    /**
     * Create a new icon pane .
     * @param size size [{@linkplain #MIN_SIZE} .. {@linkplain #MAX_SIZE}]
     * @return icon pane
     */
    public static IconPane ofSize( double size ) {
        return new IconPane(size, size);
    }

    private void createIconPane(  ) {
        //
        iconLPane = new Pane();
        baseIV = new ImageView();

        getChildren().addAll( baseIV, iconLPane);
    }


    /**
     * Set the size of the base panel.
     * @param w new width [{@linkplain #MIN_SIZE} .. {@linkplain #MAX_SIZE}]
     * @param h new height [{@linkplain #MIN_SIZE} .. {@linkplain #MAX_SIZE}]
     */
    public void setBaseSize( double w, double h ) {
        double prefSize = Math.max(w, h);

        if( prefSize < MIN_SIZE ) {
            LOG.info(PREF_SIZE_OOB_TMPL, prefSize, "<", "MIN_SIZE", MIN_SIZE);
            prefSize = MIN_SIZE;
        } else if( prefSize > MAX_SIZE) {
            LOG.info(PREF_SIZE_OOB_TMPL, prefSize, ">", "MAX_SIZE", MAX_SIZE );
            prefSize = MAX_SIZE;
        }

        prefSize += _gap;

        // only when the pref size is !=
        // we set pref size and update the position
        // of sub icon
        if( getPrefWidth() != prefSize ) {
            setPrefSize(prefSize, prefSize);
            updatePosition();
        }

    }

    /**
     * Set image for the base image view, optional scaled to a new size.
     *
     * @param image image
     *
     */
    public void setBaseImage( Image image ) {
        Objects.requireNonNull(image, "Image ");

        // set new image
        baseIV.setImage(image);
    }

    /**
     * Set Base Opacity.
     * @param opacity opacity [{@linkplain ModUtils#MIN_OPACITY} .. {@linkplain ModUtils#MAX_OPACITY}]
     */
    public void setBaseOpacity( double opacity ) {
        opacity = ModUtils.clamp(opacity,ModUtils.MIN_OPACITY, ModUtils.MAX_OPACITY);
        baseIV.setOpacity(opacity);
    }

    public void setBaseEffect( final Effect effect  ) {

        baseIV.setEffect(effect);
    }


    public DoubleProperty gapPropFX() {
        if( null == fxGapProp ) {
            fxGapProp = new SimpleDoubleProperty(this, GAP_BEAN_PROP_FX, GAP_DEF);
        }

        return fxGapProp;
    }

    public final double getGap() {
        return null == fxGapProp? _gap : fxGapProp.get();
    }

    public final void setGap( double gap ) {
        _gap = ModUtils.clamp(gap, GAP_MIN, GAP_MAX);

        if( null != fxGapProp ) {
            fxGapProp.set(_gap);
        }

    }

    /**
     * Add a new sub icon
     * @param pos pos
     * @param image image
     * @throws NullPointerException if {@code pos} | {@code image}
     */
    public void addSubIcon( Pos pos, Image image ) {
        Objects.requireNonNull(pos, "Position must != null");
        Objects.requireNonNull(image, "Image must != null");

        ImageView iv = ivPosEMap.get(pos);

        // if we did not have this pos
        // we create a new ImageView and add this
        // to our pane
        if( null == iv ) {
            iv = new ImageView();
            iconLPane.getChildren().add(iv);
        }

        ivPosEMap.put(pos, iv);

        Point2D p2 = locationFor(pos,
                image.getWidth(),
                image.getHeight(),
                centerXBind.get(),
                centerYBind.get(),
                getPrefWidth(),
                getPrefWidth()
                );

        iv.relocate(p2.getX(), p2.getY());
        iv.setImage(image);
    }

    /**
     * Remove the image view for pos.
     * @param pos position
     * @return old ImageView or null if pos was not set
     */
    public ImageView removeSubIcon( Pos pos ) {
        Objects.requireNonNull(pos);
        ImageView iv =  ivPosEMap.get(pos);
        if( null == iv ) {

            return null;
        }
        iconLPane.getChildren().remove(iv);
        return iv;
    }

    private void updatePosition() {

        for( Map.Entry<Pos, ImageView> mapE : ivPosEMap.entrySet() ) {

            Pos pos = mapE.getKey();
            ImageView iv = mapE.getValue();
            Image image = iv.getImage();

            Point2D p2D = locationFor(pos,
                    image.getWidth(),
                    image.getHeight(),
                    centerXBind.get(),
                    centerYBind.get(),
                    getPrefWidth(),
                    getPrefHeight());
            iv.relocate(p2D.getX() , p2D.getY());

        }
    }

    private static Point2D locationFor(Pos pos, double imageW, double imageH, double cX, double cY, double pW, double pH ) {


        double halfIW = Math.round(imageW / 2D);
        double halfIH = Math.round(imageH / 2D);


        // TODO : gap
        return switch (pos ) {

            case TOP_LEFT -> new Point2D(0D, 0D);
            case TOP_CENTER -> new Point2D(cX - halfIW, 0D);
            case TOP_RIGHT -> new Point2D( pW - imageW, 0D);
            case CENTER_LEFT -> new Point2D(0D, cY - halfIH );
            case CENTER -> new Point2D(cX - halfIW, cY - halfIH );
            case CENTER_RIGHT -> new Point2D(pW - imageW, cY - halfIH );
            case BOTTOM_LEFT, BASELINE_LEFT -> new Point2D(0D, pH - imageH );
            case BOTTOM_CENTER, BASELINE_CENTER -> new Point2D(cX - imageW, pH - imageH);
            case BOTTOM_RIGHT, BASELINE_RIGHT -> new Point2D(pW- imageW, pH - imageH);
        };
    }

    private class AdvancedIconView {

        private static final float DEFAULT_OPACITY = 1F;
        private ImageDimension imageDimension;
        private Image image;

        private float opacity;

        private Effect effect;

        private ImageView imageView;

        private AdvancedIconView(final Image image ) {
            this.image = image;
            // TODO : Double
            this.imageDimension = ImageDimension.of((int)image.getWidth(), (int)image.getHeight());
            this.opacity=DEFAULT_OPACITY;
            this.effect = null;
            this.imageView = new ImageView(image);
        }

        ImageDimension getImageDimension() {
            return imageDimension;
        }
    }
}
