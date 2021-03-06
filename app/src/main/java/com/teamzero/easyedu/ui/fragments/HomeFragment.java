package com.teamzero.easyedu.ui.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.orhanobut.logger.Logger;
import com.teamzero.easyedu.R;
import com.teamzero.easyedu.adapters.HomeRecyclerAdapter;
import com.teamzero.easyedu.models.UploadDocumentModel;
import com.teamzero.easyedu.ui.activities.UploadDocumentActivity;
import com.teamzero.easyedu.utils.FireStoreQueryLiveData;
import com.teamzero.easyedu.utils.SharedPrefsUtils;
import com.teamzero.easyedu.viewmodel.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class HomeFragment extends Fragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.recycler_home_frag_main)
    RecyclerView rvMain;

    private FirebaseFirestore mDb;
    private CollectionReference collectionReference;
    private List<String> followers;

    private HomeRecyclerAdapter mAdapter;
    private SharedPrefsUtils sharedPrefsUtils;

    private HomeViewModel homeViewModel;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPrefsUtils = new SharedPrefsUtils(getContext());
        homeViewModel = ViewModelProviders.of(getActivity()).get(HomeViewModel.class);
        setUpRecyclerView();
        initDatabase();
    }

    private void setUpRecyclerView() {
        mAdapter = new HomeRecyclerAdapter(getContext());
        mAdapter.setHasStableIds(true);
        rvMain.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        rvMain.setAdapter(mAdapter);
    }

    private void initDatabase() {
        mDb = FirebaseFirestore.getInstance();
        collectionReference = mDb.collection("Uploads");
        followers = sharedPrefsUtils.getFollowList("SUBJECTS");
        refreshEntries();
    }

    private void refreshEntries() {
        FireStoreQueryLiveData queryLiveData = new FireStoreQueryLiveData(collectionReference);
        if (queryLiveData.hasActiveObservers()) {
            queryLiveData.removeObservers(getActivity());
        }
        queryLiveData.observe(getActivity(), new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(QuerySnapshot queryDocumentSnapshots) {
                List<UploadDocumentModel> uploadDocumentModels = queryDocumentSnapshots.toObjects(UploadDocumentModel.class);
                homeViewModel.cacheDataInSql(uploadDocumentModels);
            }
        });
        getCachedDataFromRoom();
    }

    @OnClick(R.id.fab_home_frag_upload)
    void fabClicked() {
        Intent intent = new Intent(getContext(), UploadDocumentActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SharedPrefsUtils.SHARED_PREF_FOLLOW_LIST)) {
            followers = sharedPrefsUtils.getFollowList("SUBJECTS");
            Logger.d(followers);
            refreshEntries();
        }
    }

    private void getCachedDataFromRoom() {
        LiveData<List<UploadDocumentModel>> cachedData = homeViewModel.getCachedData();
        if (cachedData.hasActiveObservers()) {
            cachedData.removeObservers(this);
        }
        cachedData.observe(this, new Observer<List<UploadDocumentModel>>() {
            @Override
            public void onChanged(List<UploadDocumentModel> uploadDocumentModels) {
                List<UploadDocumentModel> models = new ArrayList<>();
                for (int i = 0; i < uploadDocumentModels.size(); i++) {
                    UploadDocumentModel model = uploadDocumentModels.get(i);
                    String subject = model.getSubject();
                    if (followers.contains(subject)) {
                        models.add(model);
                    }
                }
                mAdapter.setDataItem(models);
            }
        });
    }

}
