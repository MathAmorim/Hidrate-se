package com.example.base;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000h\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0010\t\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001bH\u0002J\u0010\u0010\u001c\u001a\u00020\u00192\u0006\u0010\u001d\u001a\u00020\u001bH\u0002J\u0016\u0010\u001e\u001a\u00020\u001b2\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020!0 H\u0002J\b\u0010\"\u001a\u00020\u0019H\u0002J\b\u0010#\u001a\u00020!H\u0002J\b\u0010$\u001a\u00020!H\u0002J\b\u0010%\u001a\u00020\u0019H\u0002J\b\u0010&\u001a\u00020\u0019H\u0016J\u0012\u0010\'\u001a\u00020\u00192\b\u0010(\u001a\u0004\u0018\u00010)H\u0014J\b\u0010*\u001a\u00020\u0019H\u0002J\b\u0010+\u001a\u00020\u0019H\u0002J\b\u0010,\u001a\u00020\u0019H\u0002J(\u0010-\u001a\u00020\u00192\u0006\u0010.\u001a\u00020\u001b2\u0006\u0010/\u001a\u00020\u001b2\u0006\u00100\u001a\u0002012\u0006\u00102\u001a\u00020\u001bH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u00063"}, d2 = {"Lcom/example/base/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "btnMenu", "Landroid/widget/ImageButton;", "db", "Lcom/example/base/data/AppDatabase;", "drawerLayout", "Landroidx/drawerlayout/widget/DrawerLayout;", "navigationView", "Lcom/google/android/material/navigation/NavigationView;", "notificationHelper", "Lcom/example/base/util/NotificationHelper;", "progressWater", "Landroid/widget/ProgressBar;", "tvCurrentIntake", "Landroid/widget/TextView;", "tvGoal", "tvGreeting", "tvNextNotification", "tvPercentage", "tvStreakDays", "waterWave", "Landroid/view/View;", "addWater", "", "amount", "", "animateWave", "percentage", "calculateStreak", "timestamps", "", "", "checkNotificationPermissions", "getEndOfDay", "getStartOfDay", "loadWaterData", "onBackPressed", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "scheduleNextNotification", "setupButtons", "setupNavigation", "updateUI", "current", "goal", "userName", "", "streak", "app_debug"})
public final class MainActivity extends androidx.appcompat.app.AppCompatActivity {
    private androidx.drawerlayout.widget.DrawerLayout drawerLayout;
    private com.google.android.material.navigation.NavigationView navigationView;
    private android.widget.ImageButton btnMenu;
    private com.example.base.data.AppDatabase db;
    private android.widget.TextView tvCurrentIntake;
    private android.widget.TextView tvGoal;
    private android.widget.TextView tvPercentage;
    private android.widget.ProgressBar progressWater;
    private android.widget.TextView tvGreeting;
    private android.widget.TextView tvStreakDays;
    private android.view.View waterWave;
    private android.widget.TextView tvNextNotification;
    private com.example.base.util.NotificationHelper notificationHelper;
    
    public MainActivity() {
        super();
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    private final void setupNavigation() {
    }
    
    private final void setupButtons() {
    }
    
    private final void addWater(int amount) {
    }
    
    private final void loadWaterData() {
    }
    
    private final void updateUI(int current, int goal, java.lang.String userName, int streak) {
    }
    
    private final void animateWave(int percentage) {
    }
    
    private final int calculateStreak(java.util.List<java.lang.Long> timestamps) {
        return 0;
    }
    
    private final long getStartOfDay() {
        return 0L;
    }
    
    private final long getEndOfDay() {
        return 0L;
    }
    
    private final void checkNotificationPermissions() {
    }
    
    private final void scheduleNextNotification() {
    }
    
    @java.lang.Override
    public void onBackPressed() {
    }
}