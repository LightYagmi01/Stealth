package com.example.stealth;


import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyPosts extends PostsManager{
    MyPosts(String user)
    {
        super(user);
//        Out("We reach here");

    }
    @Override
    public void addListenerforHead()
    {
        MyActivity.child(UserId).child("Post").child("Head").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String key=snapshot.getValue(String.class);
//                Out("We reach here key:"+key);
                if(key==null||(!Posts.isEmpty() && (Posts.get(0).Key).equals(key)))return;
                if(Posts.isEmpty())
                {
                    PostKey=key;
                    RetrieveNPost(5);
                    return;
                }
                Post.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        PostInfo pinfo =new PostInfo();
                        if(!snapshot.exists())return ;
                        pinfo.Info=snapshot.child("Info").getValue(String.class);
                        pinfo.Key=key;
//                        pinfo.commentManager=new CommentManager(key);
                        pinfo.UserId=snapshot.child("User").getValue(String.class);
                        pinfo.DownVote=snapshot.child("Vote").child("DownVote").getValue(long.class);
                        pinfo.UpVote=snapshot.child("Vote").child("UpVote").getValue(long.class);
                        Posts.add(0,pinfo);
                        Vote.child(key).child(UserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())pinfo.VoteType=snapshot.getValue(long.class);
                                OnCompletePostRead();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}});
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }
    @Override
    public boolean GetNextPost()
    {
        if(PostKey==null)
        {
            OnPostRead(null);
            return false; ///there is no further post to read
        }
        Post.child(PostKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PostInfo pinfo =new PostInfo();
                if(!snapshot.exists())OnPostRead(null);
                pinfo.Info=snapshot.child("Info").getValue(String.class);
                pinfo.Key=PostKey;
//                pinfo.commentManager = new CommentManager(PostKey);
//                PostKey=snapshot.child("Next").getValue(String.class);
                pinfo.UserId=snapshot.child("User").getValue(String.class);
                pinfo.DownVote=snapshot.child("Vote").child("DownVote").getValue(long.class);
                pinfo.UpVote=snapshot.child("Vote").child("UpVote").getValue(long.class);
//                Out("We reach here");
                Vote.child(pinfo.Key).child(UserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())pinfo.VoteType=snapshot.getValue(long.class);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                MyActivity.child(UserId).child("Post").child(PostKey).child("Next").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        PostKey=snapshot.getValue(String.class);
                        OnPostRead(pinfo);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return true; //means atleast one post is there to read
    }

}
