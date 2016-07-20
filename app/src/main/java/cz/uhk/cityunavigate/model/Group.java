package cz.uhk.cityunavigate.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

/**
 * Group DTO
 */
@IgnoreExtraProperties
public class Group {
    private String id;
    private String name;
    private String idUniversity;
    private List<String> adminsIds;
    private List<String> userIds;

    // For Firebase
    public Group() {

    }

    public Group(String id, String name, String idUniversity, List<String> adminIds, List<String> userIds) {
        this.id = id;
        this.name = name;
        this.idUniversity = idUniversity;
        this.adminsIds = adminIds;
        this.userIds = userIds;
    }

    private Group(Builder builder) {
        id = builder.id;
        name = builder.name;
        idUniversity = builder.idUniversity;
        adminsIds = builder.adminsIds;
        userIds = builder.userIds;
    }

    public static Builder builder(Group copy) {
        Builder builder = new Builder();
        builder.userIds = copy.userIds;
        builder.adminsIds = copy.adminsIds;
        builder.idUniversity = copy.idUniversity;
        builder.name = copy.name;
        builder.id = copy.id;
        return builder;
    }

    public static IId builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIdUniversity() {
        return idUniversity;
    }

    public List<String> getAdminsIds() {
        return adminsIds;
    }

    public List<String> getUserIds() {
        return userIds;
    }


    public static final class Builder implements IUserIds, IAdminsIds, IUniversity, IName, IId, IBuild {
        private List<String> userIds;
        private List<String> adminsIds;
        private String idUniversity;
        private String name;
        private String id;

        private Builder() {
        }

        @Override
        public IBuild withUserIds(List<String> val) {
            userIds = val;
            return this;
        }

        @Override
        public IUserIds withAdminsIds(List<String> val) {
            adminsIds = val;
            return this;
        }

        @Override
        public IAdminsIds withUniversity(String val) {
            idUniversity = val;
            return this;
        }

        @Override
        public IUniversity withName(String val) {
            name = val;
            return this;
        }

        @Override
        public IName withId(String val) {
            id = val;
            return this;
        }

        public Group build() {
            return new Group(this);
        }
    }

    public interface IBuild {
        Group build();
    }

    public interface IUserIds {
        IBuild withUserIds(List<String> val);
    }

    public interface IAdminsIds {
        IUserIds withAdminsIds(List<String> val);
    }

    public interface IUniversity {
        IAdminsIds withUniversity(String val);
    }

    public interface IName {
        IUniversity withName(String val);
    }

    public interface IId {
        IName withId(String val);
    }
}
