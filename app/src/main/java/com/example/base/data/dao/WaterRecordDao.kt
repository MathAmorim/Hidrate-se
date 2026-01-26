package com.example.base.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.base.data.model.WaterRecord

@Dao
interface WaterRecordDao {
    @Insert
    suspend fun insert(record: WaterRecord)

    @Query("SELECT * FROM water_record_table WHERE date = :date")
    suspend fun getRecordsByDate(date: String): List<WaterRecord>

    @Query("SELECT * FROM water_record_table WHERE timestamp BETWEEN :start AND :end")
    suspend fun getRecordsForDay(start: Long, end: Long): List<WaterRecord>

    @Query("SELECT SUM(amount) FROM water_record_table WHERE timestamp BETWEEN :start AND :end")
    suspend fun getTotalForDay(start: Long, end: Long): Int?

    @Query("SELECT timestamp FROM water_record_table ORDER BY timestamp DESC")
    suspend fun getAllTimestamps(): List<Long>

    @Query("SELECT * FROM water_record_table WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp ASC")
    suspend fun getRecordsBetween(start: Long, end: Long): List<WaterRecord>

    @Query("SELECT SUM(amount) FROM water_record_table")
    suspend fun getTotalVolume(): Long?
}
