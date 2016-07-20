package cz.uhk.cityunavigate.util;

import org.jetbrains.annotations.NotNull;

/**
 * Allows chaining and handling future/async values.
 */
public abstract class Promise<T> {
    public abstract <R> Promise<R> success(@NotNull SuccessListener<T, R> listener);

    public abstract <R> Promise<R> error(@NotNull ErrorListener<R> listener);

    public interface SuccessListener<T, R> {
        R onSuccess(T result);
    }

    public interface ErrorListener<R> {
        R onError(Throwable error);
    }
}