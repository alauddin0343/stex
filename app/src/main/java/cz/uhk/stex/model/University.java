package cz.uhk.stex.model;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

import java.net.URL;

/**
 * University DTO
 */
@IgnoreExtraProperties
public class University implements Identifiable {
    private String id;
    private String name;
    private Bitmap logo;
    private URL website;
    private LatLng coord;

    public University(String id, String name, Bitmap logo, URL website, LatLng coord) {
        this.id = id;
        this.name = name;
        this.logo = logo;
        this.website = website;
        this.coord = coord;
    }

    private University(Builder builder) {
        id = builder.id;
        name = builder.name;
        logo = builder.logo;
        website = builder.website;
        coord = builder.coord;
    }

    public static Builder builder(University copy) {
        Builder builder = new Builder();
        builder.coord = copy.coord;
        builder.website = copy.website;
        builder.logo = copy.logo;
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

    public Bitmap getLogo() {
        return logo;
    }

    public URL getWebsite() {
        return website;
    }

    public LatLng getCoord() {
        return coord;
    }


    public static final class Builder implements ICoord, IWebsite, ILogo, IName, IId, IBuild {
        private LatLng coord;
        private URL website;
        private Bitmap logo;
        private String name;
        private String id;

        private Builder() {
        }

        @Override
        public IBuild withCoord(LatLng val) {
            coord = val;
            return this;
        }

        @Override
        public ICoord withWebsite(URL val) {
            website = val;
            return this;
        }

        @Override
        public IWebsite withLogo(Bitmap val) {
            logo = val;
            return this;
        }

        @Override
        public ILogo withName(String val) {
            name = val;
            return this;
        }

        @Override
        public IName withId(String val) {
            id = val;
            return this;
        }

        public University build() {
            return new University(this);
        }
    }

    public interface IBuild {
        University build();
    }

    public interface ICoord {
        IBuild withCoord(LatLng val);
    }

    public interface IWebsite {
        ICoord withWebsite(URL val);
    }

    public interface ILogo {
        IWebsite withLogo(Bitmap val);
    }

    public interface IName {
        ILogo withName(String val);
    }

    public interface IId {
        IName withId(String val);
    }

}
