package cz.uhk.stex.util;

/**
 * A simple method which supplies a value from a mutable source.
 */
public interface Supplier<T> {
    T supply() throws Exception;
}
