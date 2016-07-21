package cz.uhk.cityunavigate.util;

import android.support.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Allows chaining and handling future/async values. Implementation of {@link Promise}.
 */
public class PromiseImpl<T> extends Promise<T> {
    private @Nullable T value;
    private @Nullable Throwable error;
    private Set<ResolutionListener<T>> resolutionListeners = new HashSet<>();
    private Set<RejectionListener> rejectionListeners = new HashSet<>();
    private @NotNull State state = State.Pending;

    @Override
    public synchronized <R> Promise<R> success(@NotNull final SuccessListener<T, R> listener) {
        final PromiseImpl<R> nextPromise = new PromiseImpl<>();
        resolutionListeners.add(new ResolutionListener<T>() {
            @Override
            public void onSuccess(T result) {
                try {
                    R r = listener.onSuccess(result);
                    nextPromise.resolve(r);
                } catch (Throwable ex) {
                    nextPromise.reject(ex);
                }
                resolutionListeners.remove(this);
            }
        });

        rejectionListeners.add(new RejectionListener() {
            @Override
            public void onRejected(Throwable error) {
                nextPromise.reject(error);
                rejectionListeners.remove(this);
            }
        });

        checkStateAndRerun();

        return nextPromise;
    }

    @Override
    public synchronized Promise<T> error(@NotNull final ErrorListener<T> listener) {
        final PromiseImpl<T> nextPromise = new PromiseImpl<>();
        resolutionListeners.add(new ResolutionListener<T>() {
            @Override
            public void onSuccess(T result) {
                nextPromise.resolve(result);
            }
        });

        rejectionListeners.add(new RejectionListener() {
            @Override
            public void onRejected(Throwable error) {
                try {
                    T val = listener.onError(error);
                    nextPromise.resolve(val);
                } catch (Throwable ex) {
                    nextPromise.reject(ex);
                }
                rejectionListeners.remove(this);
            }
        });
        checkStateAndRerun();

        return nextPromise;
    }

    public synchronized <R> Promise<R> successFlat(@NotNull final SuccessListener<T, Promise<R>> listener) {
        final PromiseImpl<R> nextPromise = new PromiseImpl<>();
        resolutionListeners.add(new ResolutionListener<T>() {
            @Override
            public void onSuccess(T result) {
                Promise<R> r = listener.onSuccess(result);
                r.success(new SuccessListener<R, Void>() {
                    @Override
                    public Void onSuccess(R result) {
                        nextPromise.resolve(result);
                        return null;
                    }
                });
                r.error(new ErrorListener<R>() {
                    @Override
                    public R onError(Throwable error) {
                        nextPromise.reject(error);
                        return null;
                    }
                });
                resolutionListeners.remove(this);
            }
        });

        rejectionListeners.add(new RejectionListener() {
            @Override
            public void onRejected(Throwable error) {
                nextPromise.reject(error);
                rejectionListeners.remove(this);
            }
        });

        checkStateAndRerun();

        return nextPromise;
    }

    public synchronized Promise<T> errorFlat(@NotNull final ErrorListener<Promise<T>> listener) {
        final PromiseImpl<T> nextPromise = new PromiseImpl<>();
        resolutionListeners.add(new ResolutionListener<T>() {
            @Override
            public void onSuccess(T result) {
                nextPromise.resolve(result);
            }
        });

        rejectionListeners.add(new RejectionListener() {
            @Override
            public void onRejected(Throwable error) {
                final Promise<T> val = listener.onError(error);
                val.success(new SuccessListener<T, Object>() {
                                @Override
                                public Object onSuccess(T result) {
                                    nextPromise.resolve(result);
                                    return null;
                                }
                            });
                val.error(new ErrorListener<T>() {
                    @Override
                    public T onError(Throwable error) {
                        nextPromise.reject(error);
                        return null;
                    }
                });
                rejectionListeners.remove(this);
            }
        });
        checkStateAndRerun();

        return nextPromise;
    }

    private void checkStateAndRerun() {
        if (state == State.Resolved)
            resolve(value);
        if (state == State.Rejected)
            reject(error);
    }

    public synchronized void resolve(T value) {
        this.state = State.Resolved;
        this.value = value;

        for (ResolutionListener<T> resolutionListener : resolutionListeners) {
            resolutionListener.onSuccess(value);
        }
    }

    public synchronized void reject(Throwable error) {
        this.state = State.Rejected;
        this.error = error;

        for (RejectionListener rejectionListener : rejectionListeners) {
            rejectionListener.onRejected(error);
        }
    }

    private enum State {
        Pending,
        Resolved,
        Rejected
    }

    private interface RejectionListener {
        void onRejected(Throwable error);
    }

    private interface ResolutionListener<T> {
        void onSuccess(T result);
    }
}
