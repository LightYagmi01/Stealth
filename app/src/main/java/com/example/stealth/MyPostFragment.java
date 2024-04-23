package com.example.stealth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MyPostFragment extends Fragment {
    TextView txtNoPost;
    public MyPostFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_post, container, false);
        txtNoPost = rootView.findViewById(R.id.txtNoPost);

        RecyclerView recyclerView;
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerPostAdapter adapter = new RecyclerPostAdapter(getActivity(), BackendCommon.myPosts.Posts,BackendCommon.myPosts,RecyclerPostAdapter.PersonalPost);
        recyclerView.setAdapter(adapter);
        BackendCommon.myPosts.adapter=adapter;
        if (adapter.getItemCount() == 0) {
            txtNoPost.setVisibility(View.VISIBLE);
        } else {
            txtNoPost.setVisibility(View.GONE);
        }
        return rootView;
    }
}