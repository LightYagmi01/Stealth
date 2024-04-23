package com.example.stealth;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;


public class PostDetailsActivity extends AppCompatActivity {
    private BottomSheetBehavior bottomSheetBehavior;
    TextView txtPost, txtUp, txtDown;
    ImageView imgSend;
    EditText edtComment;
    PostsManager postsManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        final LinearLayout postLayout = findViewById(R.id.postLayout);

        postLayout.post(new Runnable() {
            @Override
            public void run() {
                int postHeight = postLayout.getHeight();

                View bottomSheet = findViewById(R.id.bottom_sheet);

                bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenHeight = displayMetrics.heightPixels;

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                bottomSheetBehavior.setPeekHeight(screenHeight - postHeight);
            }
        });

        int position=getIntent().getIntExtra("position",-1);
        int PostType=getIntent().getIntExtra("PostType",-1);
        postsManager=(PostType==RecyclerPostAdapter.GeneralPost)?BackendCommon.postsManager:BackendCommon.myPosts;
        txtPost = findViewById(R.id.txtPost);
        txtUp = findViewById(R.id.txtUp);
        txtDown = findViewById(R.id.txtDown);
        postsManager.DetailsRef=this;
        postsManager.UpdateDetailsRef(postsManager.Posts.get(position));


//        positio
        txtUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postsManager.UpVote(postsManager.Posts.get(position));
            }
        });
        txtDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)  {
                postsManager.DownVote(postsManager.Posts.get(position));
            }
        });
        MaterialToolbar topBar = findViewById(R.id.top_bar);
        topBar.setNavigationIcon(R.drawable.arrow_back);
        topBar.setTitle("Post");
        Menu menu = topBar.getMenu();
        topBar.inflateMenu(R.menu.menu_popup);
        MenuItem menuItem;
        if(BackendCommon.UserId.equals(postsManager.Posts.get(position).UserId))
        {
            menuItem = menu.findItem(R.id.menuReport);
            menuItem.setVisible(false);
        }
        topBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menuCopy) {
                    // Copy text to clipboard
                    CharSequence text = ("Shared via Stealth\n").concat(postsManager.Posts.get(position).Info);
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Copied Text", text);
                    clipboard.setPrimaryClip(clip);
                    return true;
                }else if(item.getItemId() == R.id.menuReport)
                {
                    LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                    View customView = inflater.inflate(R.layout.report_dialog, null);
                    Button button = customView.findViewById(R.id.btnReport);
                    EditText editText = customView.findViewById(R.id.edtReport);
                    RadioGroup radioGroup = customView.findViewById(R.id.radio_group);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                    builder.setView(customView);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String text = editText.getText().toString();
                            if(text.isEmpty() || radioGroup.getCheckedRadioButtonId() == -1)
                                return;
//                            BackendCommon.postsManager.ReportPost(key);
                            alertDialog.dismiss();
                        }
                    });
                }
                return false;
            }
        });


        topBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postsManager.DetailsRef=null;
                finish();
            }
        });
        BackendCommon.commentManager = new CommentManager(postsManager.Posts.get(position).Key,BackendCommon.UserId);
        RecyclerView recyclerView;
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerCommentAdapter adapter = new RecyclerCommentAdapter(this,BackendCommon.commentManager.Comments );
        recyclerView.setAdapter(adapter);
        BackendCommon.commentManager.adapter = adapter;
        edtComment = findViewById(R.id.edtComment);
        imgSend = findViewById(R.id.imgSend);
        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = edtComment.getText().toString();
                if(comment.isEmpty())
                    return;
                BackendCommon.commentManager.MakeComment(comment);
                edtComment.setText(null);
            }
        });
    }
}