package cz.uhk.stex.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Marker category
 */
@IgnoreExtraProperties
public class Category implements Identifiable {
    private String id;
    private String name;
    private float hue;

    public Category(String id, String name, float hue) {
        this.id = id;
        this.name = name;
        this.hue = hue;
    }

    private Category(Builder builder) {
        id = builder.id;
        name = builder.name;
        hue = builder.hue;
    }

    public static Builder newBuilder(Category copy) {
        Builder builder = new Builder();
        builder.hue = copy.hue;
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

    public float getHue() {
        return hue;
    }

    @Override
    public String toString() {
        return name;
    }

    public interface IBuild {
        Category build();
    }

    public interface IHue {
        IBuild withHue(float val);
    }

    public interface IName {
        IHue withName(String val);
    }

    public interface IId {
        IName withId(String val);
    }

    public static final class Builder implements IHue, IName, IId, IBuild {
        private float hue;
        private String name;
        private String id;

        private Builder() {
        }

        @Override
        public IBuild withHue(float val) {
            hue = val;
            return this;
        }

        @Override
        public IHue withName(String val) {
            name = val;
            return this;
        }

        @Override
        public IName withId(String val) {
            id = val;
            return this;
        }

        public Category build() {
            return new Category(this);
        }
    }
}
