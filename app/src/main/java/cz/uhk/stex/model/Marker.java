package cz.uhk.stex.model;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Marker DTO
 */
@IgnoreExtraProperties
public class Marker implements Identifiable {
    private String id, idGroup, idUserAuthor, idCategory;
    private LatLng location;
    private String title, text;
    private long created;
    private Uri image;

    private Marker(Builder builder) {
        id = builder.id;
        idGroup = builder.idGroup;
        idUserAuthor = builder.idUserAuthor;
        idCategory = builder.idCategory;
        location = builder.location;
        title = builder.title;
        text = builder.text;
        created = builder.created;
        image = builder.image;
    }

    public static Builder newBuilder(Marker copy) {
        Builder builder = new Builder();
        builder.image = copy.image;
        builder.created = copy.created;
        builder.text = copy.text;
        builder.title = copy.title;
        builder.location = copy.location;
        builder.idCategory = copy.idCategory;
        builder.idUserAuthor = copy.idUserAuthor;
        builder.idGroup = copy.idGroup;
        builder.id = copy.id;
        return builder;
    }

    public static IId builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public String getIdUserAuthor() {
        return idUserAuthor;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getIdCategory() {
        return idCategory;
    }

    public long getCreated() {
        return created;
    }

    public Uri getImage() {
        return image;
    }


    public interface IBuild {
        Marker build();
    }

    public interface IImage {
        IBuild withImage(Uri val);
    }

    public interface ICreated {
        IImage withCreated(long val);
    }

    public interface IText {
        ICreated withText(String val);
    }

    public interface ITitle {
        IText withTitle(String val);
    }

    public interface ILocation {
        ITitle withLocation(LatLng val);
    }

    public interface IIdCategory {
        ILocation withIdCategory(String val);
    }

    public interface IIdUserAuthor {
        IIdCategory withIdUserAuthor(String val);
    }

    public interface IIdGroup {
        IIdUserAuthor withIdGroup(String val);
        Marker build();
    }

    public interface IId {
        IIdGroup withId(String val);
    }

    public static final class Builder implements IImage, ICreated, IText, ITitle, ILocation, IIdCategory, IIdUserAuthor, IIdGroup, IId, IBuild {
        private Uri image;
        private long created;
        private String text;
        private String title;
        private LatLng location;
        private String idCategory;
        private String idUserAuthor;
        private String idGroup;
        private String id;

        private Builder() {
        }

        @Override
        public IBuild withImage(Uri val) {
            image = val;
            return this;
        }

        @Override
        public IImage withCreated(long val) {
            created = val;
            return this;
        }

        @Override
        public ICreated withText(String val) {
            text = val;
            return this;
        }

        @Override
        public IText withTitle(String val) {
            title = val;
            return this;
        }

        @Override
        public ITitle withLocation(LatLng val) {
            location = val;
            return this;
        }

        @Override
        public ILocation withIdCategory(String val) {
            idCategory = val;
            return this;
        }

        @Override
        public IIdCategory withIdUserAuthor(String val) {
            idUserAuthor = val;
            return this;
        }

        @Override
        public IIdUserAuthor withIdGroup(String val) {
            idGroup = val;
            return this;
        }

        @Override
        public IIdGroup withId(String val) {
            id = val;
            return this;
        }

        public Marker build() {
            return new Marker(this);
        }
    }
}
