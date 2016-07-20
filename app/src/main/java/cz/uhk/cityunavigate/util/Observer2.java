package cz.uhk.cityunavigate.util;

/**
 * Observer whose action takes 2 parameters.
 */
public interface Observer2<Arg0, Arg1> {
    void onAction(Arg0 arg0, Arg1 arg1);
}
