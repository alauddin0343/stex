
package cz.uhk.stex;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StreamDownloadTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.uhk.stex.model.Category;
import cz.uhk.stex.model.Comment;
import cz.uhk.stex.model.FeedItem;
import cz.uhk.stex.model.Group;
import cz.uhk.stex.model.Identifiable;
import cz.uhk.stex.model.Marker;
import cz.uhk.stex.model.User;
import cz.uhk.stex.util.Cache;
import cz.uhk.stex.util.Function;
import cz.uhk.stex.util.ObservableList;
import cz.uhk.stex.util.Promise;
import cz.uhk.stex.util.PromiseImpl;
import cz.uhk.stex.util.Run;
import cz.uhk.stex.util.Supplier;
import cz.uhk.stex.util.Util;

/**
 * Database access
 */
public class Database {
    private static FirebaseDatabase db() {
        return FirebaseDatabase.getInstance();
    }

    private static final Cache<Uri, Bitmap> imageCache = new Cache<>(64);
    private static final Cache<String, User> userCache = new Cache<>(1024);
    private static final Cache<String, Marker> markerCache = new Cache<>(1024);
    private static final Cache<String, Group> groupCache = new Cache<>(1024);
    private static final Cache<String, Category> categoryCache = new Cache<>(1024);

    /**
     * Accept all existing and future group invitations for the given user.
     *
     * @param user logged in user
     */
    public static Promise<FirebaseUser> acceptInvitations(@NotNull final FirebaseUser user) {
        final PromiseImpl<FirebaseUser> resPromise = new PromiseImpl<>();
        final String md5Mail = Util.MD5(user.getEmail());
        DatabaseReference invitations = db()
                .getReference("invitations")
                .child(md5Mail)
                .child("groups");

        invitations.addChildEventListener(new ChildEventAdapter() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String s) {
                String groupId = snapshot.getKey();
                DatabaseReference groupUsers = db()
                        .getReference("groups")
                        .child(groupId)
                        .child("users")
                        .child(user.getUid());
                Promise<Void> groupUsersPromise = Promise.fromTask(groupUsers.setValue(System.currentTimeMillis()));

                DatabaseReference userGroups = db()
                        .getReference("users")
                        .child(user.getUid())
                        .child("groups")
                        .child(groupId);
                Promise<Void> userGroupsPromise = Promise.fromTask(userGroups.setValue(System.currentTimeMillis()));

                Promise<Void> removePromise = Promise.fromTask(snapshot.getRef().removeValue());
                Promise<Void> groupRemovePromise = Promise.fromTask(db().getReference("groups").child(groupId).child("invitations").child(md5Mail).removeValue());
                Promise.all(Arrays.asList(groupUsersPromise, userGroupsPromise, removePromise, groupRemovePromise))
                        .success(new Promise.SuccessListener<List<Void>, Void>() {
                            @Override
                            public Void onSuccess(List<Void> result) throws Exception {
                                resPromise.resolve(user);
                                return null;
                            }
                        }).error(new Promise.ErrorListener<Void>() {
                            @Override
                            public Void onError(Throwable error) {
                                resPromise.reject(error);
                                return null;
                            }
                        });
            }

        });
        return resPromise;
    }

    /**
     * Adds a freshly registered user to the database. Does nothing if the user is already
     * registered in the database.
     *
     * @param user registered user
     */
    public static Promise<FirebaseUser> registerUserToDatabase(@NotNull final FirebaseUser user) {
        final DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid());

        final PromiseImpl<FirebaseUser> resPromise = new PromiseImpl<>();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Add the user if not present
                    final String displayName = user.getDisplayName() != null ? user.getDisplayName() : emailToUserName(user.getEmail());
                    Promise.fromTask(userRef.setValue(new HashMap<String, Object>() {{
                        put("created", System.currentTimeMillis());
                        put("email", user.getEmail());
                        put("name", displayName);
                    }})).successFlat(new Promise.SuccessListener<Void, Promise<Void>>() {
                        @Override
                        public Promise<Void> onSuccess(Void result) throws Exception {
                            return Promise.fromTask(user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(displayName).build()));
                        }
                    }).success(new Promise.SuccessListener<Void, Void>() {
                        @Override
                        public Void onSuccess(Void result) throws Exception {
                            resPromise.resolve(user);
                            return null;
                        }
                    }).error(new Promise.ErrorListener<Void>() {
                        @Override
                        public Void onError(Throwable error) {
                            resPromise.reject(error);
                            return null;
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                resPromise.reject(databaseError.toException());
            }
        });
        return resPromise;
    }

    private static String emailToUserName(String email) {
        if (email.indexOf('@') < 0)
            return email;
        String namePart = email.substring(0, email.indexOf('@'));
        String[] parts = namePart.split("\\.");
        StringBuilder res = new StringBuilder();
        for (String part : parts) {
            if (part.length() > 0)
                res.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase()).append(' ');
        }
        return res.toString().trim();
    }

    /**
     * Returns the list of groups the user is enrolled in. You can add observers
     * to the list to react when the user is added/removed to/from a group.
     *
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
                            getGroupById(groupId).success(new Promise.SuccessListener<Group, Void>() {
                                @Override
                                public Void onSuccess(Group group) {
                                    result.add(group);
                                    return null;
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
     * Returns the first group of a user
     * @param userId user id
     * @return first group (asynchronously)
     */
    public static Promise<Group> getFirstUserGroup(String userId) {
        return childObjectAsPromise(db().getReference("users")
                .child(userId)
                .child("groups")).successFlat(new Promise.SuccessListener<ChildObject, Promise<Group>>() {
                    @Override
                    public Promise<Group> onSuccess(ChildObject result) throws Exception {
                        if (result.value.isEmpty())
                            throw new Exception("User has no groups");
                        return Database.getGroupById(result.value.keySet().iterator().next());
                    }
                });
    }

    /**
     * Returns a list of comments for the given marker
     *
     * @param markerId marker ID
     * @return marker comments
     */
    public static ObservableList<Comment> getCommentsForMarker(String markerId) {
        final ObservableList<Comment> res = new ObservableList<>();
        db().getReference("comments").child(markerId).addChildEventListener(new ChildEventAdapter() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map<String, Object> commentMap = snapshotToMap(dataSnapshot);
                Comment comment = Comment.builder()
                        .withId(dataSnapshot.getKey())
                        .withCreated(longFromMap(commentMap, "created"))
                        .withImage(uriFromMap(commentMap, "image"))
                        .withText((String) commentMap.get("text"))
                        .withUserId((String) commentMap.get("user"))
                        .build();
                res.add(comment);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                removeFromListById(res, dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                onChildRemoved(dataSnapshot);
                onChildAdded(dataSnapshot, s);
            }
        });
        return res;
    }

    /**
     * Gets feed for the given group. The resulting list should be observed
     * for any changes in the feed.
     *
     * @param groupId       group to get the feed for
     * @param feedItemLimit maximum number of items to fetch
     * @return group feed
     */
    public static ObservableList<FeedItem> getGroupFeed(@NotNull final String groupId, final int feedItemLimit) {
        final ObservableList<FeedItem> result = new ObservableList<>();
        db().getReference("timeline")
                .child(groupId)
                .orderByChild("created")
                .limitToLast(feedItemLimit)
                .addChildEventListener(new ChildEventAdapter() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Map<String, Object> itemMap = snapshotToMap(dataSnapshot);
                        Map<String, Object> readByMapRaw = objectToMap(itemMap.get("read"));
                        Map<String, Long> readByMap = new HashMap<>();
                        for (Map.Entry<String, Object> entry : readByMapRaw.entrySet()) {
                            if (entry.getValue() instanceof Long)
                                readByMap.put(entry.getKey(), (Long)entry.getValue());
                        }

                        FeedItem item = FeedItem.builder()
                                .withId(dataSnapshot.getKey())
                                .withUserId((String) itemMap.get("user"))
                                .withGroupId(groupId)
                                .withMarkerId((String) itemMap.get("marker"))
                                .withCreated(longFromMap(itemMap, "created"))
                                .withType(enumFromMap(itemMap, "type", FeedItem.Type.MarkerAdd))
                                .withText((String) itemMap.get("text"))
                                .withTitle((String) itemMap.get("title"))
                                .withThumbnail(uriFromMap(itemMap, "thumbnail"))
                                .withReadBy(readByMap)
                                .build();
                        result.add(item);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        removeFromListById(result, dataSnapshot.getKey());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        onChildRemoved(dataSnapshot);
                        onChildAdded(dataSnapshot, s);
                    }
                });
        return result;
    }

    /**
     * Waits for a feed item to become read by the given user.
     * @param feedItem feed item to observe
     * @param userId user ID to check for
     * @param listener action to be performed
     */
    public static void observeFeedItemRead(final FeedItem feedItem, final String userId, final FeedItemReadListener listener) {
        final DatabaseReference ref = db().getReference("timeline").child(feedItem.getGroupId()).child(feedItem.getId()).child("read")
                .child(userId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() instanceof Long) {
                    listener.itemRead(feedItem, userId);
                    ref.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ref.removeEventListener(this);
            }
        });
    }

    public interface FeedItemReadListener {
        void itemRead(FeedItem feedItem, String readByUserId);
    }

    /**
     * Marks the given {@link FeedItem} as read
     * @param feedItem feed item
     * @param userId user that read the message
     * @return promise that gets fulfilled when the item is marked as read
     */
    public static Promise<Void> markFeedItemAsRead(FeedItem feedItem, String userId) {
        return Promise.fromTask(db().getReference("timeline").child(feedItem.getGroupId()).child(feedItem.getId()).child("read")
                .child(userId).setValue(System.currentTimeMillis()));
    }

    /**
     * Get a {@link Marker} by its ID.
     *
     * @param groupId  group id
     * @param markerId marker id
     * @return marker (asynchronously)
     */
    public static Promise<Marker> getMarkerById(final String groupId, String markerId) {
        return getItemById(markerId, db().getReference("markers").child(groupId), markerCache, new Function<ChildObject, Marker>() {
            @Override
            public Marker apply(ChildObject result) {
                return Marker.builder()
                        .withId(result.id)
                        .withIdGroup(groupId)
                        .withIdUserAuthor((String) result.value.get("user"))
                        .withIdCategory((String) result.value.get("category"))
                        .withLocation(new LatLng(doubleFromMap(result.value, "lat"), doubleFromMap(result.value, "lng")))
                        .withTitle((String) result.value.get("title"))
                        .withText((String) result.value.get("text"))
                        .withCreated(longFromMap(result.value, "created"))
                        .withImage(uriFromMap(result.value, "image"))
                        .build();
            }
        });
    }

    /**
     * Get a {@link Category} by its id.
     *
     * @param categoryId category id
     * @return category object (asynchronously)
     */
    public static Promise<Category> getCategoryById(final String categoryId) {
        return getItemById(categoryId, db().getReference("categories"), categoryCache, new Function<ChildObject, Category>() {
            @Override
            public Category apply(ChildObject result) {
                return Category.builder()
                        .withId(result.id)
                        .withName((String) result.value.get("title"))
                        .withHue((float) doubleFromMap(result.value, "color"))
                        .build();
            }
        });
    }

    /**
     * Returns observable collection of all marker categories.
     * @return all categories
     */
    public static ObservableList<Category> getAllCategories() {
        final ObservableList<Category> res = new ObservableList<>();
        db().getReference("categories")
                .addChildEventListener(new ChildEventAdapter() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Map<String, Object> categoryMap = snapshotToMap(dataSnapshot);
                        Category category = Category.builder()
                                .withId(dataSnapshot.getKey())
                                .withName((String)categoryMap.get("title"))
                                .withHue((float)doubleFromMap(categoryMap, "color"))
                                .build();
                        res.add(category);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        removeFromListById(res, dataSnapshot.getKey());
                    }
                });
        return res;
    }

    /**
     * Resolve user info based on the user ID.
     *
     * @param userId user ID
     * @return user (asynchronously)
     */
    public static Promise<User> getUserById(final String userId) {
        return getItemById(userId, db().getReference("users"), userCache, new Function<ChildObject, User>() {
            @Override
            public User apply(ChildObject result) {
                return User.builder()
                        .withId(result.id)
                        .withName((String) result.value.get("name"))
                        .withEmail((String) result.value.get("email"))
                        .withActiveGroup((String)result.value.get("activeGroup"))
                        .withGroups(new ArrayList<>(objectToMap(result.value.get("groups")).keySet()))
                        .withAdministrators(new ArrayList<>(objectToMap(result.value.get("administrator")).keySet()))
                        .withImage(uriFromMap(result.value, "image"))
                        .withCreated(longFromMap(result.value, "created"))
                        .build();
            }
        });
    }

    /**
     * Listen to changes in active user group
     * @param userId user id
     * @param listener listener
     */
    public static void addUserChangeListener(final String userId, final UserChangeListener listener) {
        db().getReference("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getUserById(userId).success(new Promise.SuccessListener<User, Void>() {
                    @Override
                    public Void onSuccess(User result) throws Exception {
                        listener.userChanged(result);
                        return null;
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public interface UserChangeListener {
        void userChanged(@Nullable User newUser);
    }

    /**
     * Resolve group info based on the group ID.
     *
     * @param groupId group ID
     * @return group (asynchronously)
     */
    public static Promise<Group> getGroupById(final String groupId) {
        return getItemById(groupId, db().getReference("groups"), groupCache, new Function<ChildObject, Group>() {
            @Override
            public Group apply(ChildObject groupMap) {
                Map<String, Object> userMap = objectToMap(groupMap.value.get("users"));
                List<String> users = new ArrayList<>(userMap.keySet());
                return Group.builder()
                        .withId(groupMap.id)
                        .withName((String) groupMap.value.get("name"))
                        .withUniversity((String) groupMap.value.get("university"))
                        .withAdminsIds(Collections.singletonList((String) groupMap.value.get("administrator")))
                        .withUserIds(users)
                        .build();
            }
        });
    }

    /**
     * Get all markers for the given group. The resulting list should be observed for changes
     * in the markers list.
     *
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
                        .withIdUserAuthor((String) markerMap.get("user"))
                        .withIdCategory((String) markerMap.get("category"))
                        .withLocation(new LatLng(doubleFromMap(markerMap, "lat"), doubleFromMap(markerMap, "lng")))
                        .withTitle((String) markerMap.get("title"))
                        .withText((String) markerMap.get("text"))
                        .withCreated(longFromMap(markerMap, "created"))
                        .withImage(uriFromMap(markerMap, "image"))
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

    /**
     * Adds a new comment to the given marker. Returns comment with a filled-in ID as soon
     * as the comment is stored in the database.
     *
     * @param markerId marker id
     * @param comment  comment to add
     * @return comment with database ID
     */
    public static Promise<Comment> addComment(String markerId, @NotNull final Comment comment) {
        final PromiseImpl<Comment> res = new PromiseImpl<>();
        final DatabaseReference commentRef = db().getReference("comments").child(markerId).push();
        commentRef.setValue(new HashMap<String, Object>() {{
            put("created", comment.getCreated());
            if (comment.getImage() != null)
                put("image", comment.getImage().toString());
            put("text", comment.getText());
            put("user", comment.getUserId());
        }}).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                res.resolve(Comment.builder(comment).withId(commentRef.getKey()).build());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                res.reject(e);
            }
        });
        return res;
    }

    /**
     * Adds a new feed item to the database. Returns a new Feed Item with the proper database
     * ID set.
     *
     * @param groupId  group id
     * @param feedItem feed item to add
     * @return promise to a stored feed item
     */
    public static Promise<FeedItem> addFeedItem(String groupId, final FeedItem feedItem) {
        final PromiseImpl<FeedItem> res = new PromiseImpl<>();
        final DatabaseReference commentRef = db().getReference("timeline").child(groupId).push();
        commentRef.setValue(new HashMap<String, Object>() {{
            put("created", feedItem.getCreated());
            put("marker", feedItem.getMarkerId());
            put("text", feedItem.getText());
            put("title", feedItem.getTitle());
            put("type", feedItem.getType().toString());
            if (feedItem.getThumbnail() != null)
                put("thumbnail", feedItem.getThumbnail().toString());
            put("user", feedItem.getUserId());
            put("read", new HashMap<String, Object>() {{
               put(feedItem.getUserId(), System.currentTimeMillis());
            }});
        }}).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                res.resolve(FeedItem.builder(feedItem).withId(commentRef.getKey()).build());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                res.reject(e);
            }
        });
        return res;
    }

    /**
     * Add a new marker. Returns a promise to the database marker with a proper database ID set.
     *
     * @param groupId group ID
     * @param marker  marker to save
     * @return database marker promise
     */
    public static Promise<Marker> addMarker(String groupId, final Marker marker) {
        final PromiseImpl<Marker> res = new PromiseImpl<>();
        final DatabaseReference markerRef = db().getReference("markers").child(groupId).push();
        markerRef.setValue(new HashMap<String, Object>() {{
            put("category", marker.getIdCategory());
            put("created", marker.getCreated());
            if (marker.getImage() != null)
                put("image", marker.getImage().toString());
            put("lat", marker.getLocation().latitude);
            put("lng", marker.getLocation().longitude);
            put("text", marker.getText());
            put("title", marker.getTitle());
            put("user", marker.getIdUserAuthor());
        }}).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                res.resolve(Marker.builder().withId(markerRef.getKey()).build());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                res.reject(e);
            }
        });
        return res;
    }

    /**
     * Change user name to the specified name
     * @param user user
     * @param newName new name
     * @return promise
     */
    public static Promise<Void> changeUserName(final FirebaseUser user, final String newName) {
        return Promise.fromTask(user.updateProfile(new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .setPhotoUri(user.getPhotoUrl())
                .build())).successFlat(new Promise.SuccessListener<Void, Promise<Void>>() {
                    @Override
                    public Promise<Void> onSuccess(Void result) {
                        return Promise.fromTask(
                            db().getReference("users")
                                    .child(user.getUid())
                                    .child("name")
                                    .setValue(newName));
                    }
                });
    }

    /**
     * Change user photo to the specified url
     * @param user user
     * @param photoUri new photo Url
     * @return promise
     */
    public static Promise<Void> changeUserPhotoUri(final FirebaseUser user, final Uri photoUri) {
        return Promise.fromTask(user.updateProfile(new UserProfileChangeRequest.Builder()
                .setPhotoUri(photoUri)
                .setPhotoUri(user.getPhotoUrl())
                .build())).successFlat(new Promise.SuccessListener<Void, Promise<Void>>() {
            @Override
            public Promise<Void> onSuccess(Void result) {
                return Promise.fromTask(
                        db().getReference("users")
                                .child(user.getUid())
                                .child("image")
                                .setValue(photoUri.toString()));
            }
        });
    }

    /**
     * Change user name to the specified name
     * @param user user
     * @param newGroup new name
     * @return promise
     */
    public static Promise<Void> changeUserGroup(final FirebaseUser user, final String newGroup) {
        return Promise.fromTask(
                db().getReference("users")
                        .child(user.getUid())
                        .child("activeGroup")
                        .setValue(newGroup));
    }

    /**
     * Downloads a {@link Bitmap} from Firebase storage
     * @param uri image URI
     * @return downloaded image
     */
    public static Promise<Bitmap> downloadImage(final Uri uri) {
        final PromiseImpl<Bitmap> res = new PromiseImpl<>();
        Bitmap cached = imageCache.getItem(uri);
        if (cached != null) {
            res.resolve(cached);
            return res;
        }

        Run.runAsync(new Supplier<Void>() {
            @Override
            public Void supply() throws Exception {
                FirebaseStorage.getInstance().getReferenceFromUrl(uri.toString()).getStream().addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
                        final Bitmap bitmap = BitmapFactory.decodeStream(taskSnapshot.getStream());
                        if (bitmap != null)
                            imageCache.cacheItem(uri, bitmap);
                        res.resolve(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        res.reject(e);
                    }
                });
                return null;
            }
        });

        return res;
    }


    private static class ChildObject {
        private final String id;
        private final Map<String, Object> value;

        private ChildObject(String id, Map<String, Object> value) {
            this.id = id;
            this.value = value;
        }
    }

    /**
     * Loads an object from database based on its ID and caches it. Also adds a value change
     * listener to update the cache if the object changed in the database.
     * @param itemId object identifier
     * @param path path to the object holder ("users" or "groups" etc.)
     * @param cache cache reference for the given object
     * @param factory factory to convert from Map to the object instance
     * @param <T> object type
     * @return promise to the loaded object
     */
    private static <T extends Identifiable> Promise<T> getItemById(final String itemId, DatabaseReference path, final Cache<String, T> cache, final Function<ChildObject, T> factory) {
        T cached = cache.getItem(itemId);
        if (cached != null) {
            return Promise.resolved(cached);
        }

        Promise<T> r = childObjectAsPromise(path.child(itemId))
                .success(new Promise.SuccessListener<ChildObject, T>() {
                    @Override
                    public T onSuccess(ChildObject result) {
                        T loaded = factory.apply(result);
                        cache.cacheItem(result.id, loaded);
                        return loaded;
                    }
                });

        path.child(itemId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChildObject result = new ChildObject(dataSnapshot.getKey(), snapshotToMap(dataSnapshot));
                T loaded = factory.apply(result);
                cache.cacheItem(result.id, loaded);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return r;
    }

    /**
     * Returns the object-value of the given database reference as a promise to the value
     * @param reference reference to load
     * @return promise to the reference value
     */
    private static Promise<ChildObject> childObjectAsPromise(DatabaseReference reference) {
        final PromiseImpl<ChildObject> res = new PromiseImpl<>();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                res.resolve(new ChildObject(dataSnapshot.getKey(), snapshotToMap(dataSnapshot)));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                res.reject(databaseError.toException());
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

    private static
    @Nullable
    Uri uriFromMap(Map<String, Object> map, String key) {
        Object res = map.get(key);
        if (res instanceof String) {
            return Uri.parse((String) res);
        }
        return null;
    }

    private static <T extends Enum<T>> T enumFromMap(Map<String, Object> map, String key, @NotNull T def) {
        Object res = map.get(key);
        if (res instanceof String) {
            for (Enum anEnum : def.getClass().getEnumConstants()) {
                if (anEnum.name().equals(res))
                    //noinspection unchecked
                    return (T) anEnum;
            }
        }
        return def;
    }

    private static double doubleFromMap(Map<String, Object> map, String key) {
        Object res = map.get(key);
        if (res instanceof Double)
            return (double) res;
        if (res instanceof Float)
            return (float) (double) res;
        if (res instanceof Long)
            return (double) (long) res;
        return 0;
    }

    private static long longFromMap(Map<String, Object> map, String key) {
        Object res = map.get(key);
        if (res instanceof Long)
            return (long) res;
        return 0;
    }

    private static Map<String, Object> objectToMap(Object object) {
        if (object == null)
            return new HashMap<>();
        if (object instanceof Map) {
            //noinspection unchecked
            return (Map<String, Object>) object;
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
