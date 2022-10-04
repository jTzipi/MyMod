package eu.jpangolin.jtzipi.mymod.utils;

/**
 * Builder.
 * <p>
 *     The OO 'builder' pattern.
 * </p>
 *
 * @author jTzipi
 * @param <T> type to build
 */
public interface IBuilder<T> {

    /**
     * Create new instance.
     * @return created instance
     */
    T build();
}