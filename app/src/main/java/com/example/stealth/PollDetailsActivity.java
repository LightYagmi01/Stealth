package com.example.stealth;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;

public class PollDetailsActivity extends AppCompatActivity {
    private BottomSheetBehavior bottomSheetBehavior;
    TextView txtTitle;
    ImageView imgSend;
    EditText edtComment;
    PollInfo pollInfo;
    int pollType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_poll_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar topBar = findViewById(R.id.top_bar);
        topBar.setNavigationIcon(R.drawable.arrow_back);
        topBar.setTitle("Poll");
        topBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final LinearLayout pollLayout = findViewById(R.id.pollLayout);
        pollLayout.post(new Runnable() {
            @Override
            public void run() {
                int pollHeight = pollLayout.getHeight();

                View bottomSheet = findViewById(R.id.bottom_sheet);

                bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenHeight = displayMetrics.heightPixels;

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                bottomSheetBehavior.setPeekHeight(screenHeight - pollHeight - 150);
            }
        });
        int position = getIntent().getIntExtra("position", -1);
        pollType=getIntent().getIntExtra("PollType",-1);
        pollInfo =(pollType==RecyclerPollOptionsAdapter.GeneralPoll)?BackendCommon.pollManager.Polls.get(position):BackendCommon.myPoll.Polls.get(position);
        txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(pollInfo.Title);
        ArrayList<Pair<String,Integer>> arrPolls = pollInfo.Options;

        RecyclerView recyclerViewPoll, recyclerViewComment;
        recyclerViewPoll = findViewById(R.id.recyclerViewPoll);
        recyclerViewPoll.setLayoutManager(new LinearLayoutManager(this));
        RecyclerPollOptionsAdapter adapterPoll = new RecyclerPollOptionsAdapter(this,arrPolls,position,pollType);
        recyclerViewPoll.setAdapter(adapterPoll);
        BackendCommon.commentManager = new CommentManager(pollInfo.Key, pollInfo.UserId);
//        Toast.makeText(this, pollInfo.Key+" "+pollInfo.UserId,Toast.LENGTH_SHORT).show();
        recyclerViewComment = findViewById(R.id.recyclerViewComment);
        recyclerViewComment.setLayoutManager(new LinearLayoutManager(this));
        RecyclerCommentAdapter adapterComment = new RecyclerCommentAdapter(this, BackendCommon.commentManager.Comments );
        recyclerViewComment.setAdapter(adapterComment);
        BackendCommon.commentManager.adapter=adapterComment;
        edtComment = findViewById(R.id.edtComment);
        imgSend = findViewById(R.id.imgSend);
        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = edtComment.getText().toString();
                if(comment.isEmpty())
                    return;
                BackendCommon.commentManager.MakeComment(comment);
                edtComment.setText("");
            }
        });

    }
}