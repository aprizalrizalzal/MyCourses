package com.application.mycourses.ui.home.activity.tab;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.application.mycourses.R;
import com.application.mycourses.model.ModelHome;
import com.application.mycourses.model.ModelUser;
import com.application.mycourses.ui.home.HomeAdapter;
import com.application.mycourses.ui.home.HomeFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MemberFragment extends Fragment {

    private SwipeRefreshLayout refreshLayout;
    private MemberAdapter memberAdapter;
    private List<ModelUser> modelUsers = new ArrayList<>();
    private RecyclerView rvMember;
    private ProgressBar progressBar;

    public MemberFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_member, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        refreshLayout = view.findViewById(R.id.swipeRefresh);
        rvMember = view.findViewById(R.id.rvMember);
        progressBar = view.findViewById(R.id.progressBar);

        AdView adView = view.findViewById(R.id.adViewMember);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        refreshLayout.setOnRefreshListener(() -> {
            if (firebaseUser != null) {
                readMember(firebaseUser,firebaseFirestore,database);
            }
            refreshLayout.setRefreshing(false);
        });

        if (getActivity() !=null){
            if (firebaseUser != null) {
                readMember(firebaseUser, firebaseFirestore, database);
            }
        }

    }

    private void readMember(FirebaseUser firebaseUser, FirebaseFirestore firebaseFirestore, FirebaseDatabase database) {
        progressBar.setVisibility(View.VISIBLE);

        String userId = firebaseUser.getUid();

        database.getReference(getString(R.string.name_class)).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        ModelHome modelHome = dataSnapshot.getValue(ModelHome.class);
                        if (modelHome !=null){
                            String idClass = modelHome.getClassId();
                            changeClass(idClass,firebaseFirestore);
                        }
                    }
                }
            }

            private void changeClass(String idClass, FirebaseFirestore firebaseFirestore) {
                firebaseFirestore.collection(idClass).get().addOnCompleteListener(task -> {
                    modelUsers.clear();
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()){
                        for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())){
                            ModelUser modelUser = documentSnapshot.toObject(ModelUser.class);
                            modelUsers.add(modelUser);
                            memberAdapter = new MemberAdapter(getContext(), modelUsers);
                        }
                        rvMember.setAdapter(memberAdapter);
                        rvMember.setLayoutManager(new LinearLayoutManager(getContext()));
                        rvMember.setHasFixedSize(true);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}