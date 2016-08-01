package cz.uhk.cityunavigate.util;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Allows chaining and handling future/async values.
 */
public abstract class Promise<T> {
    public abstract <R> Promise<R> success(@NotNull SuccessListener<T, R> listener);

    public abstract Promise<T> error(@NotNull ErrorListener<T> listener);

    public abstract  <R> Promise<R> successFlat(@NotNull final SuccessListener<T, Promise<R>> listener);

    public abstract Promise<T> errorFlat(@NotNull final ErrorListener<Promise<T>> listener);

    public interface SuccessListener<T, R> {
        R onSuccess(T result) throws Exception;
    }

    public interface ErrorListener<R> {
        R onError(Throwable error);
    }

    /**
     * Converts Google {@link Task} to a {@link Promise}.
     * @param task task to convert
     * @param <T> return type
     * @return promise
     */
    public static <T> Promise<T> fromTask(Task<T> task) {
        final PromiseImpl<T> res = new PromiseImpl<>();
        task.addOnSuccessListener(new OnSuccessListener<T>() {
            @Override
            public void onSuccess(T t) {
                res.resolve(t);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                res.reject(e);
            }
        });
        return res;
    }

    /**
     * Converts a list of promises to a promise to a list. Waits for all promises to be resolved
     * and returns their results in a list.
     * @param promises promises to wait for
     * @param <T> value type
     * @return promise to all resolved promises
     */
    public static <T> Promise<List<T>> all(List<Promise<T>> promises) {
        @SuppressWarnings("unchecked") T[] results = (T[])new Object[promises.size()];
        AtomicInteger remaining = new AtomicInteger(promises.size());
        PromiseImpl<List<T>> resPromise = new PromiseImpl<>();
        int i = 0;
        for (Promise<T> promise : promises) {
            promise.success(new AllPromiseListener<>(i, results, resPromise, remaining));
            i++;
        }
        return resPromise;
    }

    /**
     * Create a promise that is already resolved with the given value.
     * @param result promise result
     * @param <T> result type
     * @return resolved promise
     */
    public static <T> Promise<T> resolved(T result) {
        PromiseImpl<T> r = new PromiseImpl<>();
        r.resolve(result);
        return r;
    }

    private static class AllPromiseListener<T> implements SuccessListener<T, Void>, ErrorListener<Void> {
        private final int index;
        private final T[] resultList;
        private final PromiseImpl<List<T>> resultPromise;
        private final AtomicInteger remaining;

        private AllPromiseListener(int index, T[] resultList, PromiseImpl<List<T>> resultPromise, AtomicInteger remaining) {
            this.index = index;
            this.resultList = resultList;
            this.resultPromise = resultPromise;
            this.remaining = remaining;
        }

        @Override
        public Void onSuccess(T result) throws Exception {
            resultList[index] = result;
            if (remaining.decrementAndGet() == 0)
                resultPromise.resolve(Arrays.asList(resultList));
            return null;
        }

        @Override
        public Void onError(Throwable error) {
            resultPromise.reject(error);
            return null;
        }
    }
}