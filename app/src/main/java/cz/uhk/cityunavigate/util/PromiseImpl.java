package cz.uhk.cityunavigate.util;

import android.support.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

/**
 * Allows chaining and handling future/async values. Implementation of {@link Promise}.
 */
public class PromiseImpl<T> extends Promise<T> {
    private @Nullable T value;
    private @Nullable Throwable error;
    private @NotNull State state = State.Pending;
    private @Nullable SuccessListener onSuccess;
    private @Nullable ErrorListener onError;
    private @Nullable PromiseImpl nextPromise;

    @Override
    public synchronized  <R> Promise<R> success(@NotNull SuccessListener<T, R> listener) {
        onSuccess = listener;
        PromiseImpl<R> res = new PromiseImpl<>();
        nextPromise = res;

        if (state == State.Resolved)
            resolve(value);

        return res;
    }

    @Override
    public synchronized  <R> Promise<R> error(@NotNull ErrorListener<R> listener) {
        onError = listener;
        PromiseImpl<R> res = new PromiseImpl<>();
        nextPromise = res;
        if (state == State.Rejected && error != null)
            reject(error);
        return res;
    }

    public synchronized void resolve(T value) {
        this.state = State.Resolved;
        this.value = value;
        //noinspection unchecked
        Object resVal = onSuccess == null ? null : onSuccess.onSuccess(value);
        if (nextPromise != null) {
            try {
                //noinspection unchecked
                nextPromise.resolve(resVal);
            } catch (Exception ex) {
                nextPromise.reject(ex);
            }
        }
    }

    public synchronized void reject(Throwable error) {
        this.state = State.Rejected;
        this.error = error;
        Object resVal = onError == null ? null : onError.onError(error);
        if (nextPromise != null) {
            try {
                //noinspection unchecked
                nextPromise.resolve(resVal);
            } catch (Exception ex) {
                nextPromise.reject(ex);
            }
        }
    }

    private enum State {
        Pending,
        Resolved,
        Rejected
    }
}
