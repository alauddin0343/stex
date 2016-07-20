package cz.uhk.cityunavigate.model;

import android.support.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

import java.net.URI;

/**
 * User feed item
 */
@IgnoreExtraProperties
public class FeedItem implements Comparable<FeedItem> {
    private String id;
    private String userId;
    private String groupId;
    private String markerId;
    private long created;
    private Type type;
    private String text;
    private String title;
    private URI thumbnail;

    private FeedItem(Builder builder) {
        id = builder.id;
        userId = builder.userId;
        groupId = builder.groupId;
        markerId = builder.markerId;
        created = builder.created;
        type = builder.type;
        text = builder.text;
        title = builder.title;
        thumbnail = builder.thumbnail;
    }

    public static Builder builder(FeedItem copy) {
        Builder builder = new Builder();
        builder.thumbnail = copy.thumbnail;
        builder.title = copy.title;
        builder.text = copy.text;
        builder.type = copy.type;
        builder.created = copy.created;
        builder.markerId = copy.markerId;
        builder.groupId = copy.groupId;
        builder.userId = copy.userId;
        builder.id = copy.id;
        return builder;
    }

    public static IId builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public long getCreated() {
        return created;
    }

    public Type getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }

    public URI getThumbnail() {
        return thumbnail;
    }

    public String getMarkerId() {
        return markerId;
    }

    @Override
    public int compareTo(@NonNull FeedItem feedItem) {
        return (int)(feedItem.created - created);
    }

    public enum Type {
        MarkerAdd,
        CommentAdd
    }


    interface IBuild {
        FeedItem build();
    }

    interface IThumbnail {
        IBuild withThumbnail(URI val);
    }

    interface ITitle {
        IThumbnail withTitle(String val);
    }

    interface IText {
        ITitle withText(String val);
    }

    interface IType {
        IText withType(Type val);
    }

    interface ICreated {
        IType withCreated(long val);
    }

    interface IMarkerId {
        ICreated withMarkerId(String val);
    }

    interface IGroupId {
        IMarkerId withGroupId(String val);
    }

    interface IUserId {
        IGroupId withUserId(String val);
    }

    interface IId {
        IUserId withId(String val);
    }

    public static final class Builder implements IThumbnail, ITitle, IText, IType, ICreated, IMarkerId, IGroupId, IUserId, IId, IBuild {
        private URI thumbnail;
        private String title;
        private String text;
        private Type type;
        private long created;
        private String markerId;
        private String groupId;
        private String userId;
        private String id;

        private Builder() {
        }

        @Override
        public IBuild withThumbnail(URI val) {
            thumbnail = val;
            return this;
        }

        @Override
        public IThumbnail withTitle(String val) {
            title = val;
            return this;
        }

        @Override
        public ITitle withText(String val) {
            text = val;
            return this;
        }

        @Override
        public IText withType(Type val) {
            type = val;
            return this;
        }

        @Override
        public IType withCreated(long val) {
            created = val;
            return this;
        }

        @Override
        public ICreated withMarkerId(String val) {
            markerId = val;
            return this;
        }

        @Override
        public IMarkerId withGroupId(String val) {
            groupId = val;
            return this;
        }

        @Override
        public IGroupId withUserId(String val) {
            userId = val;
            return this;
        }

        @Override
        public IUserId withId(String val) {
            id = val;
            return this;
        }

        public FeedItem build() {
            return new FeedItem(this);
        }
    }
}
