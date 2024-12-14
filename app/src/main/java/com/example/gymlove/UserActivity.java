package com.example.gymlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class UserActivity extends AppCompatActivity {

    private TextView trainerTextView;
    private androidx.cardview.widget.CardView homeeCard;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private MaterialButton logoutButton; // Кнопка для разлогина

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        trainerTextView = findViewById(R.id.trainerTextView);
        homeeCard = findViewById(R.id.HOMEE);
        logoutButton = findViewById(R.id.logoutButton); // Находим кнопку разлогина

        CardView dietCard = findViewById(R.id.dietnutrition);
        dietCard.setOnClickListener(v -> {
            startActivity(new Intent(UserActivity.this, Diet.class));
        });


        CardView aboutusCard = findViewById(R.id.aboutus);
        aboutusCard.setOnClickListener(v -> {
            startActivity(new Intent(UserActivity.this, MyExercisesActivity.class));
        });

        CardView workoutCard = findViewById(R.id.workout);
        workoutCard.setOnClickListener(v -> {
            startActivity(new Intent(UserActivity.this, WorkoutActivity.class));
        });
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Если нет авторизованного пользователя
            startActivity(new Intent(UserActivity.this, MainActivity.class));
            finish();
            return;
        }
// На UserActivity есть кнопка профиля, найдите её:
        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> {
            startActivity(new Intent(UserActivity.this, ProfileActivity.class));
        });

        // Обработчик нажатия на кнопку "Разлогиниться"
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(UserActivity.this, MainActivity.class));
            finish();
        });

        String userId = currentUser.getUid();

        // Считываем флаг trainer из БД
        mDatabase.child("users").child(userId).child("trainer")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean isTrainer = snapshot.getValue(Boolean.class);

                        if (isTrainer != null && isTrainer) {
                            // Если тренер
                            trainerTextView.setText("LOOK YOUR CLIENT");
                            // Карточка перехода для тренера
                            homeeCard.setOnClickListener(v -> {
                                // Открываем активити для тренера
                                startActivity(new Intent(UserActivity.this, TrainerActivity.class));
                            });
                        } else {
                            // Не тренер
                            trainerTextView.setText("CHOOSE TRAINER");
                            // Карточка перехода для обычного пользователя
                            homeeCard.setOnClickListener(v -> {
                                startActivity(new Intent(UserActivity.this, ChooseTrainerActivity.class));
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Обработка ошибки
                    }
                });
    }
}
