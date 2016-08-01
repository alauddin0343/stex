package cz.uhk.cityunavigate.model;

import android.net.Uri;

import java.util.List;

/**
 * Information about a user from the database
 */
public class User implements Identifiable {
    private String id;
    private String name;
    private String email;
    private String activeGroup;
    private List<String> groups;
    private List<String> administrators;
    private Uri image;
    private long created;

    private User(Builder builder) {
        id = builder.id;
        name = builder.name;
        email = builder.email;
        activeGroup = builder.activeGroup;
        groups = builder.groups;
        administrators = builder.administrators;
        image = builder.image;
        created = builder.created;
    }

    public static Builder newBuilder(User copy) {
        Builder builder = new Builder();
        builder.created = copy.created;
        builder.image = copy.image;
        builder.administrators = copy.administrators;
        builder.groups = copy.groups;
        builder.activeGroup = copy.activeGroup;
        builder.email = copy.email;
        builder.name = copy.name;
        builder.id = copy.id;
        return builder;
    }

    public static IId builder() {
        return new Builder();
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getGroups() {
        return groups;
    }

    public List<String> getAdministrators() {
        return administrators;
    }

    public Uri getImage() {
        return image;
    }

    public long getCreated() {
        return created;
    }

    public String getActiveGroup() {
        return activeGroup;
    }


    public interface IBuild {
        User build();
    }

    public interface ICreated {
        IBuild withCreated(long val);
    }

    public interface IImage {
        ICreated withImage(Uri val);
    }

    public interface IAdministrators {
        IImage withAdministrators(List<String> val);
    }

    public interface IGroups {
        IAdministrators withGroups(List<String> val);
    }

    public interface IActiveGroup {
        IGroups withActiveGroup(String val);
    }

    public interface IEmail {
        IActiveGroup withEmail(String val);
    }

    public interface IName {
        IEmail withName(String val);
    }

    public interface IId {
        IName withId(String val);
    }

    public static final class Builder implements ICreated, IImage, IAdministrators, IGroups, IActiveGroup, IEmail, IName, IId, IBuild {
        private long created;
        private Uri image;
        private List<String> administrators;
        private List<String> groups;
        private String activeGroup;
        private String email;
        private String name;
        private String id;

        private Builder() {
        }

        @Override
        public IBuild withCreated(long val) {
            created = val;
            return this;
        }

        @Override
        public ICreated withImage(Uri val) {
            image = val;
            return this;
        }

        @Override
        public IImage withAdministrators(List<String> val) {
            administrators = val;
            return this;
        }

        @Override
        public IAdministrators withGroups(List<String> val) {
            groups = val;
            return this;
        }

        @Override
        public IGroups withActiveGroup(String val) {
            activeGroup = val;
            return this;
        }

        @Override
        public IActiveGroup withEmail(String val) {
            email = val;
            return this;
        }

        @Override
        public IEmail withName(String val) {
            name = val;
            return this;
        }

        @Override
        public IName withId(String val) {
            id = val;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
