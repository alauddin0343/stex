package cz.uhk.cityunavigate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
                firebaseAuth.removeAuthStateListener(this);
                final FirebaseUser loggedInUser = firebaseAuth.getCurrentUser();
                if (loggedInUser == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(StartupActivity.this, LoginActivity.class));
                            finish();
                        }
                    });
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(StartupActivity.this, MainActivity.class));
                        finish();
                    }
                });
            }
        });
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
