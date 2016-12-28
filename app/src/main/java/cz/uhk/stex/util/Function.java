package cz.uhk.stex.util;

/**
 * Created by Karelp on 12.07.2016.
 * @param <T>
 * @param <R>
 */
public interface Function<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t);
}
