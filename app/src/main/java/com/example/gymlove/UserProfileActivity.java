package com.example.gymlove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.*;

import java.io.ByteArrayOutputStream;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView userAvatarImageView;
    private TextView userNameTextView, userDescriptionTextView;
    private DatabaseReference mDatabase;
    private String userId; // ID пользователя, чей профиль мы смотрим

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userAvatarImageView = findViewById(R.id.userAvatarImageView);
        userNameTextView = findViewById(R.id.userNameTextView);
        userDescriptionTextView = findViewById(R.id.userDescriptionTextView);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Получаем userId из Intent
        userId = getIntent().getStringExtra("userId");
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "No userId provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserProfile(userId);
    }

    private void loadUserProfile(String userId) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(UserProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                String name = snapshot.child("name").getValue(String.class);
                String description = snapshot.child("description").getValue(String.class);
                String avatarBase64 = snapshot.child("avatarBase64").getValue(String.class);

                userNameTextView.setText(!TextUtils.isEmpty(name) ? name : "No name");
                userDescriptionTextView.setText(!TextUtils.isEmpty(description) ? description : "No description");

                if (!TextUtils.isEmpty(avatarBase64)) {
                    Bitmap avatarBitmap = base64ToBitmap(avatarBase64);
                    userAvatarImageView.setImageBitmap(avatarBitmap);
                } else {
                    userAvatarImageView.setImageResource(R.drawable.profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap base64ToBitmap(String base64Str) {
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
