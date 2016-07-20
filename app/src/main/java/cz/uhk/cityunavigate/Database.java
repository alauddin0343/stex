
package cz.uhk.cityunavigate;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.uhk.cityunavigate.model.Category;
import cz.uhk.cityunavigate.model.FeedItem;
import cz.uhk.cityunavigate.model.Group;
import cz.uhk.cityunavigate.model.Identifiable;
import cz.uhk.cityunavigate.model.Marker;
import cz.uhk.cityunavigate.util.ObservableList;
import cz.uhk.cityunavigate.util.Promise;
import cz.uhk.cityunavigate.util.PromiseImpl;
import cz.uhk.cityunavigate.util.Util;

/**
 * Database access
 */
public class Database {
    private static FirebaseDatabase db() {
        return FirebaseDatabase.getInstance();
    }

    /**
     * Accept all existing and future group invitations for the given user.
     *
     * @param user logged in user
     */
    public static void acceptInvitations(@NotNull final FirebaseUser user) {
        DatabaseReference invitations = db()
                .getReference("invitations")
                .child(Util.MD5(user.getEmail()))
                .child("groups");

        invitations.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String s) {
                String groupId = snapshot.getKey();
                DatabaseReference groupUsers = db()
                        .getReference("groups")
                        .child(groupId)
                        .child("users")
                        .child(user.getUid());
                groupUsers.setValue(System.currentTimeMillis());

                DatabaseReference userGroups = db()
                        .getReference("users")
                        .child(user.getUid())
                        .child("groups")
                        .child(groupId);
                userGroups.setValue(System.currentTimeMillis());

                snapshot.getRef().removeValue();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Adds a freshly registered user to the database. Does nothing if the user is already
     * registered in the database.
     *
     * @param user registered user
     */
    public static void registerUserToDatabase(@NotNull final FirebaseUser user) {
        final DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Add the user if not present
                    userRef.child("created").setValue(System.currentTimeMillis());
                    userRef.child("email").setValue(user.getEmail());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Returns the list of groups the user is enrolled in. You can add observers
     * to the list to react when the user is added/removed to/from a group.
     * @param user user to get the groups for
     * @return user groups
     */
    public static ObservableList<Group> getUserGroups(@NotNull FirebaseUser user) {
        final ObservableList<Group> result = new ObservableList<>();
        db().getReference("users")
                .child(user.getUid())
                .child("groups")
                .addChildEventListener(new ChildEventAdapter() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String groupId = dataSnapshot.getKey();
                        if (groupId != null)
                            db().getReference("groups").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Map<String, Object> groupMap = snapshotToMap(dataSnapshot);
                                    Map<String, Object> userMap = objectToMap(groupMap.get("users"));
                                    List<String> users = new ArrayList<>(userMap.keySet());
                                    Group group = Group.builder()
                                            .withId(dataSnapshot.getKey())
                                            .withName((String)groupMap.get("name"))
                                            .withUniversity((String)groupMap.get("university"))
                                            .withAdminsIds(Collections.singletonList((String)groupMap.get("administrator")))
                                            .withUserIds(users)
                                            .build();
                                    result.add(group);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        removeFromListById(result, dataSnapshot.getKey());
                    }
                });
        return result;
    }

    /**
     * Gets feed for the given group. The resulting list should be observed
     * for any changes in the feed.
     * @param groupId group to get the feed for
     * @param feedItemLimit maximum number of items to fetch
     * @return group feed
     */
    public static ObservableList<FeedItem> getGroupFeed(@NotNull final String groupId, final int feedItemLimit) {
        final ObservableList<FeedItem> result = new ObservableList<>();
        db().getReference("timeline")
                .child(groupId)
                .orderByChild("created")
                .limitToFirst(feedItemLimit)
                .addChildEventListener(new ChildEventAdapter() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Map<String, Object> itemMap = snapshotToMap(dataSnapshot);
                        FeedItem item = FeedItem.builder()
                                .withId(dataSnapshot.getKey())
                                .withUserId((String)itemMap.get("user"))
                                .withGroupId(groupId)
                                .withMarkerId((String)itemMap.get("marker"))
                                .withCreated(longFromMap(itemMap, "created"))
                                .withType(enumFromMap(itemMap, "type", FeedItem.Type.MarkerAdd))
                                .withText((String)itemMap.get("text"))
                                .withTitle((String)itemMap.get("title"))
                                .withThumbnail(uriFromMap(itemMap, "url"))
                                .build();
                        result.add(item);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        removeFromListById(result, dataSnapshot.getKey());
                    }
                });
        return result;
    }

    /**
     * Get a {@link Marker} by its ID.
     * @param groupId group id
     * @param markerId marker id
     * @return marker (asynchronously)
     */
    public static Promise<Marker> getMarkerById(String groupId, String markerId) {
        final PromiseImpl<Marker> res = new PromiseImpl<>();
        db().getReference("markers").child(groupId).child(markerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Marker marker = dataSnapshot.getValue(Marker.class);
                if (marker != null)
                    res.resolve(marker);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                res.reject(databaseError.toException());
            }
        });
        return res;
    }

    /**
     * Get a {@link Category} by its id.
     * @param categoryId category id
     * @return category object (asynchronously)
     */
    public static Promise<Category> getCategoryById(final String categoryId) {
        final PromiseImpl<Category> res = new PromiseImpl<>();
        db().getReference("categories").child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> catMap = snapshotToMap(dataSnapshot);
                Category category = Category.builder()
                        .withId(dataSnapshot.getKey())
                        .withName((String)catMap.get("name"))
                        .withHue((float)doubleFromMap(catMap, "hue"))
                        .build();
                res.resolve(category);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                res.reject(databaseError.toException());
            }
        });
        return res;
    }

    /**
     * Get all markers for the given group. The resulting list should be observed for changes
     * in the markers list.
     * @param groupId group ID
     * @return group markers
     */
    public static ObservableList<Marker> getGroupMarkers(final String groupId) {
        final ObservableList<Marker> res = new ObservableList<>();
        db().getReference("markers").child(groupId).addChildEventListener(new ChildEventAdapter() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map<String, Object> markerMap = snapshotToMap(dataSnapshot);
                Marker marker = Marker.builder()
                        .withId(dataSnapshot.getKey())
                        .withIdGroup(groupId)
                        .withIdUserAuthor((String)markerMap.get("user"))
                        .withIdCategory((String)markerMap.get("category"))
                        .withLocation(new LatLng(doubleFromMap(markerMap, "lat"), doubleFromMap(markerMap, "lng")))
                        .withCommentIds(Collections.<String>emptyList())
                        .withTitle((String)markerMap.get("title"))
                        .withText((String)markerMap.get("text"))
                        .build();
                res.add(marker);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                removeFromListById(res, dataSnapshot.getKey());
            }
        });
        return res;
    }

    private static void removeFromListById(List<? extends Identifiable> list, String id) {
        for (Identifiable identifiable : list) {
            if (id.equals(identifiable.getId())) {
                list.remove(identifiable);
                break;
            }
        }
    }

    private static @Nullable URI uriFromMap(Map<String, Object> map, String key) {
        Object res = map.get(key);
        if (res instanceof String) {
            try {
                return new URI((String) res);
            } catch (URISyntaxException e) {
                return null;
            }
        }
        return null;
    }

    private static <T extends Enum<T>> T enumFromMap(Map<String, Object> map, String key, @NotNull T def) {
        Object res = map.get(key);
        if (res instanceof String) {
            for (Enum anEnum : def.getClass().getEnumConstants()) {
                if (anEnum.name().equals(res))
                    //noinspection unchecked
                    return (T)anEnum;
            }
        }
        return def;
    }

    private static double doubleFromMap(Map<String, Object> map, String key) {
        Object res = map.get(key);
        if (res instanceof Double)
            return (double)res;
        if (res instanceof Float)
            return (float)(double)res;
        return 0;
    }

    private static long longFromMap(Map<String, Object> map, String key) {
        Object res = map.get(key);
        if (res instanceof Long)
            return (long)res;
        return 0;
    }

    private static Map<String, Object> objectToMap(Object object) {
        if (object == null)
            return new HashMap<>();
        if (object instanceof Map) {
            //noinspection unchecked
            return (Map<String, Object>)object;
        }
        return new HashMap<>();
    }

    private static Map<String, Object> snapshotToMap(DataSnapshot snapshot) {
        if (snapshot == null)
            return new HashMap<>();
        return objectToMap(snapshot.getValue());
    }

    private static abstract class ChildEventAdapter implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
