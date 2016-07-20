package cz.uhk.cityunavigate.util;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Represents a list of Observers. Basically a wrapped list
 * with a fire() method. The observers are simple event wrappers,
 * the error() and complete() methods are assumed to be empty.
 *
 * This is valuable for wrapping property change listeners and
 * similar observables to better interface with RxJava.
 *
 * This class is extended for various observers with various number of parameters.
 */
public abstract class ObserverListBase<Observer> implements Serializable {
    protected final HashSet<Observer> observers = new HashSet<>();

    /**
     * Subscribe a new observer for this observable.
     * @param observer observer to subscribe
     */
    public void subscribe(@NotNull Observer observer) {
        observers.add(observer);
    }

    /**
     * Unsubscribe the given observer from this observable.
     * @param observer observer to unsubscribe
     */
    public void unsubscribe(@NotNull Observer observer) {
        observers.remove(observer);
    }
}
