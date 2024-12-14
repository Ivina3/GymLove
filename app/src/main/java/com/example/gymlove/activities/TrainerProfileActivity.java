package com.example.gymlove.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gymlove.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class TrainerProfileActivity extends AppCompatActivity {
    private TextView trainerNameTextView, trainerDescriptionTextView;
    private LinearLayout trainingsContainer;
    private MaterialButton subscribeButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String trainerId;
    private boolean isSubscribed = false; // Флаг: подписан ли текущий пользователь на данного тренера

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.gymlove.R.layout.activity_trainer_profile);

        trainerNameTextView = findViewById(com.example.gymlove.R.id.trainerNameTextView);
        trainerDescriptionTextView = findViewById(com.example.gymlove.R.id.trainerDescriptionTextView);
        trainingsContainer = findViewById(com.example.gymlove.R.id.trainingsContainer);
        subscribeButton = findViewById(com.example.gymlove.R.id.subscribeButton);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        trainerId = getIntent().getStringExtra("trainerId");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        String userId = currentUser.getUid();

        // Проверяем, подписан ли пользователь на тренера
        mDatabase.child("users").child(userId).child("subscribedTo").child(trainerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isSubscribed = snapshot.exists();
                        updateSubscribeButtonUI();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Загрузка данных тренера
        mDatabase.child("users").child(trainerId).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String description = snapshot.child("description").getValue(String.class);

                trainerNameTextView.setText(name);
                trainerDescriptionTextView.setText(description);

                DataSnapshot trainingsSnap = snapshot.child("trainings");
                for (DataSnapshot tSnap : trainingsSnap.getChildren()) {
                    String trainingName = tSnap.getValue(String.class);
                    TextView trainingItem = new TextView(TrainerProfileActivity.this);
                    trainingItem.setText(trainingName);
                    trainingItem.setPadding(0, 16, 0, 16);
                    trainingItem.setOnClickListener(v -> {
                        // Добавляем тренировку в myExercises
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            String userId = currentUser.getUid();
                            // Ключ для тренировки: trainerId_trainingKey
                            String key = trainerId + "_" + tSnap.getKey();
                            mDatabase.child("users").child(userId).child("myExercises").child(key).setValue(trainingName);
                            Toast.makeText(TrainerProfileActivity.this, "Added to MyExercises", Toast.LENGTH_SHORT).show();
                        }
                    });
                    trainingsContainer.addView(trainingItem);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Подписка / Отписка при нажатии на кнопку
        subscribeButton.setOnClickListener(v -> {
            FirebaseUser currentU = mAuth.getCurrentUser();
            if (currentU != null) {
                String uId = currentU.getUid();
                if (isSubscribed) {
                    // Уже подписан - отписываемся
                    Map<String,Object> unsub = new HashMap<>();
                    unsub.put("users/"+uId+"/subscribedTo/"+trainerId, null);
                    unsub.put("users/"+trainerId+"/clients/"+uId, null);
                    mDatabase.updateChildren(unsub).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            isSubscribed = false;
                            Toast.makeText(TrainerProfileActivity.this, "Unsubscribed!", Toast.LENGTH_SHORT).show();
                            updateSubscribeButtonUI();
                        } else {
                            Toast.makeText(TrainerProfileActivity.this, "Error unsubscribing", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Не подписан - подписываемся
                    Map<String,Object> updates = new HashMap<>();
                    updates.put("users/"+uId+"/subscribedTo/"+trainerId, true);
                    updates.put("users/"+trainerId+"/clients/"+uId, true);
                    mDatabase.updateChildren(updates).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            isSubscribed = true;
                            Toast.makeText(TrainerProfileActivity.this, "Subscribed!", Toast.LENGTH_SHORT).show();
                            updateSubscribeButtonUI();
                        } else {
                            Toast.makeText(TrainerProfileActivity.this, "Error subscribing", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void updateSubscribeButtonUI() {
        if (isSubscribed) {
            // Подписан: кнопка "Unsubscribe"
            subscribeButton.setText("Unsubscribe");
            subscribeButton.setBackgroundTintList(getResources().getColorStateList(com.example.gymlove.R.color.red, null));
        } else {
            // Не подписан: кнопка "Subscribe"
            subscribeButton.setText("Subscribe");
            subscribeButton.setBackgroundTintList(getResources().getColorStateList(R.color.black, null));
        }
    }
}
