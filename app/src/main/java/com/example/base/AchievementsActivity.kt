package com.example.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base.adapter.AchievementAdapter
import com.example.base.data.AchievementRepository
import com.example.base.data.AppDatabase
import kotlinx.coroutines.launch

class AchievementsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AchievementAdapter
    private lateinit var repository: AchievementRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        recyclerView = findViewById(R.id.rvAchievements)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AchievementAdapter()
        recyclerView.adapter = adapter

        val db = AppDatabase.getDatabase(this)
        repository = AchievementRepository(db.waterRecordDao(), db.userDao())

        loadAchievements()
    }

    private fun loadAchievements() {
        lifecycleScope.launch {
            val achievements = repository.getAchievements()
            adapter.submitList(achievements)
        }
    }
}
