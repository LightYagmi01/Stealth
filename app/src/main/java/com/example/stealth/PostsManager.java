package com.example.stealth;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PostsManager extends DatabaseManager{
    String PostKey;
    int PostToRead;
    String UserId;
    ArrayList<PostInfo> Posts;
    RecyclerPostAdapter adapter;
    PostDetailsActivity DetailsRef;
    public void addListenerforHead()
    {
        Post.child("Head").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String key=snapshot.getValue(String.class);
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
                        pinfo.VoteType=0;
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
    public void addListenerForRemoved()
    {
        Post.child("Removed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String key=snapshot.getValue(String.class);
                if(Posts.isEmpty() || key==null)return;
                else
                {
                    for(int i=0;i<Posts.size();i++)
                    {
                        if(Posts.get(i).Key.equals(key))
                        {
                            Posts.remove(i);
                            OnCompletePostRead();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    PostsManager(String User) {
        UserId=User;
        PostToRead=0;
        Posts=new ArrayList<>();
        addListenerforHead();
        addListenerForRemoved();
    }
    public void OnCompletePostRead()
    {
        for(int i=0;i<BackendCommon.myPosts.Posts.size();i++)                           //Try to make it efficient
        {
            for(int j=0;j<BackendCommon.postsManager.Posts.size();j++) {
                if (BackendCommon.postsManager.Posts.get(j).Key.equals(BackendCommon.myPosts.Posts.get(i).Key)) {
                    PostInfo pinfo = BackendCommon.myPosts.Posts.get(i);
                    BackendCommon.postsManager.Posts.set(j,pinfo);
                }
            }
        }
        if(BackendCommon.postsManager.adapter!=null)BackendCommon.postsManager.adapter.notifyDataSetChanged();
        if(BackendCommon.myPosts.adapter!=null)BackendCommon.myPosts.adapter.notifyDataSetChanged();
        retriving=0;
//        this.UpVote(Posts.get(0));
    }
    int retriving=0;
    public boolean RetrieveNPost(int n)
    {
        if(retriving==1)return false;
        retriving=1;
        if(PostKey == null)
                return false;
        PostToRead=n;
        GetNextPost();
        return true;
    }
    public void ResetPostKey()
    {
        Post.child("Head").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PostKey=snapshot.getValue(String.class);
                RetrieveNPost(5);
            }
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    protected boolean OnPostRead(PostInfo pinfo)
    {
        if(pinfo==null)
        {
            OnCompletePostRead();
            return false;
        }
        Posts.add(pinfo);
        if(--PostToRead>0)
        {
            GetNextPost();
        }
        else OnCompletePostRead();
        return true;
    }
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
                pinfo.UserId=snapshot.child("User").getValue(String.class);
                pinfo.DownVote=snapshot.child("Vote").child("DownVote").getValue(long.class);
                pinfo.UpVote=snapshot.child("Vote").child("UpVote").getValue(long.class);
                pinfo.VoteType=0;
                Vote.child(pinfo.Key).child(UserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())pinfo.VoteType=snapshot.getValue(long.class);
                                OnPostRead(pinfo);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                PostKey=snapshot.child("Next").getValue(String.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return true; //means atleast one post is there to read
    }

    public void AddMyActivityPostRef(String key) {
        MyActivity.child(UserId).child("Post").child("Head").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String HeadKey=snapshot.getValue(String.class);
                if(HeadKey!=null)
                {
                    MyActivity.child(UserId).child("Post").child(key).child("Next").setValue(HeadKey);
                    MyActivity.child(UserId).child("Post").child(HeadKey).child("Prev").setValue(key);
                }
                MyActivity.child(UserId).child("Post").child("Head").setValue(key);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public void MakePost(String info)
    {
        if(UserId==null)return;
        String key=Post.push().getKey();
        // Don't remove this below code , users of other application will also change this
        Post.child("Head").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String HeadKey=snapshot.getValue(String.class);
                Post.child(key).child("User").setValue(UserId);
                Post.child(key).child("Info").setValue(info);
                Post.child(key).child("Vote").child("UpVote").setValue((long)(0));
                Post.child(key).child("Vote").child("DownVote").setValue((long)(0));
                Post.child(key).child("Report").setValue((long)(0));
                if(HeadKey!=null)
                {
                    Post.child(key).child("Next").setValue(HeadKey);
                    Post.child(HeadKey).child("Prev").setValue(key);
                }
                Post.child("Head").setValue(key);
                AddMyActivityPostRef(key);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public void DeleteActivityPostRef(PostInfo pinfo)
    {
        MyActivity.child(pinfo.UserId).child("Post").child(pinfo.Key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(!snapshot.exists() )return ; //if already deleted
                String Prev=snapshot.child("Prev").getValue(String.class);
                String Next=snapshot.child("Next").getValue(String.class);
                if(Prev!=null)
                    MyActivity.child(pinfo.UserId).child("Post").child(Prev).child("Next").setValue(Next);
                else MyActivity.child(pinfo.UserId).child("Post").child("Head").setValue(Next);
                if(Next!=null)
                    MyActivity.child(pinfo.UserId).child("Post").child(Next).child("Prev").setValue(Prev);
                MyActivity.child(pinfo.UserId).child("Post").child(pinfo.Key).removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void DeletePost(PostInfo pinfo)
    {
        Post.child(pinfo.Key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists())return ; //if already deleted
                String Prev=snapshot.child("Prev").getValue(String.class);
                String Next=snapshot.child("Next").getValue(String.class);
                String User=snapshot.child("User").getValue(String.class);
                Post.child("Removed").setValue(pinfo.Key);
                if(Prev!=null)
                    Post.child(Prev).child("Next").setValue(Next);
                else Post.child("Head").setValue(Next);
                if(Next!=null)
                    Post.child(Next).child("Prev").setValue(Prev);
                DeleteActivityPostRef(pinfo);
                Post.child(pinfo.Key).removeValue();
                Vote.child(pinfo.Key).removeValue();
                Comment.child(pinfo.Key).removeValue();
                ReportCount.child("Post").child(pinfo.Key).removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void IncreaseReport(PostInfo pinfo)
    {
        Post.child(pinfo.Key).child("Report").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long report=snapshot.getValue(long.class);
                if(report+1>=MaxReport)
                {
                    DeletePost(pinfo);
                }
                else Post.child(pinfo.Key).child("Report").setValue(report+1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void ReportPost(PostInfo pinfo)
    {
        ReportCount.child("Post").child(pinfo.Key).child(UserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())return;    //so that same user cannot report again
                else
                {
                    IncreaseReport(pinfo);
                    ReportCount.child("Post").child(pinfo.Key).child(UserId).setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private String vote1,vote2;
    void UpdateDetailsRef(PostInfo pinfo)
    {
        if(DetailsRef!=null)
        {

            DetailsRef.txtPost.setText(pinfo.Info);
            DetailsRef.txtUp.setText(pinfo.UpVote+"");
            DetailsRef.txtDown.setText(pinfo.DownVote+"");

            if(pinfo.VoteType==0)
            {
                DetailsRef.txtUp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_upward, 0, 0, 0);
                DetailsRef.txtDown.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_downward, 0, 0, 0);
            }
            else if(pinfo.VoteType==1) {
                DetailsRef.txtUp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_upward_filled, 0, 0, 0);
                DetailsRef.txtDown.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_downward, 0, 0, 0);
            }else if (pinfo.VoteType==-1) {
                DetailsRef.txtDown.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_downward_filled, 0, 0, 0);
                DetailsRef.txtUp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_upward, 0, 0, 0);
            }
        }
    }
    int working=0;
    private void OnVoteTypeRead(PostInfo pinfo,String PostId,long VoteType)
    {
        if(working==1)return;

        Post.child(PostId).child("Vote").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                working=1;                              //to ensure that only one transaction at a time is called
                if(currentData.getValue()==null)return Transaction.success(currentData);
                long vote1count = currentData.child(vote1).getValue(long.class);
                long vote2count = currentData.child(vote2).getValue(long.class);
//                LastSelected=pinfo.
                if(pinfo.VoteType==0) {
                    currentData.child(vote1).setValue(1+vote1count);
                }
                else if(pinfo.VoteType==VoteType)
                {
                    currentData.child(vote1).setValue(vote1count-1);
                }
                else if(pinfo.VoteType==-VoteType)
                {
                    currentData.child(vote1).setValue(1+vote1count);
                    currentData.child(vote2).setValue(vote2count-1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                working=0;
                if(committed==false)return;
                if(pinfo.VoteType==1)pinfo.UpVote--;
                else if(pinfo.VoteType==-1)pinfo.DownVote--;
//                pinfo.UpVote=currentData.child("UpVote").getValue(long.class);
//                pinfo.DownVote=currentData.child("DownVote").getValue(long.class);
                if(pinfo.VoteType == VoteType)
                {
                    pinfo.VoteType=0;
                    Vote.child(PostId).child(UserId).removeValue();
                }
                else
                {
                    if(VoteType==1)pinfo.UpVote++;
                    else if(VoteType==-1)pinfo.DownVote++;
                    pinfo.VoteType=VoteType;
                    Vote.child(PostId).child(UserId).setValue(VoteType);

                }
                adapter.notifyDataSetChanged();
                UpdateDetailsRef(pinfo);
            }
        });
    }

    private void Vote(PostInfo pinfo,long VoteType)       //VoteType 1 for Upvote -1 for Downvote
    {
        if(UserId==null)return ;
        if(VoteType==1) {
            vote1 = "UpVote";
            vote2 = "DownVote";
        }
        else if(VoteType==-1)
        {
            vote1="DownVote";
            vote2="UpVote";
        }
        OnVoteTypeRead(pinfo,pinfo.Key,VoteType);
    }
    public void DownVote(PostInfo pinfo)
    {
        Vote(pinfo,-1);
    }

    public void UpVote(PostInfo pinfo) {Vote(pinfo,1);}
}
