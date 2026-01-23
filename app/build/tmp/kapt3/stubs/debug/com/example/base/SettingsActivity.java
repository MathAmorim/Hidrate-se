package com.example.base;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u0010H\u0002J\u0018\u0010\u001a\u001a\u00020\u00182\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u0018H\u0002J\u0010\u0010\u001e\u001a\u00020\u00182\u0006\u0010\u001d\u001a\u00020\u0018H\u0002J\b\u0010\u001f\u001a\u00020 H\u0002J\u0012\u0010!\u001a\u00020 2\b\u0010\"\u001a\u0004\u0018\u00010#H\u0014J\b\u0010$\u001a\u00020 H\u0002J\b\u0010%\u001a\u00020 H\u0002J\b\u0010&\u001a\u00020 H\u0002J\u0010\u0010\'\u001a\u00020 2\u0006\u0010(\u001a\u00020)H\u0002J\b\u0010*\u001a\u00020 H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0015X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006+"}, d2 = {"Lcom/example/base/SettingsActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "btnEndTime", "Lcom/google/android/material/button/MaterialButton;", "btnSave", "btnStartTime", "db", "Lcom/example/base/data/AppDatabase;", "etBirthDate", "Lcom/google/android/material/textfield/TextInputEditText;", "etName", "etWeight", "notificationHelper", "Lcom/example/base/util/NotificationHelper;", "selectedBirthDate", "", "selectedEndTime", "", "selectedStartTime", "tvCalculatedGoal", "Landroid/widget/TextView;", "tvGoalExplanation", "calculateAge", "", "birthDate", "calculateGoal", "weight", "", "age", "getMultiplier", "loadUserData", "", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "saveSettings", "setupViews", "showDatePicker", "showTimePicker", "isStart", "", "updateGoalDisplay", "app_debug"})
public final class SettingsActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.example.base.data.AppDatabase db;
    private com.example.base.util.NotificationHelper notificationHelper;
    private com.google.android.material.textfield.TextInputEditText etName;
    private com.google.android.material.textfield.TextInputEditText etWeight;
    private com.google.android.material.textfield.TextInputEditText etBirthDate;
    private android.widget.TextView tvCalculatedGoal;
    private android.widget.TextView tvGoalExplanation;
    private com.google.android.material.button.MaterialButton btnStartTime;
    private com.google.android.material.button.MaterialButton btnEndTime;
    private com.google.android.material.button.MaterialButton btnSave;
    private long selectedBirthDate = 0L;
    @org.jetbrains.annotations.NotNull
    private java.lang.String selectedStartTime = "08:00";
    @org.jetbrains.annotations.NotNull
    private java.lang.String selectedEndTime = "22:00";
    
    public SettingsActivity() {
        super();
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    private final void setupViews() {
    }
    
    private final void loadUserData() {
    }
    
    private final void showDatePicker() {
    }
    
    private final void showTimePicker(boolean isStart) {
    }
    
    private final void updateGoalDisplay() {
    }
    
    private final int calculateAge(long birthDate) {
        return 0;
    }
    
    private final int getMultiplier(int age) {
        return 0;
    }
    
    private final int calculateGoal(float weight, int age) {
        return 0;
    }
    
    private final void saveSettings() {
    }
}