package cz.uhk.stex;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import cz.uhk.stex.util.Promise;
import cz.uhk.stex.util.Run;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String email;
        if (mEmailView.getText() != null) {
            email = mEmailView.getText().toString();
        } else {
            email = "";
        }

        final String password;
        if (mPasswordView.getText() != null) {
            password = mPasswordView.getText().toString();
        } else {
            password = "";
        }

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        // Check for a valid password, if the user entered one.
        else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            final Promise.SuccessListener<FirebaseUser, Void> onSuccess = new Promise.SuccessListener<FirebaseUser, Void>() {
                @Override
                public Void onSuccess(FirebaseUser result) throws Exception {
                    finish();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    return null;
                }
            };

            final Promise.ErrorListener<Void> onError = new Promise.ErrorListener<Void>() {
                @Override
                public Void onError(Throwable error) {
                    if (error instanceof FirebaseAuthUserCollisionException) {
                        // User already exists
                        Promise.fromTask(FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password))
                                .success(new Promise.SuccessListener<AuthResult, FirebaseUser>() {
                                    @Override
                                    public FirebaseUser onSuccess(AuthResult result) throws Exception {
                                        return result.getUser();
                                    }
                                })
                                .success(onSuccess)
                                .error(this);
                        return null;
                    }
                    showProgress(false);
                    Log.e("login", "Error signing in", error);
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                    return null;
                }
            };

            Promise.fromTask(FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password))
                    .successFlat(new Promise.SuccessListener<AuthResult, Promise<FirebaseUser>>() {
                        @Override
                        public Promise<FirebaseUser> onSuccess(AuthResult result) throws Exception {
                            return Database.registerUserToDatabase(result.getUser());
                        }
                    })
                    .successFlat(new Promise.SuccessListener<FirebaseUser, Promise<FirebaseUser>>() {
                        @Override
                        public Promise<FirebaseUser> onSuccess(FirebaseUser result) throws Exception {
                            return Database.insertUserToGroup(result, "-KOGmIa_p-nEJp26XzHi");
//                            return Database.acceptInvitations(result);
                        }
                    })
                    .successFlat(Run.promiseUi(this, onSuccess))
                    .errorFlat(Run.promiseUi(this, onError));
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

