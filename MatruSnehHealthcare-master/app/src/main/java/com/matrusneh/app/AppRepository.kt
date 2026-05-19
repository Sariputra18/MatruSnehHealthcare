package com.matrusneh.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRepository(private val dao: AppDao) {
    val profile: Flow<MotherProfile?> = dao.getProfile()
    val allKicks: Flow<List<KickEvent>> = dao.getAllKicks()

    suspend fun saveProfile(profile: MotherProfile) = withContext(Dispatchers.IO) {
        dao.saveProfile(profile)
    }

    suspend fun insertKick(kick: KickEvent) = withContext(Dispatchers.IO) {
        dao.insertKick(kick)
    }

    fun getKicksToday(date: String): Flow<Int> = dao.getKicksCountByDate(date)

    fun getKicksLastHour(since: Long): Flow<Int> = dao.getKicksCountSince(since)

    fun getNutrition(date: String): Flow<List<NutritionLog>> = dao.getNutritionLogs(date)

    suspend fun saveNutrition(log: NutritionLog) = withContext(Dispatchers.IO) {
        dao.saveNutritionLog(log)
    }
}
