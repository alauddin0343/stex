package cz.uhk.stex.model;

import android.net.Uri;

/**
 * User comment
 */
public class Comment implements Identifiable {
    private String id;
    private long created;
    private Uri image;
    private String text;
    private String userId;

    private Comment(Builder builder) {
        id = builder.id;
        created = builder.created;
        image = builder.image;
        text = builder.text;
        userId = builder.userId;
    }

    public static Builder builder(Comment copy) {
        Builder builder = new Builder();
        builder.userId = copy.userId;
        builder.text = copy.text;
        builder.image = copy.image;
        builder.created = copy.created;
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

    public long getCreated() {
        return created;
    }

    public Uri getImage() {
        return image;
    }

    public String getText() {
        return text;
    }

    public String getUserId() {
        return userId;
    }


    public interface IBuild {
        Comment build();
    }

    public interface IUserId {
        IBuild withUserId(String val);
    }

    public interface IText {
        IUserId withText(String val);
    }

    public interface IImage {
        IText withImage(Uri val);
    }

    public interface ICreated {
        IImage withCreated(long val);
        Comment build();
    }

    public interface IId {
        ICreated withId(String val);
    }

    public static final class Builder implements IUserId, IText, IImage, ICreated, IId, IBuild {
        private String userId;
        private String text;
        private Uri image;
        private long created;
        private String id;

        private Builder() {
        }

        @Override
        public IBuild withUserId(String val) {
            userId = val;
            return this;
        }

        @Override
        public IUserId withText(String val) {
            text = val;
            return this;
        }

        @Override
        public IText withImage(Uri val) {
            image = val;
            return this;
        }

        @Override
        public IImage withCreated(long val) {
            created = val;
            return this;
        }

        @Override
        public ICreated withId(String val) {
            id = val;
            return this;
        }

        public Comment build() {
            return new Comment(this);
        }
    }
}
