package cz.uhk.cityunavigate;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cz.uhk.cityunavigate.model.Category;
import cz.uhk.cityunavigate.model.FeedItem;
import cz.uhk.cityunavigate.model.Group;
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
                        Group group = dataSnapshot.getValue(Group.class);
                        if (group != null)
                            result.add(group);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Group group = dataSnapshot.getValue(Group.class);
                        if (group != null)
                            result.remove(group);
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
    public static ObservableList<FeedItem> getGroupFeed(@NotNull String groupId, final int feedItemLimit) {
        final ObservableList<FeedItem> result = new ObservableList<>();
        db().getReference("timeline")
                .child(groupId)
                .orderByChild("created")
                .limitToFirst(feedItemLimit)
                .addChildEventListener(new ChildEventAdapter() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        FeedItem item = dataSnapshot.getValue(FeedItem.class);
                        if (item != null)
                            result.add(item);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        FeedItem item = dataSnapshot.getValue(FeedItem.class);
                        if (item != null)
                            result.remove(item);
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
    public static Promise<Category> getCategoryById(String categoryId) {
        final PromiseImpl<Category> res = new PromiseImpl<>();
        db().getReference("categories").child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Category category = dataSnapshot.getValue(Category.class);
                if (category != null)
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
    public static ObservableList<Marker> getGroupMarkers(String groupId) {
        final ObservableList<Marker> res = new ObservableList<>();
        db().getReference("markers").child(groupId).addChildEventListener(new ChildEventAdapter() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Marker marker = dataSnapshot.getValue(Marker.class);
                if (marker != null)
                    res.add(marker);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Marker marker = dataSnapshot.getValue(Marker.class);
                if (marker != null)
                    res.remove(marker);
            }
        });
        return res;
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
