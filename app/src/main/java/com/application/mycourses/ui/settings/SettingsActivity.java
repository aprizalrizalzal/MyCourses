package com.application.mycourses.ui.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.application.mycourses.MainNavActivity;
import com.application.mycourses.R;
import com.application.mycourses.sign.in.SignInActivity;
import com.application.mycourses.ui.settings.reauth.ReAuthenticateActivity;
import com.application.mycourses.ui.utils.LoadingProgress;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        MobileAds.initialize(this, initializationStatus -> {
        });

        AdView adViewSettings = findViewById(R.id.adViewAppBar);
        AdRequest adRequest = new AdRequest.Builder().build();
        adViewSettings.loadAd(adRequest);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CircleImageView imageViewAppBar = findViewById(R.id.imgAppBar);
        Glide.with(getApplication())
                .load(R.mipmap.ic_launcher_round)
                .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round).error(R.mipmap.ic_launcher_round))
                .into(imageViewAppBar);
        TextView appBarMain = findViewById(R.id.tvAppBar);
        appBarMain.setText(getString(R.string.action_settings));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_nav, menu);
        MenuItem itemSearch = menu.findItem(R.id.action_search);
        itemSearch.setVisible(false);
        MenuItem itemSetting = menu.findItem(R.id.action_setting);
        itemSetting.setVisible(false);
        MenuItem itemHelp = menu.findItem(R.id.action_help);
        itemHelp.setOnMenuItemClickListener(menuItem -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
            alertDialogBuilder
                    .setTitle(R.string.action_help)
                    .setMessage(R.string.help_or_questions)
                    .setCancelable(true)
                    .setNeutralButton(R.string.yes, (dialog, id) -> {
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("text/plain");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"aprizal040498@gmail.com"});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.action_settings));

                        if (emailIntent.resolveActivity(this.getPackageManager()) != null) {
                            this.startActivity(emailIntent);
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.bg_costume));
            alertDialog.show();
            return true;
        });
        return true;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private static final int MY_REQUEST_CODE = 99;
        private LoadingProgress loadingProgress;
        private FirebaseAuth firebaseAuth;
        private FirebaseFirestore firebaseFirestore;

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            loadingProgress = new LoadingProgress(requireActivity());
            firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            firebaseFirestore = FirebaseFirestore.getInstance();

            final SwitchPreferenceCompat lockAccount = findPreference(getString(R.string.lock_account));
            final Preference deleteAccount = findPreference(getString(R.string.delete_account));
            final Preference signOutAccount = findPreference(getString(R.string.sign_out));
            Preference language = findPreference(getString(R.string.language));
            Preference review = findPreference(getString(R.string.review));
            Preference checkUpdate = findPreference(getString(R.string.check_update));

            if (lockAccount != null && lockAccount.isChecked()) {
                if (deleteAccount != null) {
                    deleteAccount.setEnabled(false);
                }
            }

            if (lockAccount != null) {
                lockAccount.setOnPreferenceChangeListener((preference, newValue) -> {
                    if (lockAccount.isChecked()){
                        if (deleteAccount != null) {
                            deleteAccount.setEnabled(true);
                        }
                        lockAccount.setChecked(false);
                    }else {
                        if (deleteAccount != null) {
                            deleteAccount.setEnabled(false);
                        }
                        lockAccount.setChecked(true);
                    }
                    return true;
                });
            }

            assert deleteAccount != null;
            deleteAccount.setOnPreferenceClickListener(preference -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireActivity());
                alertDialogBuilder
                        .setTitle(R.string.verification)
                        .setMessage(getString(R.string.next_delete_account))
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, (dialog, id) -> {
                            if (firebaseUser !=null){
                                startActivity(new Intent(requireActivity(), ReAuthenticateActivity.class));
                                requireActivity().overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                                requireActivity().finish();
                            }
                        })
                        .setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());
                AlertDialog alertDialog = alertDialogBuilder.create();
                Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(requireActivity().getDrawable(R.drawable.bg_costume));
                alertDialog.show();
                return true;
            });

            assert signOutAccount != null;
            signOutAccount.setOnPreferenceClickListener(preference -> {
                if (haveConnection()){
                    userSignOut(firebaseAuth,firebaseFirestore);
                    firebaseAuth.signOut();
                    Toast.makeText(requireActivity(),getText(R.string.sign_out) ,Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(requireActivity(), SignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    requireActivity().overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                    requireActivity().finish();
                }else {
                    Toast.makeText(requireActivity(),getText(R.string.not_have_connection),Toast.LENGTH_SHORT).show();
                }
                return true;
            });

            assert language != null;
            language.setOnPreferenceClickListener(preference -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireActivity());
                alertDialogBuilder
                        .setTitle(R.string.language)
                        .setMessage(R.string.settingLanguage)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, (dialog, id) -> dialog.dismiss());
                AlertDialog alertDialog = alertDialogBuilder.create();
                Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(requireActivity().getDrawable(R.drawable.bg_costume));
                alertDialog.show();
                return true;
            });

            assert review != null;
            review.setOnPreferenceClickListener(preference -> {
                Toast.makeText(getActivity(), getString(R.string.review),Toast.LENGTH_SHORT).show();
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"aprizal040498@gmail.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Review \n \n");

                if (emailIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    requireActivity().startActivity(emailIntent);
                }
                return true;
            });

            assert checkUpdate != null;
            checkUpdate.setOnPreferenceClickListener(preference -> {
                Toast.makeText(requireActivity(), getString(R.string.check_update),Toast.LENGTH_SHORT).show();
                checkAppUpdate();
                return true;
            });
        }

        private boolean haveConnection() {
            ConnectivityManager cm = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            assert cm != null;
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }

        private void userSignOut(FirebaseAuth firebaseAuth, FirebaseFirestore firebaseFirestore) {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                String userId = firebaseUser.getUid();
                String saveCurrencyDate,saveCurrencyTime;

                Calendar calendar = Calendar.getInstance();
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
                saveCurrencyDate = dateFormat.format(calendar.getTime());

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                saveCurrencyTime = timeFormat.format(calendar.getTime());

                Map<String, Object> map = new HashMap<>();
                map.put("userSignIn",false);
                map.put("userOnline", false);
                map.put("lastDate", saveCurrencyDate);
                map.put("lastTime",saveCurrencyTime);

                firebaseFirestore.collection(getString(R.string.app_name)).document(userId).update(map);
            }
        }

        private void checkAppUpdate() {
            loadingProgress.startLoadingProgress();
            AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(requireActivity());
            Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
            appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                // Pass the intent that is returned by 'getAppUpdateInfo()'.
                                appUpdateInfo,
                                // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                                AppUpdateType.IMMEDIATE,
                                // The current activity making the update request.
                                requireActivity(),
                                // Include a request code to later monitor this update request.
                                MY_REQUEST_CODE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(requireActivity(),requireActivity().getString(R.string.beta,getString(R.string.app_name)),Toast.LENGTH_SHORT).show();
                loadingProgress.dismissLoadingProgress();
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            if (requestCode == MY_REQUEST_CODE) {
                if (resultCode != RESULT_OK) {
                    Toast.makeText(requireActivity(),resultCode,Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SettingsActivity.this, MainNavActivity.class));
        overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
        finish();
        super.onBackPressed();
    }
}