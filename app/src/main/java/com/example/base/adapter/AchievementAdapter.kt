package com.example.base.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.base.R
import com.example.base.data.model.Achievement

class AchievementAdapter : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    private var achievements: List<Achievement> = emptyList()

    fun submitList(list: List<Achievement>) {
        achievements = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position])
    }

    override fun getItemCount(): Int = achievements.size

    class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        private val ivStatus: ImageView = itemView.findViewById(R.id.ivStatus)

        fun bind(achievement: Achievement) {
            tvTitle.text = achievement.title
            tvDescription.text = achievement.description
            ivIcon.setImageResource(achievement.iconResId)

            if (achievement.isUnlocked) {
                // Unlocked State
                ivIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.primary_color)) // Assuming primary color
                ivStatus.setImageResource(R.drawable.ic_check_circle)
                ivStatus.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.primary_color))
                
                progressBar.visibility = View.GONE
                tvProgress.visibility = View.GONE
            } else {
                // Locked State
                ivIcon.imageTintList = ColorStateList.valueOf(Color.GRAY)
                ivStatus.setImageResource(R.drawable.ic_check_circle) // Or lock icon if available
                ivStatus.imageTintList = ColorStateList.valueOf(Color.LTGRAY) // Make it look disabled
                // Ideally use a lock icon, but check circle grayed out works for now or just hide it
                ivStatus.visibility = View.INVISIBLE 

                // Show progress if applicable
                if (achievement.maxProgress > 1) {
                    progressBar.visibility = View.VISIBLE
                    tvProgress.visibility = View.VISIBLE
                    progressBar.max = achievement.maxProgress
                    progressBar.progress = achievement.progress
                    tvProgress.text = "${achievement.progress}/${achievement.maxProgress}"
                } else {
                    progressBar.visibility = View.GONE
                    tvProgress.visibility = View.GONE
                }
            }
        }
    }
}
