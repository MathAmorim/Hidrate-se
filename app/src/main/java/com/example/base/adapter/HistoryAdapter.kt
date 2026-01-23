package com.example.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.base.R
import com.example.base.data.model.WaterRecord
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter(private var records: List<WaterRecord> = emptyList()) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    fun updateData(newRecords: List<WaterRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_water_entry, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount(): Int = records.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_amount)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(record: WaterRecord) {
            tvAmount.text = "${record.amount}ml"
            tvTime.text = timeFormat.format(record.timestamp)
        }
    }
}
