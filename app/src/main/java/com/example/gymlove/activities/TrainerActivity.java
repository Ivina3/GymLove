package com.example.gymlove.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymlove.R;
import com.example.gymlove.model.User;
import com.example.gymlove.adapter.ClientsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class TrainerActivity extends AppCompatActivity {
    private EditText searchEditText;
    private Toolbar toolbar;
    private RecyclerView clientsRecyclerView;
    private DatabaseReference mDatabase;
    private String trainerId;

    private List<User> clientsList = new ArrayList<>();
    private List<User> filteredList = new ArrayList<>();
    private ClientsAdapter adapter;

    private boolean searchVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.gymlove.R.layout.activity_trainer);

        searchEditText = findViewById(com.example.gymlove.R.id.searchEditText);
        toolbar = findViewById(com.example.gymlove.R.id.toolbar);
        clientsRecyclerView = findViewById(R.id.clientsRecyclerView);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        trainerId = currentUser.getUid(); // Предполагается, что сейчас авторизован тренер

        adapter = new ClientsAdapter(filteredList, clientId -> {
            // По клику на клиента открываем профиль клиента (UserProfileActivity)
            Intent intent = new Intent(TrainerActivity.this, UserProfileActivity.class);
            intent.putExtra("userId", clientId);
            startActivity(intent);
        });
        clientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        clientsRecyclerView.setAdapter(adapter);

        loadClients();

        // Нажатие на toolbar для переключения поиска
        toolbar.setOnClickListener(v -> {
            searchVisible = !searchVisible;
            if (searchVisible) {
                searchEditText.setVisibility(View.VISIBLE);
            } else {
                searchEditText.setVisibility(View.GONE);
                searchEditText.setText("");
                // Сброс фильтра
                filteredList.clear();
                filteredList.addAll(clientsList);
                adapter.notifyDataSetChanged();
            }
        });

        // Фильтрация при вводе текста в поисковое поле
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override
            public void onTextChanged(CharSequence s,int start,int before,int count){
                filterClients(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s){}
        });
    }

    private void loadClients() {
        // Сначала получаем список clientId из "users/{trainerId}/clients"
        mDatabase.child("users").child(trainerId).child("clients")
                .addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> clientIds = new ArrayList<>();
                        for (DataSnapshot clientSnap : snapshot.getChildren()) {
                            clientIds.add(clientSnap.getKey());
                        }
                        // Теперь по каждому clientId получаем name и создаём User
                        fetchClientsData(clientIds);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void fetchClientsData(List<String> clientIds) {
        if (clientIds.isEmpty()) {
            // Нет клиентов
            clientsList.clear();
            filteredList.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        // Загружаем данные о каждом клиенте
        // Можно оптимизировать, но для простоты пройдёмся циклом
        clientsList.clear();
        for (String clientId : clientIds) {
            mDatabase.child("users").child(clientId).addListenerForSingleValueEvent(new ValueEventListener(){
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    boolean trainerFlag = snapshot.child("trainer").getValue(Boolean.class) != null && snapshot.child("trainer").getValue(Boolean.class);

                    User client = new User(clientId, name != null ? name : "", description != null ? description : "", trainerFlag);
                    clientsList.add(client);

                    // После загрузки всех клиентов обновим список
                    if (clientsList.size() == clientIds.size()) {
                        filteredList.clear();
                        filteredList.addAll(clientsList);
                        adapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void filterClients(String query) {
        query = query.toLowerCase();
        List<User> temp = new ArrayList<>();
        for (User c : clientsList) {
            if (c.getName().toLowerCase().contains(query)) {
                temp.add(c);
            }
        }
        filteredList.clear();
        filteredList.addAll(temp);
        adapter.notifyDataSetChanged();
    }
}
