package com.example.stealth;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DatabaseManager {
    static DatabaseReference dr= FirebaseDatabase.getInstance().getReference("ROOT");
    static DatabaseReference EmailUserid=dr.child("EmailUserid");
    static DatabaseReference Post=dr.child("Post");
    static DatabaseReference Comment=dr.child("Comment");
    static DatabaseReference MyActivity=dr.child("MyActivity");
    static DatabaseReference Vote=dr.child("Vote");
    static DatabaseReference Poll = dr.child("Poll");
    static DatabaseReference PollVote = dr.child("PollVote");
    static DatabaseReference ReportCount = dr.child("ReportCount");
    static final int MaxReport=3;
//    static public void Out(String s)
//    {textView.setText(textView.getText()+s+"\n");}

}
