package com.matrusneh.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM mother_profile LIMIT 1")
    fun getProfile(): Flow<MotherProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: MotherProfile)

    @Insert
    suspend fun insertKick(kick: KickEvent)

    @Query("SELECT * FROM kick_events ORDER BY timestamp DESC")
    fun getAllKicks(): Flow<List<KickEvent>>

    @Query("SELECT COUNT(*) FROM kick_events WHERE date = :date")
    fun getKicksCountByDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM kick_events WHERE timestamp > :since")
    fun getKicksCountSince(since: Long): Flow<Int>

    @Query("SELECT * FROM nutrition_log WHERE date = :date")
    fun getNutritionLogs(date: String): Flow<List<NutritionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNutritionLog(log: NutritionLog)
}
