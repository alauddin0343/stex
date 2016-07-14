package cz.uhk.cityunavigate.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Marker DTO
 */
public class Marker {
    private String id, idGroup, idUserAuthor, idUserAdmin;
    private LatLng location;
    private List<String> commentIds;

    public Marker(String id, String idGroup, String idUserAuthor, String idUserAdmin, LatLng location, List<String> commentIds) {
        this.id = id;
        this.idGroup = idGroup;
        this.idUserAuthor = idUserAuthor;
        this.idUserAdmin = idUserAdmin;
        this.location = location;
        this.commentIds = commentIds;
    }

    private Marker(Builder builder) {
        id = builder.id;
        idGroup = builder.idGroup;
        idUserAuthor = builder.idUserAuthor;
        idUserAdmin = builder.idUserAdmin;
        location = builder.location;
        commentIds = builder.commentIds;
    }

    public static Builder builder(Marker copy) {
        Builder builder = new Builder();
        builder.commentIds = copy.commentIds;
        builder.location = copy.location;
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

    public LatLng getLocation() {
        return location;
    }

    public List<String> getCommentIds() {
        return commentIds;
    }


    public static final class Builder implements ICommentIds, ILocation, IIdUserAdmin, IIdUserAuthor, IIdGroup, IId, IBuild {
        private List<String> commentIds;
        private LatLng location;
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
        public ILocation withIdUserAdmin(String val) {
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

    interface IBuild {
        Marker build();
    }

    interface ICommentIds {
        IBuild withCommentIds(List<String> val);
    }

    interface ILocation {
        ICommentIds withLocation(LatLng val);
    }

    interface IIdUserAdmin {
        ILocation withIdUserAdmin(String val);
    }

    interface IIdUserAuthor {
        IIdUserAdmin withIdUserAuthor(String val);
    }

    interface IIdGroup {
        IIdUserAuthor withIdGroup(String val);
    }

    interface IId {
        IIdGroup withId(String val);
    }
}
