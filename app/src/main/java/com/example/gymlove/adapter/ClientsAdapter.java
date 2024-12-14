package com.example.gymlove.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymlove.R;
import com.example.gymlove.model.User;

import java.util.List;

public class ClientsAdapter extends RecyclerView.Adapter<ClientsAdapter.ViewHolder> {
    private List<User> clientList;
    private OnClientClickListener listener;

    public interface OnClientClickListener {
        void onClientClick(String clientId);
    }

    public ClientsAdapter(List<User> clientList, OnClientClickListener listener) {
        this.clientList = clientList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClientsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trainer, parent, false);
        // Можно использовать тот же item_trainer.xml с TextView
        // Или создать item_client.xml. Главное, чтобы было поле nameTextView
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientsAdapter.ViewHolder holder, int position) {
        User client = clientList.get(position);
        holder.nameTextView.setText(client.getName());
        holder.itemView.setOnClickListener(v -> listener.onClientClick(client.getId()));
    }

    @Override
    public int getItemCount() {
        return clientList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.trainerNameTextView);
        }
    }
}
