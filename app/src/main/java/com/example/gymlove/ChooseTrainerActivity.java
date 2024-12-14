package com.example.gymlove;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChooseTrainerActivity extends AppCompatActivity {
    private EditText searchEditText;
    private RecyclerView trainersRecyclerView;
    private DatabaseReference mDatabase;
    private List<User> trainersList = new ArrayList<>();
    private List<User> filteredList = new ArrayList<>();
    private TrainersAdapter adapter;
    private Toolbar toolbar;

    private boolean searchVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_trainer);

        searchEditText = findViewById(R.id.searchEditText);
        trainersRecyclerView = findViewById(R.id.trainersRecyclerView);
        toolbar = findViewById(R.id.toolbar);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        adapter = new TrainersAdapter(filteredList, trainerId -> {
            // По клику на тренера открываем TrainerProfileActivity
            Intent intent = new Intent(ChooseTrainerActivity.this, TrainerProfileActivity.class);
            intent.putExtra("trainerId", trainerId);
            startActivity(intent);
        });
        trainersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        trainersRecyclerView.setAdapter(adapter);

        loadTrainers();

        // Нажатие на Toolbar для переключения поля поиска
        toolbar.setOnClickListener(v -> {
            searchVisible = !searchVisible;
            if (searchVisible) {
                searchEditText.setVisibility(View.VISIBLE);
            } else {
                searchEditText.setVisibility(View.GONE);
                searchEditText.setText("");
                // Сбрасываем фильтр, показываем всех
                filteredList.clear();
                filteredList.addAll(trainersList);
                adapter.notifyDataSetChanged();
            }
        });

        // Поиск по имени
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override
            public void onTextChanged(CharSequence s,int start,int before,int count){
                filterTrainers(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s){}
        });
    }

    private void loadTrainers() {
        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trainersList.clear();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    Boolean isTrainer = userSnap.child("trainer").getValue(Boolean.class);
                    if (isTrainer != null && isTrainer) {
                        String id = userSnap.getKey();
                        String name = userSnap.child("name").getValue(String.class);
                        String description = userSnap.child("description").getValue(String.class);
                        if (name == null) name = "";
                        if (description == null) description = "";
                        trainersList.add(new User(id, name, description, true));
                    }
                }
                // Изначально показываем всех
                filteredList.clear();
                filteredList.addAll(trainersList);
                adapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void filterTrainers(String query) {
        query = query.toLowerCase();
        List<User> temp = new ArrayList<>();
        for (User t : trainersList) {
            if (t.getName().toLowerCase().contains(query)) {
                temp.add(t);
            }
        }
        filteredList.clear();
        filteredList.addAll(temp);
        adapter.notifyDataSetChanged();
    }
}
