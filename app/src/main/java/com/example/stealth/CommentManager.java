package com.example.stealth;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;

public class CommentManager extends DatabaseManager implements Serializable {
    String ParentKey;
    String UserId;
    ArrayList<CommentInfo> Comments;
    RecyclerCommentAdapter adapter;
    String CommentKey;
    long CommentToRead;
    public void addListenerforHead()
    {
        Comment.child(ParentKey).child("Head").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String key=snapshot.getValue(String.class);

                if(key==null||(!Comments.isEmpty() && (Comments.get(0).Key).equals(key)))return;
                if(Comments.isEmpty())
                {
//                    Out("Head:"+key);
                    CommentKey=key;
                    RetrieveNComment(10);
                    return;
                }
                Comment.child(ParentKey).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        CommentInfo cinfo =new CommentInfo();
                        if(!snapshot.exists())return ;
                        cinfo.Info=snapshot.child("Info").getValue(String.class);
                        cinfo.Key=key;
                        cinfo.UserId=snapshot.child("User").getValue(String.class);
                        Comments.add(0,cinfo);
                        OnCompleteCommentRead();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}});
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }
    public void addListenerForRemoved()
    {
        Comment.child(ParentKey).child("Removed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String key=snapshot.getValue(String.class);
                if(Comments.isEmpty() || key==null)return;
                else
                {
                    for(int i=0;i<Comments.size();i++)
                    {
                        if(Comments.get(i).Key.equals(key))
                        {
                            Comments.remove(i);
                            OnCompleteCommentRead();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }
    CommentManager(String PKey,String User)
    {
        UserId=User;
        CommentToRead=0;
        Comments=new ArrayList<>();
        ParentKey=PKey;
        addListenerforHead();
        addListenerForRemoved();
    }
    public void ResetCommentKey()
    {
        Comment.child(ParentKey).child("Head").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                CommentKey=snapshot.getValue(String.class);
            }
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        CommentToRead=0;
    }
    public boolean OnCompleteCommentRead()
    {
        if(adapter!=null)adapter.notifyDataSetChanged();
        return true;
    }
    public boolean OnSingleCommentRead(CommentInfo cinfo)
    {
        if(cinfo==null)
        {
            OnCompleteCommentRead();
            return false;
        }
        //Write your code here
        Comments.add(cinfo);
        if(--CommentToRead>0)
            GetNextComment();
        else OnCompleteCommentRead();
        return true;
    }
    public boolean  GetNextComment()
    {
        if(CommentKey==null)
        {
            OnSingleCommentRead(null);
            return false;
        }
        Comment.child(ParentKey).child(CommentKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                CommentInfo cinfo=new CommentInfo();
                if(!snapshot.exists())OnSingleCommentRead(null);
//                Out("Get Next function is called"+CommentKey);
                cinfo.Info=snapshot.child("Info").getValue(String.class);
                cinfo.Key=CommentKey;
                CommentKey=snapshot.child("Next").getValue(String.class);
                cinfo.UserId=snapshot.child("User").getValue(String.class);
                OnSingleCommentRead(cinfo);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        return true;
    }

    public void RetrieveNComment(int n)
    {
        CommentToRead=n;
        GetNextComment();
    }


    public void MakeComment(String info)
    {
        if(UserId==null)return;
        String key=Comment.child(ParentKey).push().getKey();
        Comment.child(ParentKey).child("Head").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String HeadKey=snapshot.getValue(String.class);
                Comment.child(ParentKey).child(key).child("User").setValue(UserId);
                Comment.child(ParentKey).child(key).child("Info").setValue(info);
                Comment.child(ParentKey).child(key).child("Report").setValue((long)0);
                if(HeadKey!=null)
                {
                    Comment.child(ParentKey).child(key).child("Next").setValue(HeadKey);
                    Comment.child(ParentKey).child(HeadKey).child("Prev").setValue(key);
                }
                Comment.child(ParentKey).child("Head").setValue(key);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public void DeleteComment(String CommentId)
    {
        Comment.child(ParentKey).child(CommentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists())return ; //if already deleted
                String Prev=snapshot.child("Prev").getValue(String.class);
                String Next=snapshot.child("Next").getValue(String.class);
                Comment.child(ParentKey).child("Removed").setValue(CommentId);
                if(Prev!=null)
                    Comment.child(ParentKey).child(Prev).child("Next").setValue(Next);
                else Comment.child(ParentKey).child("Head").setValue(Next);
                if(Next!=null)
                    Comment.child(ParentKey).child(Next).child("Prev").setValue(Prev);
                Comment.child(ParentKey).child(CommentId).removeValue();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }
    public void IncreaseReport(String CommentId)
    {
        Comment.child(ParentKey).child(CommentId).child("Report").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long report=snapshot.getValue(long.class);
                if(report>=MaxReport)
                {
                    DeleteComment(CommentId);
                }
                else Comment.child(ParentKey).child(CommentId).child("Report").setValue(report+1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void ReportComment(String CommentId)
    {
        ReportCount.child("Comment").child(CommentId).child(UserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())return;
                else
                {
                    IncreaseReport(CommentId);

                    ReportCount.child("Comment").child(CommentId).child(UserId).setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}
