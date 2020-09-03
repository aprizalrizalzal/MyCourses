package com.application.mycourses.ui.settings.reauth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.application.mycourses.MainNavActivity;
import com.application.mycourses.R;
import com.application.mycourses.sign.in.SignInActivity;
import com.application.mycourses.ui.utils.LoadingProgress;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReAuthenticateActivity extends AppCompatActivity {
    private FirebaseUser firebaseUser;
    private LoadingProgress loadingProgress;
    private EditText edtEmail,edtPassword;
    private String email,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_re_authenticate);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CircleImageView imageViewAppBar = findViewById(R.id.imgAppBar);
        Glide.with(getApplication())
                .load(R.mipmap.ic_launcher_round)
                .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round).error(R.mipmap.ic_launcher_round))
                .into(imageViewAppBar);
        TextView appBar = findViewById(R.id.tvAppBar);
        appBar.setText(getText(R.string.verification));

        Calendar welcome = Calendar.getInstance();
        int timeOfDay = welcome.get(Calendar.HOUR_OF_DAY);

        TextView run = findViewById(R.id.tvRun);
        if (timeOfDay <12){
            run.setText(getString(R.string.welcome,getString(R.string.morning)));
            run.setSelected(true);
        } else if (timeOfDay >12 && timeOfDay <18){
            run.setText(getString(R.string.welcome,getString(R.string.afternoon)));
            run.setSelected(true);
        }else if (timeOfDay > 18) {
            run.setText(getString(R.string.welcome, getString(R.string.night)));
            run.setSelected(true);
        }

        MobileAds.initialize(this, initializationStatus -> {
        });
        AdView adView = findViewById(R.id.adViewAppBar);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        loadingProgress = new LoadingProgress(ReAuthenticateActivity.this);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        Button btnVer = findViewById(R.id.btnVerification);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        btnVer.setOnClickListener(view -> {
            if (haveConnection()) {
                if (!validEmail()||!validPassword()){
                    return;
                }
                verificationUser(firebaseUser);
            }
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void verificationUser(FirebaseUser firebaseUser) {
        loadingProgress.startLoadingProgress();
        email = edtEmail.getText().toString();
        password = edtPassword.getText().toString();

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        firebaseUser.reauthenticate(credential).addOnCompleteListener(taskVer -> {
            if (taskVer.isSuccessful()){

                loadingProgress.dismissLoadingProgress();
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ReAuthenticateActivity.this);
                alertDialogBuilder
                        .setTitle(R.string.delete_account)
                        .setMessage(getString(R.string.verify_account))
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, (dialog, id) -> {
                            loadingProgress.dismissLoadingProgress();
                            deleteAuthUser(firebaseUser);
                                })
                        .setNegativeButton(R.string.no, (dialog, id) -> {
                            loadingProgress.dismissLoadingProgress();
                            edtEmail.setText(null);
                            edtPassword.setText(null);
                            dialog.cancel();
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.bg_costume));
                alertDialog.show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(ReAuthenticateActivity.this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
            loadingProgress.dismissLoadingProgress();
        });
    }

    private void deleteAuthUser(FirebaseUser firebaseUser) {
        loadingProgress.startLoadingProgress();
        firebaseUser.delete().addOnSuccessListener(aVoidAuth -> {
            Toast.makeText(ReAuthenticateActivity.this, getText(R.string.delete_auth_account),Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ReAuthenticateActivity.this, SignInActivity.class));
            overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
            loadingProgress.dismissLoadingProgress();
            finish();

        }).addOnFailureListener(e -> {
            loadingProgress.dismissLoadingProgress();
            Toast.makeText(ReAuthenticateActivity.this, getText(R.string.delete_auth_account_failed),Toast.LENGTH_SHORT).show();
        });
    }

    private boolean haveConnection() {
        ConnectivityManager cm = (ConnectivityManager) ReAuthenticateActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private boolean validEmail(){
        email = edtEmail.getText().toString();
        if (email.isEmpty()){
            edtEmail.setError(getString(R.string.field_not_empty));
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            edtEmail.setError(getText(R.string.not_valid_email));
            return false;
        } else {
            edtEmail.setError(null);
            return true;
        }
    }

    private boolean validPassword(){
        password = edtPassword.getText().toString();
        if (password.isEmpty()){
            edtPassword.setError(getString(R.string.field_not_empty));
            return false;
        } else if (edtPassword.getText().length() < 6){
            edtPassword.setError(getText(R.string.not_valid_password));
            return false;
        } else {
            edtPassword.setError(null);
            return true;
        }
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
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ReAuthenticateActivity.this);
            alertDialogBuilder
                    .setTitle(R.string.action_help)
                    .setMessage(R.string.help_or_questions)
                    .setCancelable(true)
                    .setNeutralButton(R.string.yes, (dialog, id) -> {
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("text/plain");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"aprizal040498@gmail.com"});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.verification) + "\n \n");

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ReAuthenticateActivity.this, MainNavActivity.class));
        overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
        finish();
    }
}