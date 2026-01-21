package com.example.base

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var btnMenu: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.btn_menu)

        setupNavigation()
    }

    private fun setupNavigation() {
        // Open drawer on menu button click
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Already on home
                }
                R.id.nav_achievements -> {
                    startActivity(android.content.Intent(this, AchievementsActivity::class.java))
                }
                R.id.nav_settings -> {
                    startActivity(android.content.Intent(this, SettingsActivity::class.java))
                }
                R.id.nav_about -> {
                    startActivity(android.content.Intent(this, AboutActivity::class.java))
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // FAB Stats click
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_stats).setOnClickListener {
            startActivity(android.content.Intent(this, StatisticsActivity::class.java))
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
