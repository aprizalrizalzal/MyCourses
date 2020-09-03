package com.application.mycourses;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.application.mycourses.model.ModelUser;
import com.application.mycourses.sign.in.SignInActivity;
import com.application.mycourses.ui.home.HomeFragment;
import com.application.mycourses.ui.main.courses.CreateActivity;
import com.application.mycourses.ui.main.courses.JoinActivity;
import com.application.mycourses.ui.main.edit.ProfileActivity;
import com.application.mycourses.ui.settings.SettingsActivity;
import com.application.mycourses.ui.utils.LoadingProgress;

import com.application.mycourses.ui.utils.FabRotate;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainNavActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawer;
    private FloatingActionButton imgBtnHelp,imgBtnCreate,imgBtnJoin;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private TextView appBarMain;
    private CircleImageView imgViewUserNav;
    private TextView userNameNav, emailNav;
    private String userId;
    private LoadingProgress loadingProgress;
    private boolean doubleBackToExitPressedOnce = false;
    private boolean isFabRotate = false;
    private boolean isFabOpen = false;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nav);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appBarMain=findViewById(R.id.tvAppBarMain);
        loadingProgress = new LoadingProgress(this);

        firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        firebaseFirestore=FirebaseFirestore.getInstance();

        MobileAds.initialize(this, initializationStatus -> {
        });
        AdView adView = findViewById(R.id.adViewAppBarMain);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        imgBtnCreate = findViewById(R.id.fabCreate);
        imgBtnJoin = findViewById(R.id.fabJoin);
        imgBtnHelp = findViewById(R.id.fabHelp);
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            if (!isFabOpen){
                showFab();
            }else {
                closeFab();
            }
            isFabRotate = FabRotate.rotateFab(view, !isFabRotate);
        });

        imgBtnHelp.setOnClickListener(view -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainNavActivity.this);
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
                            this.startActivity(emailIntent);
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.bg_costume));
            alertDialog.show();
            closeFab();
        });

        imgBtnCreate.setOnClickListener(view -> {
            if (haveConnection()){
                startActivity(new Intent(this, CreateActivity.class));
                overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                isFabRotate = FabRotate.rotateFab(view, !isFabRotate);
                fab.hide();
                closeFab();
                finish();
            } else {
                Snackbar.make(view, getString(R.string.not_have_connection), BaseTransientBottomBar.LENGTH_SHORT ).show();
            }
        });

        imgBtnJoin.setOnClickListener(view -> {
            if (haveConnection()){
                startActivity(new Intent(this, JoinActivity.class));
                overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                isFabRotate = FabRotate.rotateFab(view, !isFabRotate);
                fab.hide();
                closeFab();
                finish();
            } else {
                Snackbar.make(view, getString(R.string.not_have_connection), BaseTransientBottomBar.LENGTH_SHORT ).show();
            }
        });

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer, toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeaderView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        ImageButton btnEditProfileNav = navHeaderView.findViewById(R.id.btnEditProfileNav);

        btnEditProfileNav.setOnClickListener(view -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
            finish();
        });

        imgViewUserNav = navHeaderView.findViewById(R.id.imgViewUserNav);
        userNameNav = navHeaderView.findViewById(R.id.tvUserNameNav);
        emailNav = navHeaderView.findViewById(R.id.tvEmailNav);

        if (firebaseUser != null) {
            userId=firebaseUser.getUid();
            DocumentReference documentReference = firebaseFirestore.collection(getString(R.string.app_name)).document(userId);
            documentReference.addSnapshotListener((value, error) -> {
                if (value !=null && value.exists()){
                    ModelUser modelUser = value.toObject(ModelUser.class);
                    if (modelUser != null) {
                        if (modelUser.getUrlPicture().equals("urlPicture")){
                            Glide.with(getApplication())
                                    .load(R.mipmap.ic_launcher_round)
                                    .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                                    .into(imgViewUserNav);
                        } else {
                            Glide.with(getApplication())
                                    .load(modelUser.getUrlPicture())
                                    .apply(RequestOptions.placeholderOf(R.mipmap.ic_launcher_round))
                                    .into(imgViewUserNav);
                        }

                        if (modelUser.getUserName().equals("userName")){
                            appBarMain.setText(getString(R.string.update_your_profile));
                            userNameNav.setText(getString(R.string.update_your_profile));
                        } else {
                            appBarMain.setText(modelUser.getUserName());
                            userNameNav.setText(modelUser.getUserName());
                        }
                        appBarMain.setSelected(true);
                        userNameNav.setSelected(true);

                        if (modelUser.getEmailVerify().equals(false)){
                            emailNav.setText(getString(R.string.not_email_verified));
                        } else {
                            emailNav.setText(modelUser.getEmail());
                        }

                    }
                }
            });
        }

        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    private void closeFab() {
        isFabOpen = false;
        imgBtnHelp.animate().translationY(0);
        imgBtnHelp.hide();
        imgBtnCreate.animate().translationY(0);
        imgBtnCreate.animate().translationX(0);
        imgBtnCreate.hide();
        imgBtnJoin.animate().translationX(0);
        imgBtnJoin.hide();
    }

    private void showFab() {
        isFabOpen = true;
        imgBtnHelp.animate().translationY(-getResources().getDimension(R.dimen.fab_animate_i));
        imgBtnHelp.show();
        imgBtnCreate.animate().translationY(-getResources().getDimension(R.dimen.fab_animate_ii));
        imgBtnCreate.animate().translationX(-getResources().getDimension(R.dimen.fab_animate_ii));
        imgBtnCreate.show();
        imgBtnJoin.animate().translationX(-getResources().getDimension(R.dimen.fab_animate_i));
        imgBtnJoin.show();
    }

    private void searchClass(EditText edtIdClass, Dialog dialogJoin) {
        if (!validateIdClass(edtIdClass)){
            return;
        }
        dialogJoin.cancel();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (firebaseUser != null) {
            String idClass = edtIdClass.getText().toString();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Class").child(idClass);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainNavActivity.this);
                        alertDialogBuilder
                                .setTitle(R.string.info)
                                .setMessage(getString(R.string.id_class_found))
                                .setCancelable(false)
                                .setNeutralButton(R.string.yes, (dialogInterface, i) -> joinClass(idClass,firebaseUser, firebaseFirestore))
                                .setNegativeButton(R.string.no,(dialogInterface, i) -> dialogInterface.cancel());
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.bg_costume));
                        alertDialog.show();
                    } else {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainNavActivity.this);
                        alertDialogBuilder
                                .setTitle(R.string.info)
                                .setMessage(getString(R.string.id_class_not_found))
                                .setCancelable(false)
                                .setNegativeButton(R.string.no,(dialogInterface, i) -> dialogInterface.cancel());
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.bg_costume));
                        alertDialog.show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    private void joinClass(String idClass, FirebaseUser firebaseUser, FirebaseFirestore firebaseFirestore) {
        loadingProgress.startLoadingProgress();
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
            Map<String,Object> map = new HashMap<>();
            map.put("idClass",idClass);
            map.put("userId",userId);

            firebaseFirestore.collection(getString(R.string.app_name)).document(userId).collection("Join").document(idClass).set(map)
                    .addOnSuccessListener(this, aVoid -> {
                        loadingProgress.dismissLoadingProgress();

                    })
                    .addOnFailureListener(this, e -> {
                        loadingProgress.dismissLoadingProgress();

                    });
        }
    }

    private boolean haveConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private boolean validateIdClass(EditText edtIdClass){
        String idClass = edtIdClass.getText().toString();
        if (idClass.isEmpty()){
            edtIdClass.setError(getString(R.string.field_not_empty));
            return false;
        } else {
            edtIdClass.setError(null);
            return true;
        }
    }

    private void userOnline(FirebaseAuth firebaseAuth, FirebaseFirestore firebaseFirestore) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser !=null){
            userId = firebaseUser.getUid();
            Map<String, Object> map = new HashMap<>();
            map.put("userSignIn", true);
            map.put("userOnline", true);
            userId = firebaseUser.getUid();

            firebaseFirestore.collection(getString(R.string.app_name)).document(userId).update(map);
        }
    }

    private void userOffline(FirebaseAuth firebaseAuth, FirebaseFirestore firebaseFirestore) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
            String saveCurrencyDate,saveCurrencyTime;

            Calendar calendar = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
            saveCurrencyDate = dateFormat.format(calendar.getTime());

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            saveCurrencyTime = timeFormat.format(calendar.getTime());

            Map<String, Object> map = new HashMap<>();
            map.put("userOnline", false);
            map.put("lastDate", saveCurrencyDate);
            map.put("lastTime",saveCurrencyTime);

            firebaseFirestore.collection(getString(R.string.app_name)).document(userId).update(map);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_nav, menu);
        MenuItem itemSearch = menu.findItem(R.id.action_search);
        itemSearch.setVisible(false);
        MenuItem itemSetting = menu.findItem(R.id.action_setting);
        itemSetting.setOnMenuItemClickListener(menuItem -> {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
            finish();
            return true;
        });
        MenuItem itemHelp = menu.findItem(R.id.action_help);
        itemHelp.setVisible(false);

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new HomeFragment()).commit();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        userOnline(firebaseAuth, firebaseFirestore);
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!doubleBackToExitPressedOnce) {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getText(R.string.on_back), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        } else {
            userOffline(firebaseAuth,firebaseFirestore);
            super.onBackPressed();
        }
    }
}