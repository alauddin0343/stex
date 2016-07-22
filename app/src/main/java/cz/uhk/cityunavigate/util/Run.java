package cz.uhk.cityunavigate.util;

import android.app.Activity;
import android.os.AsyncTask;

/**
 * Allows asynchronously running various tasks
 */
public final class Run {
    public static <R> Promise<R> runInUI(Activity ui, final Supplier<R> task) {
        final PromiseImpl<R> res = new PromiseImpl<>();
        ui.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    R val = task.supply();
                    res.resolve(val);
                } catch (Exception ex) {
                    res.reject(ex);
                }
            }
        });
        return res;
    }

    public static <R> Promise<R> runAsync(final Supplier<R> task) {
        final PromiseImpl<R> res = new PromiseImpl<>();
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                try {
                    res.resolve(task.supply());
                } catch (Exception ex) {
                    res.reject(ex);
                }
                return null;
            }
        }.execute();
        return res;
    }

    public static <T, R> Promise.SuccessListener<T, Promise<R>> promiseUi(final Activity ui, final Function<T, R> function) {
        return new Promise.SuccessListener<T, Promise<R>>() {
            @Override
            public Promise<R> onSuccess(final T result) {
                return runInUI(ui, new Supplier<R>() {
                    @Override
                    public R supply() throws Exception {
                        return function.apply(result);
                    }
                });
            }
        };
    }
}
