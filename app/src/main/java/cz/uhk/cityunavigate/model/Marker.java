package cz.uhk.cityunavigate.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

/**
 * Marker DTO
 */
@IgnoreExtraProperties
public class Marker {
    private String id, idGroup, idUserAuthor, idUserAdmin, idCategory;
    private LatLng location;
    private List<String> commentIds;

    // For Firebase
    public Marker() {

    }

    public Marker(String id, String idGroup, String idUserAuthor, String idUserAdmin, String idCategory, LatLng location, List<String> commentIds) {
        this.id = id;
        this.idGroup = idGroup;
        this.idUserAuthor = idUserAuthor;
        this.idUserAdmin = idUserAdmin;
        this.idCategory = idCategory;
        this.location = location;
        this.commentIds = commentIds;
    }

    private Marker(Builder builder) {
        id = builder.id;
        idGroup = builder.idGroup;
        idUserAuthor = builder.idUserAuthor;
        idUserAdmin = builder.idUserAdmin;
        idCategory = builder.idCategory;
        location = builder.location;
        commentIds = builder.commentIds;
    }

    public static Builder newBuilder(Marker copy) {
        Builder builder = new Builder();
        builder.commentIds = copy.commentIds;
        builder.location = copy.location;
        builder.idCategory = copy.idCategory;
        builder.idUserAdmin = copy.idUserAdmin;
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

    public String getIdUserAdmin() {
        return idUserAdmin;
    }

    public String getIdCategory() {
        return idCategory;
    }

    public LatLng getLocation() {
        return location;
    }

    public List<String> getCommentIds() {
        return commentIds;
    }


    public interface IBuild {
        Marker build();
    }

    public interface ICommentIds {
        IBuild withCommentIds(List<String> val);
    }

    public interface ILocation {
        ICommentIds withLocation(LatLng val);
    }

    public interface IIdCategory {
        ILocation withIdCategory(String val);
    }

    public interface IIdUserAdmin {
        IIdCategory withIdUserAdmin(String val);
    }

    public interface IIdUserAuthor {
        IIdUserAdmin withIdUserAuthor(String val);
    }

    public interface IIdGroup {
        IIdUserAuthor withIdGroup(String val);
    }

    public interface IId {
        IIdGroup withId(String val);
    }

    public static final class Builder implements ICommentIds, ILocation, IIdCategory, IIdUserAdmin, IIdUserAuthor, IIdGroup, IId, IBuild {
        private List<String> commentIds;
        private LatLng location;
        private String idCategory;
        private String idUserAdmin;
        private String idUserAuthor;
        private String idGroup;
        private String id;

        private Builder() {
        }

        @Override
        public IBuild withCommentIds(List<String> val) {
            commentIds = val;
            return this;
        }

        @Override
        public ICommentIds withLocation(LatLng val) {
            location = val;
            return this;
        }

        @Override
        public ILocation withIdCategory(String val) {
            idCategory = val;
            return this;
        }

        @Override
        public IIdCategory withIdUserAdmin(String val) {
            idUserAdmin = val;
            return this;
        }

        @Override
        public IIdUserAdmin withIdUserAuthor(String val) {
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
