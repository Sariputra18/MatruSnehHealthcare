package com.matrusneh.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    val profile: StateFlow<MotherProfile?>
    val allKicks: StateFlow<List<KickEvent>>
    val kicksToday: StateFlow<Int>
    val kicksLastHour: StateFlow<Int>
    
    private val todayStr: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    init {
        val dao = AppDatabase.getDatabase(application).dao()
        repository = AppRepository(dao)
        profile = repository.profile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        allKicks = repository.allKicks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        kicksToday = repository.getKicksToday(todayStr).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        kicksLastHour = repository.getKicksLastHour(System.currentTimeMillis() - 3600000).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    }

    fun saveProfile(name: String, edd: Long, asha: String) {
        viewModelScope.launch {
            repository.saveProfile(MotherProfile(name = name, edd = edd, ashaPhone = asha))
        }
    }

    private var lastKickTime = 0L
    fun addKick() {
        val now = System.currentTimeMillis()
        if (now - lastKickTime > 500) {
            lastKickTime = now
            viewModelScope.launch {
                repository.insertKick(KickEvent(timestamp = now, date = todayStr))
            }
        }
    }

    fun getNutrition(date: String) = repository.getNutrition(date)

    fun toggleNutrition(log: NutritionLog) {
        viewModelScope.launch {
            repository.saveNutrition(log.copy(isChecked = !log.isChecked))
        }
    }
}
