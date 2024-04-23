package com.example.stealth;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RecyclerPollAdapter extends RecyclerView.Adapter<RecyclerPollAdapter.ViewHolder> {
    private ArrayList<PollInfo> polls;
    Context context;
    private int PollType;
    public RecyclerPollAdapter(Context context, ArrayList<PollInfo> polls,int pollType) {
        this.polls = polls;
        this.context = context;
        this.PollType=pollType;
    }
    @NonNull
    @Override
    public RecyclerPollAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.poll_row, parent, false);
        return new RecyclerPollAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PollInfo poll = polls.get(position);

        holder.txtTitle.setText(poll.Title);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        RecyclerPollOptionsAdapter optionAdapter = new RecyclerPollOptionsAdapter(context, poll.Options,position,PollType);
        holder.recyclerView.setAdapter(optionAdapter);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));

        holder.pollLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PollDetailsActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("PollType",PollType);
                context.startActivity(intent);
            }
        });
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
                                    BackendCommon.pollManager.ReportPoll(BackendCommon.pollManager.Polls.get(position));
                                    Toast.makeText(context, "Report Sent", Toast.LENGTH_SHORT).show();
                                    alertDialog.dismiss();
                                }
                            });
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }
    @Override
    public int getItemCount() {
        return polls.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;
        CardView pollLayout;
        TextView txtTitle;
        ImageView imgReport;
        public ViewHolder(View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.options_recycler_view);
            pollLayout = itemView.findViewById(R.id.pollLayout);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            imgReport = itemView.findViewById(R.id.imgReport);
        }
    }
}

