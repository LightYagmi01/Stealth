package com.example.stealth;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;


public class AddPostPollFragment extends Fragment {
    Button btnPost, btnPoll;
    EditText edtPost, edtOpt, edtTitle;
    TextView txtCreatePoll, txtAddOpt, txtPost;
    LinearLayout postLayout, pollLayout;

    static ArrayList<String> AddOption;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_post_poll, container, false);

        edtPost = rootView.findViewById(R.id.edtPost);
        btnPost = rootView.findViewById(R.id.btnPost);
        btnPoll = rootView.findViewById(R.id.btnPoll);
        txtCreatePoll = rootView.findViewById(R.id.txtCreatePoll);
        postLayout = rootView.findViewById(R.id.postLayout);
        pollLayout = rootView.findViewById(R.id.pollLayout);
        txtPost = rootView.findViewById(R.id.txtPost);
        edtOpt = rootView.findViewById(R.id.edtOpt);
        txtAddOpt = rootView.findViewById(R.id.txtAddOpt);
        edtTitle = rootView.findViewById(R.id.edtTitle);
        txtCreatePoll.setPaintFlags(txtCreatePoll.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtPost.setPaintFlags(txtPost.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        AddOption=new ArrayList<>();
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String post = edtPost.getText().toString();
                if(post.isEmpty())
                {
                    edtPost.setError("Empty");
                    return;
                }
                BackendCommon.postsManager.MakePost(post);
                Fragment selectedFragment = new HomeFragment();
                MainActivity.bottomNavigationView.setSelectedItemId(R.id.menuHome);

            }
        });

        txtCreatePoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postLayout.setVisibility(View.GONE);
                pollLayout.setVisibility(View.VISIBLE);
            }
        });
        txtPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pollLayout.setVisibility(View.GONE);
                postLayout.setVisibility(View.VISIBLE);
            }
        });
        txtAddOpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String opt = edtOpt.getText().toString();
            if(opt.isEmpty()) {
                edtOpt.setError("Empty");
                return;
            } else if (AddOption.size() == 5) {
                edtOpt.setError("Maximum Options Reached");
                return;
            }
                AddOption.add(opt);
                edtOpt.setText(null);
                RecyclerView recyclerView;
                recyclerView = rootView.findViewById(R.id.recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                RecyclerAddOptionsAdapter adapter = new RecyclerAddOptionsAdapter(getActivity(), AddOption);
                recyclerView.setAdapter(adapter);
                Toast.makeText(getActivity(),"Added: "+opt, Toast.LENGTH_SHORT ).show();
            }
        });
        btnPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = edtTitle.getText().toString();
                String opt = edtOpt.getText().toString();
                if(opt.isEmpty() || (AddOption.size() == 5) ){
                }
                else {
                    AddOption.add(opt);
                    edtOpt.setText(null);
                }
                if(AddOption.size() < 2) {
                    edtOpt.setError("Add at least 2 options.");
                    return;
                }else if (title.isEmpty()) {
                    edtTitle.setError("Title is Required.");
                    return;
                }
                PollInfo pollInfo=new PollInfo();
                pollInfo.Title=title;
                pollInfo.Options=new ArrayList<>();
                for(int i=0;i<AddOption.size();i++)
                    pollInfo.Options.add(new Pair<>(AddOption.get(i),0));
                BackendCommon.pollManager.MakePoll(pollInfo);
                Fragment selectedFragment = new PollsFragment();
                MainActivity.bottomNavigationView.setSelectedItemId(R.id.menuPoll);

            }
        });
        return rootView;
    }
}