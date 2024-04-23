package com.example.stealth;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Optional;

public class PollManager extends DatabaseManager{

    String PollKey;
    int PollToRead;
    String UserId;
    ArrayList<PollInfo> Polls;
    RecyclerPollAdapter adapter;
    public void addListenerforHead()
    {
        Poll.child("Head").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String key=snapshot.getValue(String.class);
                if(key==null||(!Polls.isEmpty() && (Polls.get(0).Key).equals(key)))return;
                if(Polls.isEmpty())
                {
                    PollKey=key;
                    RetrieveNPoll(10);
                    return;
                }
                Poll.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(!snapshot.exists())return ;
                        PollInfo pinfo =new PollInfo();
                        pinfo.Options=new ArrayList<>();
                        pinfo.Title=snapshot.child("Title").getValue(String.class);
                        pinfo.Key=key;
                        pinfo.UserId=snapshot.child("User").getValue(String.class);
//                        pinfo.Options=snapshot.child("Option").getValue(ArrayList<>.class);
                        for(DataSnapshot child:snapshot.child("Option").getChildren())
                        {
                            pinfo.Options.add(new Pair<>(child.getKey(),child.getValue(Integer.class)));
                        }
                        Polls.add(0,pinfo);
                        OnCompletePollRead();

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}});
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }
    public void addListenerForRemoved()
    {
        Poll.child("Removed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String key=snapshot.getValue(String.class);
                if(Polls.isEmpty() || key==null)return;
                else
                {
                    for(int i=0;i<Polls.size();i++)
                    {
                        if(Polls.get(i).Key.equals(key))
                        {
                            Polls.remove(i);
                            OnCompletePollRead();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    PollManager(String User) {
        UserId=User;
        PollToRead=0;
        Polls=new ArrayList<>();
        addListenerforHead();
        addListenerForRemoved();
    }
    public void OnCompletePollRead()
    {
        for(int i=0;i<BackendCommon.myPoll.Polls.size();i++)                           //Try to make it efficient
        {
            for(int j=0;j<BackendCommon.pollManager.Polls.size();j++) {
                if (BackendCommon.pollManager.Polls.get(j).Key.equals(BackendCommon.myPoll.Polls.get(i).Key)) {
                    PollInfo pinfo = BackendCommon.pollManager.Polls.get(j);
                    BackendCommon.myPoll.Polls.set(i,pinfo);
                }
            }
        }
        if(BackendCommon.pollManager.adapter!=null)BackendCommon.pollManager.adapter.notifyDataSetChanged();
        if(BackendCommon.myPoll.adapter!=null)BackendCommon.myPoll.adapter.notifyDataSetChanged();
        retrieving=0;
    }
    int retrieving=0;
    public void RetrieveNPoll(int n)
    {
        if(retrieving==1)return;
        retrieving=1;
        PollToRead=n;
        GetNextPoll();
    }


    protected boolean OnPollRead(PollInfo pinfo)
    {
        if(pinfo==null)
        {
            OnCompletePollRead();
            return false;
        }
        Polls.add(pinfo);
        if(--PollToRead>0)
        {
            GetNextPoll();
        }
        else OnCompletePollRead();
        return true;
    }
    public boolean GetNextPoll()
    {
        if(PollKey==null)
        {
            OnPollRead(null);
            return false; ///there is no further Poll to read
        }
        Poll.child(PollKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists())OnPollRead(null);
                PollInfo pinfo =new PollInfo();
                pinfo.Options=new ArrayList<>();
                pinfo.Title=snapshot.child("Title").getValue(String.class);
                pinfo.Key=PollKey;
                PollKey=snapshot.child("Next").getValue(String.class);
                pinfo.UserId=snapshot.child("User").getValue(String.class);
                PollVote.child(pinfo.Key).child(BackendCommon.UserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pinfo.Selected=snapshot.getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                for(DataSnapshot child:snapshot.child("Option").getChildren())
                {
                    pinfo.Options.add(new Pair<>(child.getKey(),child.getValue(Integer.class)));
                }
                OnPollRead(pinfo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return true; //means atleast one Poll is there to read
    }

    public void AddMyActivityPollRef(String key) {
        MyActivity.child(UserId).child("Poll").child("Head").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String HeadKey=snapshot.getValue(String.class);
                if(HeadKey!=null)
                {
                    MyActivity.child(UserId).child("Poll").child(key).child("Next").setValue(HeadKey);
                    MyActivity.child(UserId).child("Poll").child(HeadKey).child("Prev").setValue(key);
                }
                MyActivity.child(UserId).child("Poll").child("Head").setValue(key);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public void MakePoll(PollInfo pinfo)
    {
        if(UserId==null || pinfo.Options.isEmpty())return;
        String key=Poll.push().getKey();
        // Don't remove this below code , users of other application will also change this
        Poll.child("Head").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String HeadKey=snapshot.getValue(String.class);
                Poll.child(key).child("User").setValue(BackendCommon.UserId);
                Poll.child(key).child("Title").setValue(pinfo.Title);
                for(int i=0;i<pinfo.Options.size();i++)
                {

                    Poll.child(key).child("Option").child(pinfo.Options.get(i).first).setValue(pinfo.Options.get(i).second);
                }
                Poll.child(key).child("Report").setValue((long)(0));
                if(HeadKey!=null)
                {
                    Poll.child(key).child("Next").setValue(HeadKey);
                    Poll.child(HeadKey).child("Prev").setValue(key);
                }
                Poll.child("Head").setValue(key);
                AddMyActivityPollRef(key);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public void DeleteActivityPollRef(PollInfo pinfo)
    {
        MyActivity.child(pinfo.UserId).child("Poll").child(pinfo.Key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(!snapshot.exists() )return ; //if already deleted
                String Prev=snapshot.child("Prev").getValue(String.class);
                String Next=snapshot.child("Next").getValue(String.class);
                if(Prev!=null)
                    MyActivity.child(pinfo.UserId).child("Poll").child(Prev).child("Next").setValue(Next);
                else MyActivity.child(pinfo.UserId).child("Poll").child("Head").setValue(Next);
                if(Next!=null)
                    MyActivity.child(pinfo.UserId).child("Poll").child(Next).child("Prev").setValue(Prev);
                MyActivity.child(pinfo.UserId).child("Poll").child(pinfo.Key).removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void DeletePoll(PollInfo pinfo)
    {
        Poll.child(pinfo.Key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists())return ; //if already deleted
                String Prev=snapshot.child("Prev").getValue(String.class);
                String Next=snapshot.child("Next").getValue(String.class);
                String User=snapshot.child("User").getValue(String.class);
                Poll.child("Removed").setValue(pinfo.Key);
                if(Prev!=null)
                    Poll.child(Prev).child("Next").setValue(Next);
                else Poll.child("Head").setValue(Next);
                if(Next!=null)
                    Poll.child(Next).child("Prev").setValue(Prev);
                DeleteActivityPollRef(pinfo);
                Poll.child(pinfo.Key).removeValue();
                PollVote.child(pinfo.Key).removeValue();
                Comment.child(pinfo.Key).removeValue();
                ReportCount.child("Poll").child(pinfo.Key).removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void IncreaseReport(PollInfo pinfo)
    {
        Poll.child(pinfo.Key).child("Report").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long report=snapshot.getValue(long.class);
                if(report+1>=MaxReport)
                {
                    DeletePoll(pinfo);
                }
                else Poll.child(pinfo.Key).child("Report").setValue(report+1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void ReportPoll(PollInfo pinfo)
    {
        ReportCount.child("Poll").child(pinfo.Key).child(UserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())return;    //so that same user cannot report again
                else
                {
                    IncreaseReport(pinfo);
                    ReportCount.child("Poll").child(pinfo.Key).child(UserId).setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



    int working=0;
    void  SelectPoll(PollInfo pinfo,String ToSelect,RecyclerPollOptionsAdapter OptionAdapter) {
        if(working==1)return;
        Poll.child(pinfo.Key).child("Option").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                working=1;
                if(currentData.getValue()==null)return Transaction.success(currentData);
                if (pinfo.Selected != null) {
                    //decreasing vote by 1
                    currentData.child(pinfo.Selected).setValue(currentData.child(pinfo.Selected).getValue(long.class)-1);
                    if (!ToSelect.equals(pinfo.Selected)) {
                        currentData.child(ToSelect).setValue(currentData.child(ToSelect).getValue(long.class)+1);
                        PollVote.child(pinfo.Key).child(UserId).setValue(ToSelect);
                    }
                    else
                        PollVote.child(pinfo.Key).child(UserId).removeValue();
                } else {
                    currentData.child(ToSelect).setValue(currentData.child(ToSelect).getValue(long.class)+1);
                    PollVote.child(pinfo.Key).child(UserId).setValue(ToSelect);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                working=0;
                if(committed==false)return;
                if (pinfo.Selected != null) {
                    for(int i=0;i<pinfo.Options.size();i++)
                    {
                        if(pinfo.Options.get(i).first.equals(pinfo.Selected))
                            pinfo.Options.set(i,new Pair<>(pinfo.Selected,pinfo.Options.get(i).second-1));
                    }
                    if (!ToSelect.equals(pinfo.Selected)) {
                        pinfo.Selected=ToSelect;
                        PollVote.child(pinfo.Key).child(UserId).setValue(ToSelect);
                        for(int i=0;i<pinfo.Options.size();i++)
                        {
                            if(pinfo.Options.get(i).first.equals(ToSelect))
                                pinfo.Options.set(i,new Pair<>(ToSelect,pinfo.Options.get(i).second+1));
                        }
                    }
                    else {
                        pinfo.Selected = null;
                        PollVote.child(pinfo.Key).child(UserId).removeValue();
                    }
                } else {
                    pinfo.Selected=ToSelect;
                    PollVote.child(pinfo.Key).child(UserId).setValue(ToSelect);
                    pinfo.Options.clear();
                    for(DataSnapshot Option:currentData.getChildren())
                    {
                        pinfo.Options.add(new Pair<>(Option.getKey(),Option.getValue(Integer.class)));
                    }
                }

                OptionAdapter.notifyDataSetChanged();
            }
        });

    }


}

