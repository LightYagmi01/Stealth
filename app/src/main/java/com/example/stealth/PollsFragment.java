package com.example.stealth;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class PollsFragment extends Fragment {
    TextView txtNoPoll, txtTitle;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_polls, container, false);
        txtNoPoll = rootView.findViewById(R.id.txtNoPoll);
//        BackendCommon.pollManager=new PollManager(BackendCommon.UserId);
        RecyclerView recyclerView;
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        RecyclerPollAdapter adapter = new RecyclerPollAdapter(getActivity(), BackendCommon.pollManager.Polls,RecyclerPollOptionsAdapter.GeneralPoll);
        recyclerView.setAdapter(adapter);
        BackendCommon.pollManager.adapter=adapter;
//        if (adapter.getItemCount() == 0) {
//            txtNoPoll.setVisibility(View.VISIBLE);
//        } else {
//            txtNoPoll.setVisibility(View.GONE);
//        }
        return rootView;
    }
}