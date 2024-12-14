package com.example.gymlove;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import android.provider.MediaStore;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private ImageView avatarImageView;
    private TextView userNameTextView, userDescriptionTextView, trainingsLabel;
    private LinearLayout trainingsContainer;
    private MaterialButton editProfileButton, chooseAvatarButton, saveProfileButton, manageTrainingsButton;
    private LinearLayout editLayout;
    private EditText editDescriptionEditText;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    private Bitmap selectedBitmap = null; // Хранит выбранный аватар

    private static final int PICK_IMAGE_REQUEST = 101;

    private boolean isTrainer = false;
    private String userId; // ID текущего пользователя (тренера)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        avatarImageView = findViewById(R.id.avatarImageView2);
        userNameTextView = findViewById(R.id.userNameTextView2);
        userDescriptionTextView = findViewById(R.id.userDescriptionTextView2);
        trainingsLabel = findViewById(R.id.trainingsLabel2);
        trainingsContainer = findViewById(R.id.trainingsContainer);

        editProfileButton = findViewById(R.id.editProfileButton2);
        editLayout = findViewById(R.id.editLayout2);
        chooseAvatarButton = findViewById(R.id.chooseAvatarButton2);
        saveProfileButton = findViewById(R.id.saveProfileButton);
//        manageTrainingsButton = findViewById(R.id.manageTrainingsButton);

        editDescriptionEditText = findViewById(R.id.editDescriptionEditText);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = currentUser.getUid();
        // Считываем данные о пользователе из базы
        mDatabase.child("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            String name = snapshot.child("name").getValue(String.class);
                            Boolean trainerFlag = snapshot.child("trainer").getValue(Boolean.class);
                            String description = snapshot.child("description").getValue(String.class);
                            String avatarBase64 = snapshot.child("avatarBase64").getValue(String.class);

                            userNameTextView.setText(!TextUtils.isEmpty(name) ? name : "No name");
                            userDescriptionTextView.setText(!TextUtils.isEmpty(description) ? description : "No description");

                            if (!TextUtils.isEmpty(avatarBase64)) {
                                Bitmap avatarBitmap = base64ToBitmap(avatarBase64);
                                if (avatarBitmap != null) {
                                    avatarImageView.setImageBitmap(avatarBitmap);
                                } else {
                                    Log.e(TAG, "Failed to decode avatar bitmap.");
                                    avatarImageView.setImageResource(R.drawable.profile);
                                }
                            } else {
                                avatarImageView.setImageResource(R.drawable.profile);
                            }

                            if (trainerFlag != null && trainerFlag) {
                                isTrainer = true;
                                trainingsLabel.setVisibility(View.VISIBLE);
                                trainingsContainer.setVisibility(View.VISIBLE);

                                // Показать кнопку "Manage Trainings"
                                manageTrainingsButton.setVisibility(View.VISIBLE);
                                manageTrainingsButton.setOnClickListener(v -> {
                                    showManageTrainingsDialog();
                                });

                                loadTrainerTrainings();
                            } else {
                                trainingsLabel.setVisibility(View.GONE);
                                trainingsContainer.setVisibility(View.GONE);
                                manageTrainingsButton.setVisibility(View.GONE);
                                isTrainer = false;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user data", e);
                            Toast.makeText(ProfileActivity.this, "Error loading profile data", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Database error: " + error.getMessage());
                        Toast.makeText(ProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                    }
                });

        // Кнопка редактирования профиля
        editProfileButton.setOnClickListener(v -> {
            editLayout.setVisibility(View.VISIBLE);
            editDescriptionEditText.setText(userDescriptionTextView.getText().toString());
        });

        // Кнопка выбора аватара
        chooseAvatarButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Кнопка сохранения профиля
        saveProfileButton.setOnClickListener(v -> {
            String newDescription = editDescriptionEditText.getText().toString().trim();

            if (TextUtils.isEmpty(newDescription)) {
                editDescriptionEditText.setError("Description cannot be empty");
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("description", newDescription);

            if (selectedBitmap != null) {
                String base64Avatar = bitmapToBase64(selectedBitmap);
                updates.put("avatarBase64", base64Avatar);
            }

            mDatabase.child("users").child(userId).updateChildren(updates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                            userDescriptionTextView.setText(newDescription);

                            if (selectedBitmap != null) {
                                avatarImageView.setImageBitmap(selectedBitmap);
                            }
                            editLayout.setVisibility(View.GONE);
                        } else {
                            Log.e(TAG, "Error updating profile", task.getException());
                            Toast.makeText(ProfileActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void loadTrainerTrainings() {
        trainingsContainer.removeAllViews();
        mDatabase.child("users").child(userId).child("trainings")
                .addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(ProfileActivity.this, "No trainings found", Toast.LENGTH_SHORT).show();
                            trainingsContainer.setVisibility(View.GONE);
                            trainingsLabel.setVisibility(View.GONE);
                            return;
                        }

                        for (DataSnapshot tSnap : snapshot.getChildren()) {
                            String trainingKey = tSnap.getKey();
                            if (trainingKey == null) {
                                Log.e(TAG, "Training key is null");
                                continue;
                            }

                            String planKey = tSnap.child("planKey").getValue(String.class);
                            String trainingName = tSnap.child("planName").getValue(String.class);

                            if (TextUtils.isEmpty(planKey) || TextUtils.isEmpty(trainingName)) {
                                Log.e(TAG, "planKey or planName is missing for trainingKey: " + trainingKey);
                                continue;
                            }

                            // Получаем детали плана из myExercises
                            mDatabase.child("users").child(userId).child("myExercises").child(planKey)
                                    .addListenerForSingleValueEvent(new ValueEventListener(){
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot planSnap) {
                                            if (!planSnap.exists()) {
                                                Log.e(TAG, "Plan with key " + planKey + " does not exist.");
                                                return;
                                            }

                                            String planName = planSnap.child("name").getValue(String.class);
                                            if (TextUtils.isEmpty(planName)) {
                                                planName = "Unnamed Plan";
                                            }

                                            // Получаем упражнения
                                            DataSnapshot exSnap = planSnap.child("exercises");
                                            List<ExerciseItem> exercises = new ArrayList<>();
                                            for (DataSnapshot exSnapChild : exSnap.getChildren()) {
                                                String exName = exSnapChild.getKey();
                                                if (TextUtils.isEmpty(exName)) {
                                                    Log.e(TAG, "Exercise name is missing in plan " + planKey);
                                                    continue;
                                                }

                                                Long setsLong = exSnapChild.child("sets").getValue(Long.class);
                                                Long weightLong = exSnapChild.child("weight").getValue(Long.class);
                                                int sets = (setsLong != null) ? setsLong.intValue() : 0;
                                                int weight = (weightLong != null) ? weightLong.intValue() : 0;

                                                exercises.add(new ExerciseItem(exName, sets, weight, true));
                                            }

                                            // Создаём представление тренировки
                                            View trainingView = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.item_training_profile, trainingsContainer, false);
                                            TextView trainingNameTextView = trainingView.findViewById(R.id.trainingNameTextView);
                                            LinearLayout exerciseListContainer = trainingView.findViewById(R.id.exerciseListContainer);
                                            MaterialButton deleteTrainingButton = trainingView.findViewById(R.id.deleteTrainingButton);

                                            trainingNameTextView.setText(planName);

                                            // Добавляем упражнения
                                            for (ExerciseItem ex : exercises) {
                                                View exView = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.item_exercise_in_profile_training, exerciseListContainer, false);
                                                TextView exNameTextView = exView.findViewById(R.id.exExerciseName);
                                                TextView exSetsWeightTextView = exView.findViewById(R.id.exSetsWeight);
                                                exNameTextView.setText(ex.getName());
                                                exSetsWeightTextView.setText("Sets: " + ex.getSets() + "  Weight: " + ex.getWeight() + "kg");
                                                exerciseListContainer.addView(exView);
                                            }

                                            // Кнопка удаления тренировки
                                            deleteTrainingButton.setOnClickListener(v -> {
                                                showDeleteTrainingDialog(trainingKey);
                                            });

                                            trainingsContainer.addView(trainingView);
                                            trainingsContainer.setVisibility(View.VISIBLE);
                                            trainingsLabel.setVisibility(View.VISIBLE);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e(TAG, "Database error while fetching plan: " + error.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Database error: " + error.getMessage());
                        Toast.makeText(ProfileActivity.this, "Error loading trainings", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Показывает диалог для добавления плана из myExercises в trainings
    private void showManageTrainingsDialog() {
        // Загрузка планов из myExercises
        mDatabase.child("users").child(userId).child("myExercises")
                .addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> planKeys = new ArrayList<>();
                        List<String> planNames = new ArrayList<>();
                        for (DataSnapshot planSnap : snapshot.getChildren()) {
                            String name = planSnap.child("name").getValue(String.class);
                            if (!TextUtils.isEmpty(name)) {
                                planKeys.add(planSnap.getKey());
                                planNames.add(name);
                            }
                        }

                        if (planNames.isEmpty()) {
                            Toast.makeText(ProfileActivity.this, "You have no plans in MyExercises", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Список уже добавленных тренировок, чтобы не добавлять повторно
                        mDatabase.child("users").child(userId).child("trainings")
                                .addListenerForSingleValueEvent(new ValueEventListener(){
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot trainingSnap) {
                                        List<Integer> availableIndices = new ArrayList<>();
                                        for (int i = 0; i < planKeys.size(); i++) {
                                            String planKey = planKeys.get(i);
                                            boolean alreadyAdded = false;
                                            for (DataSnapshot existingTrainingSnap : trainingSnap.getChildren()) {
                                                String existingPlanKey = existingTrainingSnap.child("planKey").getValue(String.class);
                                                if (planKey.equals(existingPlanKey)) {
                                                    alreadyAdded = true;
                                                    break;
                                                }
                                            }
                                            if (!alreadyAdded) {
                                                availableIndices.add(i);
                                            }
                                        }

                                        if (availableIndices.isEmpty()) {
                                            Toast.makeText(ProfileActivity.this, "All your plans are already added to trainings", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        // Создать список доступных для добавления планов
                                        List<String> availablePlanNames = new ArrayList<>();
                                        List<String> availablePlanKeys = new ArrayList<>();
                                        for (Integer index : availableIndices) {
                                            availablePlanNames.add(planNames.get(index));
                                            availablePlanKeys.add(planKeys.get(index));
                                        }

                                        String[] availablePlanArray = availablePlanNames.toArray(new String[0]);

                                        new AlertDialog.Builder(ProfileActivity.this)
                                                .setTitle("Choose a plan to add to Trainings")
                                                .setItems(availablePlanArray, (dialog, which) -> {
                                                    String selectedPlanName = availablePlanNames.get(which);
                                                    String selectedPlanKey = availablePlanKeys.get(which);
                                                    addTrainingToProfile(selectedPlanName, selectedPlanKey);
                                                })
                                                .show();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e(TAG, "Database error while fetching trainings: " + error.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Database error while fetching myExercises: " + error.getMessage());
                    }
                });
    }

    private void addTrainingToProfile(String trainingName, String planKey) {
        // Добавим trainingName и planKey в users/{userId}/trainings push()
        String newKey = mDatabase.child("users").child(userId).child("trainings").push().getKey();
        if (newKey == null) {
            Log.e(TAG, "Failed to generate new training key.");
            Toast.makeText(ProfileActivity.this, "Error adding training", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> trainingData = new HashMap<>();
        trainingData.put("planName", trainingName);
        trainingData.put("planKey", planKey);

        mDatabase.child("users").child(userId).child("trainings").child(newKey).setValue(trainingData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Training added to profile", Toast.LENGTH_SHORT).show();
                        loadTrainerTrainings();
                    } else {
                        Log.e(TAG, "Error adding training: ", task.getException());
                        Toast.makeText(ProfileActivity.this, "Error adding training", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDeleteTrainingDialog(String trainingKey) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Training")
                .setMessage("Do you want to remove this training from your profile?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mDatabase.child("users").child(userId).child("trainings").child(trainingKey).removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProfileActivity.this, "Training removed", Toast.LENGTH_SHORT).show();
                                    loadTrainerTrainings();
                                } else {
                                    Log.e(TAG, "Error removing training: ", task.getException());
                                    Toast.makeText(ProfileActivity.this, "Error removing training", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                avatarImageView.setImageBitmap(selectedBitmap);
            } catch (IOException e) {
                Log.e(TAG, "Error loading selected image", e);
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Конвертация Bitmap в Base64
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    // Конвертация Base64 в Bitmap
    private Bitmap base64ToBitmap(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid Base64 string for avatar", e);
            return null;
        }
    }
}
