package cz.uhk.cityunavigate.model;

import java.util.List;

/**
 * Group DTO
 */
public class Group {
    private String id;
    private String name;
    private University university;
    private List<String> adminsIds;
    private List<String> userIds;

    public Group(String id, String name, University university, List<String> adminIds, List<String> userIds) {
        this.id = id;
        this.name = name;
        this.university = university;
        this.adminsIds = adminIds;
        this.userIds = userIds;
    }

    private Group(Builder builder) {
        id = builder.id;
        name = builder.name;
        university = builder.university;
        adminsIds = builder.adminsIds;
        userIds = builder.userIds;
    }

    public static Builder builder(Group copy) {
        Builder builder = new Builder();
        builder.userIds = copy.userIds;
        builder.adminsIds = copy.adminsIds;
        builder.university = copy.university;
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

    public University getUniversity() {
        return university;
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
        private University university;
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
        public IAdminsIds withUniversity(University val) {
            university = val;
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

    interface IBuild {
        Group build();
    }

    interface IUserIds {
        IBuild withUserIds(List<String> val);
    }

    interface IAdminsIds {
        IUserIds withAdminsIds(List<String> val);
    }

    interface IUniversity {
        IAdminsIds withUniversity(University val);
    }

    interface IName {
        IUniversity withName(String val);
    }

    interface IId {
        IName withId(String val);
    }
}
