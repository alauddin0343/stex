package cz.uhk.cityunavigate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Decides whether to switch to login activity or directly to main activity
 * if we have stored the credentials already.
 */
public class StartupActivity extends Activity {
    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser loggedInUser = firebaseAuth.getCurrentUser();
                if (loggedInUser == null)
                    return;

                DatabaseReference invitations = FirebaseDatabase.getInstance()
                        .getReference("invitations")
                        .child(Util.MD5(loggedInUser.getEmail()))
                        .child("groups");
                invitations.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String groupId = snapshot.getKey();
                            DatabaseReference groupUsers = FirebaseDatabase.getInstance()
                                    .getReference("groups")
                                    .child(groupId)
                                    .child("users")
                                    .child(loggedInUser.getUid());
                            groupUsers.setValue(System.currentTimeMillis());

                            DatabaseReference userGroups = FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(loggedInUser.getUid())
                                    .child("groups")
                                    .child(groupId);
                            userGroups.setValue(System.currentTimeMillis());

                            snapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                final DatabaseReference userRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(loggedInUser.getUid());

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            // Add the user if not present
                            userRef.child("created").setValue(System.currentTimeMillis());
                            userRef.child("email").setValue(loggedInUser.getEmail());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder()
                            .setProviders("email")
                            .build(),
                    RC_SIGN_IN);
            return;
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode);
        }
    }

    @MainThread
    private void handleSignInResponse(int resultCode) {
        if (resultCode == RESULT_OK) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Login cancelled
        finish();
    }
}
