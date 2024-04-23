package com.example.stealth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyPollFragment extends Fragment {
    TextView txtNoPoll;
    public MyPollFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_poll, container, false);
        txtNoPoll = rootView.findViewById(R.id.txtNoPoll);

        RecyclerView recyclerView;
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerPollAdapter adapter = new RecyclerPollAdapter(getActivity(), BackendCommon.myPoll.Polls,RecyclerPollOptionsAdapter.PersonalPoll);
        recyclerView.setAdapter(adapter);
        BackendCommon.myPoll.adapter=adapter;
        if (adapter.getItemCount() == 0) {
            txtNoPoll.setVisibility(View.VISIBLE);
        } else {
            txtNoPoll.setVisibility(View.GONE);
        }
        return rootView;
    }
}