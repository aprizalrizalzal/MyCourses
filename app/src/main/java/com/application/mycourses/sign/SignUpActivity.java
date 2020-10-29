package com.application.mycourses.sign;

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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.application.mycourses.R;
import com.application.mycourses.ui.utils.LoadingProgress;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private EditText edtEmail,edtPassword,edtConfirmPassword;
    private CheckBox checkBoxStatus;
    private Button btnSignUp;
    private String userId,email,password;
    private LoadingProgress loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CircleImageView imageViewAppBar = findViewById(R.id.imgAppBar);
        Glide.with(getApplication())
                .load(R.mipmap.ic_launcher_round)
                .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                .into(imageViewAppBar);
        TextView appBar = findViewById(R.id.tvAppBar);
        appBar.setText(getText(R.string.sign_up));

        loadingProgress = new LoadingProgress(this);

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

        /*MobileAds.initialize(this, initializationStatus -> {
        });
        AdView adView = findViewById(R.id.adViewAppBar);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);*/

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword =findViewById(R.id.edtConfirmPassword);
        checkBoxStatus = findViewById(R.id.checkBokStatus);
        TextView tvTermsAndConditions = findViewById(R.id.tvTermsAndConditions);
        btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvSignUpIn = findViewById(R.id.tvSignUpIn);

        checkBoxStatus.setOnClickListener(view -> {
            if (checkBoxStatus.isChecked()){
                btnSignUp.setEnabled(true);
            }else {
                btnSignUp.setEnabled(false);
            }
        });

        tvTermsAndConditions.setOnClickListener(view -> {

        });

        btnSignUp.setOnClickListener(view -> {
            if (haveConnection()){
                if (!validEmail()||!validPassword()||!validConfirmPassword()){
                    return;
                }
                userSignUp(auth,firestore);
            } else {
                Toast.makeText(this, getString(R.string.not_have_connection),Toast.LENGTH_SHORT).show();
            }
        });

        tvSignUpIn.setOnClickListener(view -> {
            startActivity(new Intent(this, SignInActivity.class));
            overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
            finish();
        });

    }

    private void userSignUp(FirebaseAuth auth, FirebaseFirestore firestore) {
        email = edtEmail.getText().toString();
        password = edtPassword.getText().toString();
        loadingProgress.startLoadingProgress();
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()){
                user = auth.getCurrentUser();
                if (user != null) {
                    userId = user.getUid();

                    Map<String,Object> map = new HashMap<>();
                    map.put("userName","");
                    map.put("userId",userId);
                    map.put("userSignIn",false);
                    map.put("email",email);
                    map.put("emailVerify",false);
                    map.put("urlPicture","");

                    firestore.collection(getString(R.string.users)).document(userId).set(map).addOnCompleteListener(this, taskUser -> {
                        if (taskUser.isSuccessful()){
                            loadingProgress.dismissLoadingProgress();
                            startActivity(new Intent(this,SignInActivity.class));
                            Toast.makeText(this,getString(R.string.sign_up_successfully),Toast.LENGTH_SHORT).show();
                            overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                            finish();
                        } else {
                            loadingProgress.dismissLoadingProgress();
                            Toast.makeText(this,getString(R.string.data_failed),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }else {
                loadingProgress.dismissLoadingProgress();
                Toast.makeText(this,getString(R.string.auth_failed),Toast.LENGTH_SHORT).show();
            }
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
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SignUpActivity.this);
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

    private boolean validConfirmPassword(){
        String confirmPassword = edtConfirmPassword.getText().toString();
        if (confirmPassword.isEmpty()){
            edtConfirmPassword.setError(getString(R.string.field_not_empty));
            return false;
        } else if (!confirmPassword.equals(password)){
            edtPassword.setError(getText(R.string.not_equals_confirm));
            return false;
        } else {
            edtConfirmPassword.setError(null);
            return true;
        }
    }

}