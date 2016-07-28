package cz.uhk.cityunavigate.util;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;

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
}