package com.example.gymlove;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TrainersAdapter extends RecyclerView.Adapter<TrainersAdapter.ViewHolder> {
    private List<User> trainerList;
    private OnTrainerClickListener listener;

    public interface OnTrainerClickListener {
        void onTrainerClick(String trainerId);
    }

    public TrainersAdapter(List<User> trainerList, OnTrainerClickListener listener) {
        this.trainerList = trainerList;
        this.listener = listener;
    }

    public void updateList(List<User> newList) {
        this.trainerList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrainersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trainer, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainersAdapter.ViewHolder holder, int position) {
        User trainer = trainerList.get(position);
        holder.nameTextView.setText(trainer.getName());
        holder.itemView.setOnClickListener(v -> listener.onTrainerClick(trainer.getId()));
    }

    @Override
    public int getItemCount() {
        return trainerList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.trainerNameTextView);
        }
    }
}
