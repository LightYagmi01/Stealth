package com.example.stealth;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.health.connect.datatypes.units.Percentage;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Optional;

public class RecyclerPollOptionsAdapter extends RecyclerView.Adapter<RecyclerPollOptionsAdapter.ViewHolder> {
    private ArrayList<Pair<String,Integer>> pollOptions;
    private int selectedPosition = -1;  // No selection by default
    private Context context;
    private int totalCount = 0;
    private int pollindex;
    static final int PersonalPoll=0;
    static final int GeneralPoll=1;
    int working =0;
    private int PollType;
    public RecyclerPollOptionsAdapter(Context context, ArrayList<Pair<String,Integer>> pollOptions,int position,int pollType) {
        this.pollOptions = pollOptions;
        this.context = context;
        this.pollindex=position;
        this.PollType=pollType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.option_layout, parent, false);
        return new ViewHolder(view);
    }

int count=2;
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        PollInfo pinfo=(PollType==GeneralPoll)?BackendCommon.pollManager.Polls.get(pollindex):BackendCommon.myPoll.Polls.get(pollindex);
        holder.txtOption.setText(pinfo.Options.get(position).first);
        holder.txtOption.setOnClickListener(v -> {
            if(PollType==GeneralPoll)
                BackendCommon.pollManager.SelectPoll(pinfo,pinfo.Options.get(position).first,this);
            else
                BackendCommon.myPoll.SelectPoll(pinfo,pinfo.Options.get(position).first,this);

        });
        // this will make seek bar non dragable
        holder.seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        if(pinfo.Selected!=null) {
            if (pollOptions != null) {
                totalCount=0;
                for (Pair<String, Integer> option : pollOptions) {
                    totalCount += option.second;
                }
            }
            float percentage =totalCount!=0? Float.max(0, (pinfo.Options.get(position).second / (float) totalCount) * 100):Float.max(0, 0);
            holder.txtPercent.setText(String.format("%.0f%%", percentage));
            if((int)percentage==holder.seekBar.getProgress())return ;       //for some reason poll is not correct if poll is drawn with same percenage again
            Drawable progressDrawable = pinfo.Options.get(position).first.equals(pinfo.Selected)?ContextCompat.getDrawable(context, R.drawable.progress_track_selected):ContextCompat.getDrawable(context, R.drawable.progress_track);
            holder.seekBar.setProgressDrawable(progressDrawable);
            holder.seekBar.setProgress((int) percentage );
        }
        else
        {
            holder.seekBar.setProgress((int) 0);
            holder.txtPercent.setText(null);
        }
    }


@Override
    public int getItemCount() {
        return pollOptions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioButton;
        TextView txtOption, txtPercent;
        SeekBar seekBar;
        ConstraintLayout optionLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            txtOption = itemView.findViewById(R.id.txtOption);
            txtPercent = itemView.findViewById(R.id.txtPercent);
            seekBar = itemView.findViewById(R.id.seekBar);
            optionLayout = itemView.findViewById(R.id.optionLayout);
        }
    }
}
