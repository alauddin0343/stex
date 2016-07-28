package cz.uhk.cityunavigate;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cz.uhk.cityunavigate.model.Group;
import cz.uhk.cityunavigate.model.User;
import cz.uhk.cityunavigate.util.Promise;
import cz.uhk.cityunavigate.util.PromiseImpl;

/**
 * Holds all information about a logged in user
 */
public class LoggedInUser {
    private final @NotNull FirebaseUser firebaseUser;
    private final @NotNull User user;
    private @NotNull Group activeGroup;
    private List<GroupChangeListener> groupChangeListeners = new ArrayList<>();

    private static @Nullable LoggedInUser instance;

    private LoggedInUser(@NotNull FirebaseUser firebaseUser, @NotNull User user, @NotNull Group activeGroup) {
        this.firebaseUser = firebaseUser;
        this.user = user;
        this.activeGroup = activeGroup;
    }

    private static Promise<FirebaseUser> resolveFirebaseUser() {
        final PromiseImpl<FirebaseUser> res = new PromiseImpl<>();
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    res.resolve(firebaseAuth.getCurrentUser());
                }
            }
        });
        return res;
    }

    private static void listenToPreferenceChanges(final Context context, final LoggedInUser user) {
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                if ("user_group".equals(s)) {
                    loadGroup(context, user.getUser().getId()).success(new Promise.SuccessListener<Group, Void>() {
                        @Override
                        public Void onSuccess(Group result) throws Exception {
                            user.activeGroup = result;
                            for (GroupChangeListener groupChangeListener : user.groupChangeListeners) {
                                groupChangeListener.groupChanged(result);
                            }
                            return null;
                        }
                    });
                }

            }
        });
    }

    private static Promise<Group> loadGroup(Context context, String userId) {
        String settingsGroupId = PreferenceManager.getDefaultSharedPreferences(context).getString("user_group", null);

        if (settingsGroupId == null)
            return Database.getFirstUserGroup(userId);
        else
            return Database.getGroupById(settingsGroupId);
    }

    private static Promise<LoggedInUser> initialize(final Context context) {
        class UserParts {
            private FirebaseUser fbUser;
            private User user;
            private Group group;
        }

        return resolveFirebaseUser().success(new Promise.SuccessListener<FirebaseUser, UserParts>() {
            @Override
            public UserParts onSuccess(FirebaseUser fbUser) {
                UserParts res = new UserParts();
                res.fbUser = fbUser;
                return res;
            }
        }).successFlat(new Promise.SuccessListener<UserParts, Promise<UserParts>>() {
            @Override
            public Promise<UserParts> onSuccess(final UserParts userParts) {
                return Database.getUserById(userParts.fbUser.getUid()).success(new Promise.SuccessListener<User, UserParts>() {
                    @Override
                    public UserParts onSuccess(User result) {
                        userParts.user = result;
                        return userParts;
                    }
                });
            }
        }).successFlat(new Promise.SuccessListener<UserParts, Promise<UserParts>>() {
            @Override
            public Promise<UserParts> onSuccess(final UserParts userParts) {
                Promise.SuccessListener<Group, UserParts> setGroup = new Promise.SuccessListener<Group, UserParts>() {
                    @Override
                    public UserParts onSuccess(Group result) throws Exception {
                        userParts.group = result;
                        return userParts;
                    }
                };
                return loadGroup(context, userParts.fbUser.getUid()).success(setGroup);
            }
        }).success(new Promise.SuccessListener<UserParts, LoggedInUser>() {
            @Override
            public LoggedInUser onSuccess(UserParts result) throws Exception {
                return new LoggedInUser(result.fbUser, result.user, result.group);
            }
        });
    }

    /**
     * Returns promise to the logged in user. The promise becomes resolved
     * as soon as all user info is downloaded.
     * @param context application context (for user prefs)
     * @return logged in user
     */
    public static Promise<LoggedInUser> get(final Context context) {
        if (instance != null) {
            PromiseImpl<LoggedInUser> res = new PromiseImpl<>();
            res.resolve(instance);
            return res;
        } else {
            return initialize(context).success(new Promise.SuccessListener<LoggedInUser, LoggedInUser>() {
                @Override
                public LoggedInUser onSuccess(LoggedInUser result) throws Exception {
                    listenToPreferenceChanges(context, result);
                    return instance = result;
                }
            });
        }
    }

    @NotNull
    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    @NotNull
    public User getUser() {
        return user;
    }

    @NotNull
    public Group getActiveGroup() {
        return activeGroup;
    }

    @Nullable
    public static LoggedInUser getInstance() {
        return instance;
    }

    /**
     * Listen to user group change events
     * @param listener listener
     */
    public void addGroupChangeListener(@NotNull GroupChangeListener listener) {
        groupChangeListeners.add(listener);
    }

    public interface GroupChangeListener {
        void groupChanged(Group newGroup);
    }
}
