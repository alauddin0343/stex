
package cz.uhk.stex;


import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.uhk.stex.model.Group;
import cz.uhk.stex.util.Promise;
import cz.uhk.stex.util.Run;
import cz.uhk.stex.util.Util;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity {
    private FrameLayout frame;

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);

            }

            if (FirebaseAuth.getInstance().getCurrentUser() != null && stringValue != null && !stringValue.trim().isEmpty()) {
                if (preference.getKey().equals("user_name")) {
                    Database.changeUserName(FirebaseAuth.getInstance().getCurrentUser(), stringValue);
                } else if (preference.getKey().equals("user_group")) {
                    Database.changeUserGroup(FirebaseAuth.getInstance().getCurrentUser(), stringValue);
                }
            }

            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.activity_settings);

        frame = (FrameLayout)findViewById(R.id.mainPreferenceFragment);
        final ProgressDialog progress = Util.progressDialog(this, "Loading");
        progress.show();

        LoggedInUser.get().successFlat(Run.promiseUi(this, new Promise.SuccessListener<LoggedInUser, Void>() {
            @Override
            public Void onSuccess(LoggedInUser user) {
                prefs.edit().putString("user_name", user.getUser().getName())
                        .putString("user_group", user.getActiveGroup().getId())
                        .apply();
                progress.dismiss();
                MainPreferenceFragment fragment = MainPreferenceFragment.newInstance(user.getGroups());
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.add(frame.getId(), fragment).commit();
                return null;
            }
        }));
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Util.REQUEST_ACTIVITY_PICK_PHOTO) {

            if (resultCode == RESULT_OK) {

                try {
                    Util.uploadPicture(
                            this,
                            getContentResolver(),
                            data.getData(),
                            "avatars",
                            100,
                            new Util.BitmapPictureResizer() {
                                @Override
                                public void onBitmapPictureResized(Bitmap bitmap) {

                                }
                            }
                    ).success(new Promise.SuccessListener<Uri, Object>() {
                        @Override
                        public Object onSuccess(Uri result) throws Exception {
                            Database.changeUserPhotoUri(FirebaseAuth.getInstance().getCurrentUser(), result).success(new Promise.SuccessListener<Void, Object>() {
                                @Override
                                public Object onSuccess(Void result) throws Exception {
                                    Toast.makeText(SettingsActivity.this, "Your avatar was changed", Toast.LENGTH_LONG).show();
                                    return null;
                                }
                            });
                            return null;
                        }
                    });
                } catch (IOException exception) {
                    Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MainPreferenceFragment extends PreferenceFragment {

        public static MainPreferenceFragment newInstance(List<Group> groups) {
            MainPreferenceFragment fragment = new MainPreferenceFragment();
            Bundle args = new Bundle();
            args.putSerializable("groups", new ArrayList<>(groups));
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            @SuppressWarnings("unchecked") List<Group> groups = (List<Group>)getArguments().get("groups");
            addPreferencesFromResource(R.xml.pref_settings);
            setHasOptionsMenu(true);

            final ListPreference listPreference = (ListPreference) findPreference("user_group");
            setListPreferenceData(listPreference, groups);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("user_name"));
            bindPreferenceSummaryToValue(findPreference("user_group"));

            findPreference("logout").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    FirebaseAuth.getInstance().signOut();

                    Intent intent = new Intent(getActivity().getApplicationContext(), StartupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    return true;
                }
            });

            findPreference("avatar").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    getActivity().startActivityForResult(Intent.createChooser(intent, "Select Picture"), Util.REQUEST_ACTIVITY_PICK_PHOTO);
                    return true;
                }
            });
        }

        protected void setListPreferenceData(final ListPreference lp, List<Group> groups) {
            CharSequence[] entries = new CharSequence[groups.size()];
            CharSequence[] entryValues = new CharSequence[entries.length];
            for (int i = 0; i < groups.size(); i++) {
                entries[i] = groups.get(i).getName();
                entryValues[i] = groups.get(i).getId();
            }
            lp.setEntries(entries);
            lp.setEntryValues(entryValues);
            if (groups.size() > 0)
                lp.setDefaultValue(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("user_group", groups.get(0).getId()));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
