package eu.jpangolin.jtzipi.mymod.utils;

/**
 * Builder.
 * <p>
 * The OO 'builder' pattern.
 * </p>
 *
 * @param <T> type to build
 * @author jTzipi
 */
public interface IBuilder<T> {

    /**
     * Create new instance.
     *
     * @return created instance
     */
    T build();
}