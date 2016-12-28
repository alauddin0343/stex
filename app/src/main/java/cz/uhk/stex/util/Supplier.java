package cz.uhk.stex.util;

/**
 * A simple method which supplies a value from a mutable source.
 * Created by Karelp on 12.07.2016.
 */
public interface Supplier<T> {
    T supply() throws Exception;
}
