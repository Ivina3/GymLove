package com.example.gymlove;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private EditText loginEmailEditText, loginPasswordEditText;
    private MaterialButton loginButton, registrationButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ваш layout файл

        loginEmailEditText = findViewById(R.id.loginEmailEditText);
        loginPasswordEditText = findViewById(R.id.loginPasswordEditText);
        loginButton = findViewById(R.id.yourLoginButton);
        registrationButton = findViewById(R.id.yourRegistrationButton);


        mAuth = FirebaseAuth.getInstance();

        // Кнопка регистрации - переходим на экран регистрации
        registrationButton.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, Register.class));
        });

        // Кнопка входа
        loginButton.setOnClickListener(view -> {
            String email = loginEmailEditText.getText().toString().trim();
            String password = loginPasswordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(MainActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Вход выполнен", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, UserActivity.class));
                            finish();
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Ошибка входа";
                            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Проверяем, вошел ли пользователь ранее
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Пользователь уже авторизован, переходим на UserActivity
            startActivity(new Intent(MainActivity.this, UserActivity.class));
            finish();
        }
    }
}
