package com.example.stealth;

import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyPoll extends PollManager{
    MyPoll(String User)
    {
        super(User);
    }
    @Override
    public void addListenerforHead()
    {
        MyActivity.child(UserId).child("Poll").child("Head").addValueEventListener(new ValueEventListener() {
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
    public boolean GetNextPoll() {
        if (PollKey == null) {
            OnPollRead(null);
            return false; ///there is no further Poll to read
        }
        Poll.child(PollKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) OnPollRead(null);
                PollInfo pinfo = new PollInfo();
                pinfo.Options = new ArrayList<>();
                pinfo.Title = snapshot.child("Title").getValue(String.class);
                pinfo.Key = PollKey;
//                PollKey = snapshot.child("Next").getValue(String.class);
                pinfo.UserId = snapshot.child("User").getValue(String.class);
                PollVote.child(pinfo.Key).child(BackendCommon.UserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pinfo.Selected = snapshot.getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                for (DataSnapshot child : snapshot.child("Option").getChildren()) {
                    pinfo.Options.add(new Pair<>(child.getKey(), child.getValue(Integer.class)));
                }
                MyActivity.child(BackendCommon.UserId).child("Poll").child(PollKey).child("Next").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        PollKey=snapshot.getValue(String.class);
                        OnPollRead(pinfo);
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
        return true;
    }
}
