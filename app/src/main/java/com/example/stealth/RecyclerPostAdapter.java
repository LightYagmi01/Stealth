package com.example.stealth;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;
public class RecyclerPostAdapter extends RecyclerView.Adapter<RecyclerPostAdapter.ViewHolder> {
    Context context;
    ArrayList<PostInfo> arrPosts;
    PostsManager postsManager;
    int PostType;
    static final int GeneralPost=0;
    static final int PersonalPost=1;
    int working=0;// allowing one click listener to be active at a time

    RecyclerPostAdapter(Context context, ArrayList<PostInfo> arrPosts,PostsManager postsManager,int PostType) {
        this.context = context;
        this.arrPosts = arrPosts;
        this.postsManager=postsManager;
        this.PostType=PostType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_row, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtPost.setText(arrPosts.get(position).Info);
        holder.txtUp.setText(String.valueOf(arrPosts.get(position).UpVote));
        holder.txtDown.setText(String.valueOf(arrPosts.get(position).DownVote));

        if(arrPosts.get(position).VoteType == 1) {
            holder.txtUp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_upward_filled, 0, 0, 0);
            holder.txtDown.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_downward, 0, 0, 0);

        }        else if (arrPosts.get(position).VoteType == -1) {
            holder.txtDown.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_downward_filled, 0, 0, 0);
            holder.txtUp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_upward, 0, 0, 0);

        }
        else {
            holder.txtDown.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_downward, 0, 0, 0);
            holder.txtUp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_upward, 0, 0, 0);
        }
        if (position == getItemCount() - 3) {
//            if(postsManager.PostKey==null)
//                Toast.makeText(context, "Reach End", Toast.LENGTH_SHORT).show();
            postsManager.RetrieveNPost(5);
        }




        // Set click listener for imgReport
        holder.imgReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_popup, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menuReport) {

                            LayoutInflater inflater = LayoutInflater.from(context);
                            View customView = inflater.inflate(R.layout.report_dialog, null);
                            Button button = customView.findViewById(R.id.btnReport);
                            EditText editText = customView.findViewById(R.id.edtReport);
                            RadioGroup radioGroup = customView.findViewById(R.id.radio_group);
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setView(customView);
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String text = editText.getText().toString();
                                    if(text.isEmpty() || radioGroup.getCheckedRadioButtonId() == -1)
                                        return;
                                    postsManager.ReportPost(arrPosts.get(position));
                                    Toast.makeText(context, "Report Sent", Toast.LENGTH_SHORT).show();
                                    alertDialog.dismiss();
                                }
                            });
                            return true;
                        } else if (item.getItemId() == R.id.menuCopy) {
                            CharSequence text = arrPosts.get(position).Info;
                            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Copied Text", text);
                            clipboard.setPrimaryClip(clip);
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
        holder.txtUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postsManager.UpVote(arrPosts.get(position));
            }
        });
        holder.txtDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)  {
                postsManager.DownVote(arrPosts.get(position));
            }
        });
        holder.post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostInfo clickedPost = arrPosts.get(position);

                Intent intent = new Intent(context, PostDetailsActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("PostType",PostType);
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return arrPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPost, txtUp, txtDown, txtComment;
        ImageView imgReport;
        CardView post;
        public ViewHolder(@NonNull View item) {
            super(item);
            post = item.findViewById(R.id.postLayout);
            txtPost = item.findViewById(R.id.txtPost);
            txtUp = item.findViewById(R.id.txtUp);
            txtDown = item.findViewById(R.id.txtDown);
            txtComment = item.findViewById(R.id.txtComment);
            imgReport = item.findViewById(R.id.imgReport);        }
    }
}