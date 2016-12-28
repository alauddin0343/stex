package cz.uhk.stex.model;

import java.io.Serializable;

/**
 * Object with an ID
 * Created by Karelp on 12.07.2016.
 */
public interface Identifiable extends Serializable {
    String getId();
}
