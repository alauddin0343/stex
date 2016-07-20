package cz.uhk.cityunavigate.util;

/**
 * @see com.tilelook.observer.ObserverListBase
 */
public class ObserverList2<Arg0, Arg1, Observer extends Observer2<Arg0, Arg1>> extends ObserverListBase<Observer> {
    /**
     * Dispatch the event to all observers.
     */
    public void fire(Arg0 arg0, Arg1 arg1) {
        for (Observer o : observers)
            o.onAction(arg0, arg1);
    }
}
