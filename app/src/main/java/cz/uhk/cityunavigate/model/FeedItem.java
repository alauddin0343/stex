package cz.uhk.cityunavigate.model;

import android.support.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

import android.net.Uri;

import java.util.Map;

/**
 * User feed item
 */
@IgnoreExtraProperties
public class FeedItem implements Comparable<FeedItem>, Identifiable {
    private String id;
    private String userId;
    private String groupId;
    private String markerId;
    private long created;
    private Type type;
    private String text;
    private String title;
    private Uri thumbnail;
    private Map<String, Long> readBy; // Map of userId/time of users that displayed this feed item

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
        readBy = builder.readBy;
    }

    public static IId builder() {
        return new Builder();
    }

    public static Builder builder(FeedItem copy) {
        Builder builder = new Builder();
        builder.readBy = copy.readBy;
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

    public Uri getThumbnail() {
        return thumbnail;
    }

    public String getMarkerId() {
        return markerId;
    }

    public Map<String, Long> getReadBy() {
        return readBy;
    }

    @Override
    public int compareTo(@NonNull FeedItem feedItem) {
        return (int)(feedItem.created - created);
    }

    public enum Type {
        MarkerAdd,
        CommentAdd
    }


    public interface IBuild {
        FeedItem build();
    }

    public interface IThumbnail {
        IReadBy withThumbnail(Uri val);
    }

    public interface ITitle {
        IThumbnail withTitle(String val);
    }

    public interface IText {
        ITitle withText(String val);
    }

    public interface IType {
        IText withType(Type val);
    }

    public interface ICreated {
        IType withCreated(long val);
    }

    public interface IMarkerId {
        ICreated withMarkerId(String val);
    }

    public interface IGroupId {
        IMarkerId withGroupId(String val);
    }

    public interface IUserId {
        IGroupId withUserId(String val);
        FeedItem build();
    }

    public interface IId {
        IUserId withId(String val);
    }

    public interface IReadBy {
        IBuild withReadBy(Map<String, Long> val);
    }

    public static final class Builder implements IThumbnail, ITitle, IText, IType, ICreated, IMarkerId, IGroupId, IUserId, IId, IBuild, IReadBy {
        private Uri thumbnail;
        private String title;
        private String text;
        private Type type;
        private long created;
        private String markerId;
        private String groupId;
        private String userId;
        private String id;
        private Map<String, Long> readBy;

        private Builder() {
        }

        @Override
        public IReadBy withThumbnail(Uri val) {
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

        @Override
        public IBuild withReadBy(Map<String, Long> val) {
            readBy = val;
            return this;
        }
    }

}
