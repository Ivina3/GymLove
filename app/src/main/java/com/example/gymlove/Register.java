package com.example.gymlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
// Если используете Realtime Database
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
// Если используете Firestore
// import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText;
    private CheckBox trainerCheckBox;
    private MaterialButton registerButton;

    private FirebaseAuth mAuth;
    // Для Realtime Database
    private DatabaseReference mDatabase;

    // Если решите использовать Firestore вместо Realtime Database,
    // раскомментируйте следующую строку и удалите соответствующие
    // строчки кода Realtime Database.
    // private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        trainerCheckBox = findViewById(R.id.trainerCheckBox);
        registerButton = findViewById(R.id.registerButton);

        mAuth = FirebaseAuth.getInstance();
        // Инициализируем Realtime Database (если используем её)
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Если бы использовали Firestore:
        // db = FirebaseFirestore.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                boolean isTrainer = trainerCheckBox.isChecked();

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(Register.this, "Введите имя", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Register.this, "Введите email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Register.this, "Введите пароль", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mAuth.getCurrentUser() != null) {
                    mAuth.signOut();
                }
                // Регистрация пользователя через Firebase Auth
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Получаем текущего пользователя
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // Сохраняем дополнительные данные о пользователе в БД
                                    String userId = user.getUid();

                                    // Данные для сохранения
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("name", name);
                                    userData.put("email", email);
                                    userData.put("trainer", isTrainer);

                                    // Сохранение в Realtime Database
                                    mDatabase.child("users").child(userId).setValue(userData)
                                            .addOnCompleteListener(dbTask -> {
                                                if (dbTask.isSuccessful()) {
                                                    Toast.makeText(Register.this,
                                                            "Регистрация прошла успешно!",
                                                            Toast.LENGTH_LONG).show();
                                                    // Перенаправляем пользователя на главный экран
                                                    startActivity(new Intent(Register.this, UserActivity.class));
                                                    finish();
                                                } else {
                                                    Toast.makeText(Register.this,
                                                            "Не удалось сохранить данные пользователя!",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            });

                                    // Если использовать Firestore вместо Realtime Database, можно сделать так:
                                    /*
                                    db.collection("users").document(userId)
                                            .set(userData)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(Register.this,
                                                    "Регистрация прошла успешно!",
                                                    Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(Register.this, UserActivity.class));
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(Register.this,
                                                    "Не удалось сохранить данные!",
                                                    Toast.LENGTH_LONG).show();
                                            });
                                    */
                                }
                            } else {
                                // Ошибка при создании пользователя
                                String errorMessage = task.getException() != null ?
                                        task.getException().getMessage() : "Ошибка регистрации";
                                Toast.makeText(Register.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }
}
