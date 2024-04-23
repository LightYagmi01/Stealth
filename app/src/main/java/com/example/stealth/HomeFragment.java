package com.example.stealth;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    TextView txtNoPost;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        txtNoPost = rootView.findViewById(R.id.txtNoPost);
        RecyclerView recyclerView;
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        RecyclerPostAdapter adapter = new RecyclerPostAdapter(getActivity(), BackendCommon.postsManager.Posts,BackendCommon.postsManager,RecyclerPostAdapter.GeneralPost);
        recyclerView.setAdapter(adapter);
        BackendCommon.postsManager.adapter = adapter;

//        if (adapter.getItemCount() == 0) {
//            txtNoPost.setVisibility(View.VISIBLE);
//        } else {
//            txtNoPost.setVisibility(View.GONE);
//        }
        return rootView;
    }
}