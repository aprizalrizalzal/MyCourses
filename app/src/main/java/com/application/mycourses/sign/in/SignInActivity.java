package com.application.mycourses.sign.in;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import com.application.mycourses.MainNavActivity;
import com.application.mycourses.R;
import com.application.mycourses.sign.up.SignUpActivity;
import com.application.mycourses.ui.utils.LoadingProgress;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private EditText edtEmail,edtPassword;
    private String email,password;
    private LoadingProgress loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CircleImageView imageViewAppBar = findViewById(R.id.imgAppBar);
        Glide.with(getApplication())
                .load(R.mipmap.ic_launcher_round)
                .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                .into(imageViewAppBar);
        TextView appBar = findViewById(R.id.tvAppBar);
        appBar.setText(getText(R.string.sign_in));

        loadingProgress=new LoadingProgress(this);

        Calendar welcome = Calendar.getInstance();
        int timeOfDay = welcome.get(Calendar.HOUR_OF_DAY);

        TextView run = findViewById(R.id.tvRun);
        if (timeOfDay >=0 && timeOfDay <12){
            run.setText(getString(R.string.welcome,getString(R.string.morning)));
            run.setSelected(true);
        } else if (timeOfDay >=12 && timeOfDay <18){
            run.setText(getString(R.string.welcome,getString(R.string.afternoon)));
            run.setSelected(true);
        }else if (timeOfDay >=18 && timeOfDay <24) {
            run.setText(getString(R.string.welcome, getString(R.string.night)));
            run.setSelected(true);
        }

        MobileAds.initialize(this, initializationStatus -> {
        });
        AdView adView = findViewById(R.id.adViewAppBar);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        firebaseAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        Button btnSignIn = findViewById(R.id.btnSignIn);
        TextView tvSignInUp = findViewById(R.id.tvSignInUp);
        Button btnSend = findViewById(R.id.btnSend);
        TextView tvForgetPassword = findViewById(R.id.tvForgetPassword);

        btnSignIn.setOnClickListener(view -> {
            if (haveConnection()){
                if (!validEmail()||!validPassword()){
                    return;
                }
                userSignIn(firebaseAuth);
            }else {
                Toast.makeText(this, getString(R.string.not_have_connection),Toast.LENGTH_SHORT).show();
            }
        });

        tvSignInUp.setOnClickListener(view -> {
            startActivity(new Intent(this, SignUpActivity.class));
            overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
            finish();
        });

        btnSend.setOnClickListener(view -> {
            if (haveConnection()){
                if (!validEmail()){
                    return;
                }
                userSend(firebaseAuth);
            } else {
                Toast.makeText(this, getString(R.string.not_have_connection),Toast.LENGTH_SHORT).show();
            }
        });

        tvForgetPassword.setOnClickListener(view -> {
            startActivity(new Intent(this,SignInActivity.class));
            overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
            finish();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser !=null){
            startActivity(new Intent(getApplication(),MainNavActivity.class));
            overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
            finish();
        }
    }

    private void userSignIn(FirebaseAuth firebaseAuth) {
        email = edtEmail.getText().toString();
        password = edtPassword.getText().toString();
        loadingProgress.startLoadingProgress();
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(this, authResult -> {
            startActivity(new Intent(getApplication(),MainNavActivity.class));
            Toast.makeText(this, getString(R.string.sign_in_successfully),Toast.LENGTH_SHORT).show();
            overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
            loadingProgress.dismissLoadingProgress();
            finish();
        }).addOnFailureListener(this, e -> {
            Toast.makeText(this, getString(R.string.sign_in_failed),Toast.LENGTH_SHORT).show();
            loadingProgress.dismissLoadingProgress();
        });
    }

    private void userSend(FirebaseAuth firebaseAuth) {
        email = edtEmail.getText().toString();
        loadingProgress.startLoadingProgress();
        firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener(this, aVoid -> {
            Toast.makeText(this, getString(R.string.cek_email),Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this,SignInActivity.class));
            overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
            loadingProgress.dismissLoadingProgress();
            finish();
        }).addOnFailureListener(this, e ->{
            loadingProgress.dismissLoadingProgress();
            Toast.makeText(this, getString(R.string.email_failed),Toast.LENGTH_SHORT).show();
        });

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
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SignInActivity.this);
            alertDialogBuilder
                    .setTitle(R.string.action_help)
                    .setMessage(R.string.help_or_questions)
                    .setCancelable(true)
                    .setNeutralButton(R.string.yes, (dialog, id) -> {
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("text/plain");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"aprizal040498@gmail.com"});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.sign_in) + "\n \n");

                        if (emailIntent.resolveActivity(this.getPackageManager()) != null) {
                            startActivity(emailIntent);
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.bg_costume));
            alertDialog.show();
            return true;
        });

        return true;
    }

    private boolean haveConnection(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
}