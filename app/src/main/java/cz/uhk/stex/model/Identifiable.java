package cz.uhk.stex.model;

import java.io.Serializable;

/**
 * Object with an ID
 */
public interface Identifiable extends Serializable {
    String getId();
}
