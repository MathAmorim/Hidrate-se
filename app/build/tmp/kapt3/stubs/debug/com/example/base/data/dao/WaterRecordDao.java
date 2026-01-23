package com.example.base.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\bg\u0018\u00002\u00020\u0001J\u0014\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0005J$\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00070\u00032\u0006\u0010\b\u001a\u00020\u00042\u0006\u0010\t\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\nJ \u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\b\u001a\u00020\u00042\u0006\u0010\t\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u0010\u00a8\u0006\u0011"}, d2 = {"Lcom/example/base/data/dao/WaterRecordDao;", "", "getAllTimestamps", "", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getRecordsForDay", "Lcom/example/base/data/model/WaterRecord;", "start", "end", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTotalForDay", "", "insertRecord", "", "record", "(Lcom/example/base/data/model/WaterRecord;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao
public abstract interface WaterRecordDao {
    
    @androidx.room.Insert
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insertRecord(@org.jetbrains.annotations.NotNull
    com.example.base.data.model.WaterRecord record, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM water_record_table WHERE timestamp BETWEEN :start AND :end")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getRecordsForDay(long start, long end, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.example.base.data.model.WaterRecord>> $completion);
    
    @androidx.room.Query(value = "SELECT SUM(amount) FROM water_record_table WHERE timestamp BETWEEN :start AND :end")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getTotalForDay(long start, long end, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Query(value = "SELECT timestamp FROM water_record_table ORDER BY timestamp DESC")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getAllTimestamps(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.Long>> $completion);
}