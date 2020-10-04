package com.application.mycourses.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.application.mycourses.R;
import com.application.mycourses.model.ModelHome;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements HomeFragmentCallback {

    private SwipeRefreshLayout refreshLayout;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private HomeAdapter homeAdapter;
    private List<ModelHome> modelHomes = new ArrayList<>();
    private ProgressBar progressBar;
    private RecyclerView rvHome;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        refreshLayout = view.findViewById(R.id.swipeRefresh);
        progressBar = view.findViewById(R.id.progressBar);
        rvHome = view.findViewById(R.id.rvHome);

        refreshLayout.setOnRefreshListener(() -> {
            readHome(user,database);
            refreshLayout.setRefreshing(false);
        });

        if (getActivity() != null) {
            readHome(user,database);
        }

    }

    private void readHome(FirebaseUser user, FirebaseDatabase database) {
        progressBar.setVisibility(View.VISIBLE);
        String userId = user.getUid();

        DatabaseReference databaseReference;
        databaseReference = database.getReference(getString(R.string.name_class));
        databaseReference.keepSynced(true);

        databaseReference.orderByChild("courses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    modelHomes.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        ModelHome modelHome = dataSnapshot.getValue(ModelHome.class);
                        try {
                            if (dataSnapshot.child(getString(R.string.name_class_member)).child(userId).exists()){
                                if (modelHome != null){
                                    modelHomes.add(modelHome);
                                    homeAdapter = new HomeAdapter(getContext(), modelHomes, HomeFragment.this);
                                }
                                rvHome.setAdapter(homeAdapter);
                                rvHome.setLayoutManager(new LinearLayoutManager(getContext()));
                                rvHome.setHasFixedSize(true);
                            }
                        } catch (Exception e) {
                            readHome(user,database);
                        }
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onShareClick(ModelHome modelHome) {
        Toast.makeText(requireActivity(),getString(R.string.wait),Toast.LENGTH_SHORT).show();
        String mimeType = "text/plain";
        ShareCompat.IntentBuilder
                .from(requireActivity())
                .setType(mimeType)
                .setChooserTitle(getString(R.string.on_share))
                .setText(getResources().getString(R.string.share_text,modelHome.getClassId(),modelHome.getCourses()))
                .startChooser();
    }
}